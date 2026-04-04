package io.septem150.xeric.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskResponse;
import io.septem150.xeric.data.task.TaskService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
@Singleton
public class ProjectXericApiClient {
  private static final MediaType JSON_MEDIA_TYPE =
      MediaType.parse("application/json; charset=utf-8");
  private static final String API_BASE_URL = "https://api.projectxeric.com/";
  private static final String IMAGES_BASE_URL = "https://images.projectxeric.com/";

  private final OkHttpClient httpClient;
  private final Gson gson;
  private final TaskService taskService;

  @Inject
  public ProjectXericApiClient(
      OkHttpClient httpClient, @Named("xericGson") Gson gson, TaskService taskService) {
    this.httpClient = httpClient;
    this.gson = gson;
    this.taskService = taskService;
  }

  private HttpUrl.Builder baseUrl() {
    return HttpUrl.parse(API_BASE_URL).newBuilder();
  }

  private <T> CompletableFuture<T> executeAsync(
      Request request, IOFunction<ResponseBody, T> bodyReader) {
    CompletableFuture<T> future = new CompletableFuture<>();
    httpClient
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException err) {
                future.completeExceptionally(err);
              }

              @Override
              public void onResponse(Call call, Response response) {
                try (Response res = response) {
                  if (!res.isSuccessful()) {
                    String msg = "HTTP " + res.code();
                    ResponseBody errBody = res.body();
                    if (errBody != null) {
                      try {
                        JsonObject json = gson.fromJson(errBody.string(), JsonObject.class);
                        if (json.has("error")) msg = json.get("error").getAsString();
                      } catch (Exception ignored) {
                      }
                    }
                    throw new IOException(msg);
                  }
                  ResponseBody body = res.body();
                  if (body == null) throw new IOException("response body empty");
                  future.complete(bodyReader.apply(body));
                } catch (IOException err) {
                  future.completeExceptionally(err);
                }
              }
            });
    return future;
  }

  private CompletableFuture<String> executeAsync(Request request) {
    return executeAsync(request, ResponseBody::string);
  }

  @FunctionalInterface
  private interface IOFunction<T, R> {
    R apply(T t) throws IOException;
  }

  public void postPlayerData(JsonObject json) {
    Request request =
        new Request.Builder()
            .post(RequestBody.create(JSON_MEDIA_TYPE, gson.toJson(json)))
            .url(baseUrl().addPathSegment("v2").addPathSegment("player").build())
            .build();
    executeAsync(request)
        .exceptionally(
            err -> {
              log.warn("POST /v2/player failed: {}", err.getMessage());
              return null;
            });
  }

  public CompletableFuture<TaskFetchResult> fetchTasksAsync() {
    String cachedHash = taskService.getCachedHash();
    Request hashRequest =
        new Request.Builder()
            .get()
            .url(
                baseUrl()
                    .addPathSegment("v2")
                    .addPathSegment("tasks")
                    .addPathSegment("hash")
                    .build())
            .build();

    return executeAsync(hashRequest)
        .thenCompose(
            bodyString -> {
              String serverHash =
                  gson.fromJson(bodyString, JsonObject.class).get("hash").getAsString();
              if (serverHash.equals(cachedHash)) {
                TaskResponse cached = taskService.loadFromCache();
                if (cached != null && cached.getTasks() != null) {
                  return CompletableFuture.completedFuture(new TaskFetchResult(cached, null));
                }
              }
              return fetchFullTaskList();
            })
        .exceptionally(
            err -> {
              log.warn("Failed to fetch tasks: {}", err.getMessage());
              TaskResponse cached = taskService.loadFromCache();
              if (cached != null && cached.getTasks() != null) {
                return new TaskFetchResult(cached, null);
              }
              throw err instanceof CompletionException
                  ? (CompletionException) err
                  : new CompletionException(err);
            });
  }

  private CompletableFuture<TaskFetchResult> fetchFullTaskList() {
    Request request =
        new Request.Builder()
            .get()
            .url(baseUrl().addPathSegment("v2").addPathSegment("tasks").build())
            .build();

    return executeAsync(request)
        .thenApply(
            bodyString -> {
              Map<Integer, String> oldHashes = new HashMap<>();
              TaskResponse oldCached = taskService.loadFromCache();
              if (oldCached != null && oldCached.getTasks() != null) {
                for (Task oldTask : oldCached.getTasks()) {
                  oldHashes.put(oldTask.getId(), oldTask.getHash());
                }
              }
              taskService.saveToCache(bodyString);
              TaskResponse taskResponse = gson.fromJson(bodyString, TaskResponse.class);
              return new TaskFetchResult(taskResponse, oldHashes);
            });
  }

  public CompletableFuture<byte[]> fetchTaskIcon(String iconFilename) {
    HttpUrl url = HttpUrl.parse(IMAGES_BASE_URL + "tasks/" + iconFilename);
    if (url == null) {
      return CompletableFuture.failedFuture(new IOException("Invalid icon URL: " + iconFilename));
    }
    Request request = new Request.Builder().get().url(url).build();
    return executeAsync(request, ResponseBody::bytes);
  }

  /**
   * Result of a task fetch — contains the task response and optionally the previous task hashes.
   */
  @Getter
  public static class TaskFetchResult {
    private final TaskResponse taskResponse;
    private final Map<Integer, String> previousTaskHashes;

    public TaskFetchResult(TaskResponse taskResponse, Map<Integer, String> previousTaskHashes) {
      this.taskResponse = taskResponse;
      this.previousTaskHashes = previousTaskHashes;
    }
  }
}
