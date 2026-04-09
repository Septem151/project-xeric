package io.septem150.xeric.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.septem150.xeric.data.player.Rank;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskResponse;
import io.septem150.xeric.data.task.TaskService;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Value;
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
    return HttpUrl.parse(API_BASE_URL).newBuilder().addPathSegment("v2");
  }

  private Request get(String... segments) {
    HttpUrl.Builder url = baseUrl();
    for (String s : segments) url.addPathSegment(s);
    return new Request.Builder().get().url(url.build()).build();
  }

  private TaskFetchResult cachedResult() {
    TaskResponse cached = taskService.loadFromCache();
    return cached != null && cached.getTasks() != null ? new TaskFetchResult(cached, null) : null;
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
                        // Ignore any parsing errors from error responses
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
            .url(baseUrl().addPathSegment("player").build())
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

    return executeAsync(get("tasks", "hash"))
        .thenCompose(
            bodyString -> {
              String serverHash =
                  gson.fromJson(bodyString, JsonObject.class).get("hash").getAsString();
              if (serverHash.equals(cachedHash)) {
                TaskFetchResult cached = cachedResult();
                if (cached != null) return CompletableFuture.completedFuture(cached);
              }
              return fetchFullTaskList();
            })
        .exceptionally(
            err -> {
              log.warn("Failed to fetch tasks: {}", err.getMessage());
              TaskFetchResult cached = cachedResult();
              if (cached != null) return cached;
              throw err instanceof CompletionException
                  ? (CompletionException) err
                  : new CompletionException(err);
            });
  }

  private CompletableFuture<TaskFetchResult> fetchFullTaskList() {
    return executeAsync(get("tasks"))
        .thenApply(
            bodyString -> {
              Map<Integer, String> oldHashes = new HashMap<>();
              TaskFetchResult oldCached = cachedResult();
              if (oldCached != null) {
                for (Task oldTask : oldCached.getTaskResponse().getTasks()) {
                  oldHashes.put(oldTask.getId(), oldTask.getHash());
                }
              }
              taskService.saveToCache(bodyString);
              TaskResponse taskResponse = gson.fromJson(bodyString, TaskResponse.class);
              return new TaskFetchResult(taskResponse, oldHashes);
            });
  }

  public CompletableFuture<List<Rank>> fetchRanksAsync() {
    return executeAsync(get("ranks"))
        .thenApply(body -> gson.fromJson(body, RankFetchResult.class).getRanks());
  }

  public CompletableFuture<byte[]> fetchRankIcon(String iconFilename) {
    HttpUrl url = HttpUrl.parse(IMAGES_BASE_URL + "ranks/" + iconFilename);
    if (url == null) {
      return CompletableFuture.failedFuture(
          new IOException("Invalid rank icon URL: " + iconFilename));
    }
    Request request = new Request.Builder().get().url(url).build();
    return executeAsync(request, ResponseBody::bytes);
  }

  public CompletableFuture<byte[]> fetchTaskIcon(String iconFilename) {
    HttpUrl url = HttpUrl.parse(IMAGES_BASE_URL + "tasks/" + iconFilename);
    if (url == null) {
      return CompletableFuture.failedFuture(new IOException("Invalid icon URL: " + iconFilename));
    }
    Request request = new Request.Builder().get().url(url).build();
    return executeAsync(request, ResponseBody::bytes);
  }

  @Value
  public static class TaskFetchResult {
    TaskResponse taskResponse;
    Map<Integer, String> previousTaskHashes;
  }

  @Getter
  public static class RankFetchResult {
    private List<Rank> ranks;
  }
}
