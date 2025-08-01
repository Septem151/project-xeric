package io.septem150.xeric.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.data.hiscore.Hiscore;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLiteProperties;
import okhttp3.*;

@Slf4j
@RequiredArgsConstructor
public class ProjectXericApiClient {
  private static final MediaType JSON_MEDIA_TYPE =
      Objects.requireNonNull(MediaType.parse("application/json; charset=utf-8"));

  @Inject private OkHttpClient okHttpClient;

  private final String userAgent;

  // increment when making breaking changes to how the plugin uses the API
  private static final String version = "1.0.0";

  private final HttpUrl baseUrl;

  private final Gson gson;

  @Inject
  public ProjectXericApiClient(
      @Named("xericGson") Gson gson, @Named("developerMode") boolean developerMode) {
    this.gson = gson;

    String runeliteVersion = RuneLiteProperties.getVersion();
    userAgent = "RuneLite:" + runeliteVersion + "," + "Client:" + version;

    //noinspection ConstantValue
    baseUrl =
        developerMode
            ? new HttpUrl.Builder().scheme("http").host("localhost:8085").build()
            : new HttpUrl.Builder().scheme("https").host("api.projectxeric.com").build();
  }

  private HttpUrl buildApiUrl(String... pathSegments) {
    HttpUrl.Builder urlBuilder = baseUrl.newBuilder();
    for (String segment : pathSegments) {
      urlBuilder.addPathSegment(segment);
    }
    return urlBuilder.build();
  }

  private RequestBody createJsonBody(JsonObject jsonObject) {
    return RequestBody.create(JSON_MEDIA_TYPE, jsonObject.toString());
  }

  private Request.Builder buildApiRequest(HttpUrl url, Consumer<Request.Builder> methodSetter) {
    Request.Builder builder =
        new Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header("User-Agent", userAgent);
    methodSetter.accept(builder);
    return builder;
  }

  private CompletableFuture<Response> executeHttpRequestAsync(
      OkHttpClient client, Request request) {
    CompletableFuture<Response> future = new CompletableFuture<>();
    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(@NonNull Call call, @NonNull IOException e) {
                log.error("Async API request failed.", e);
                future.completeExceptionally(e);
              }

              @Override
              public void onResponse(@NonNull Call call, @NonNull Response response) {
                future.complete(response);
              }
            });
    return future;
  }

  private CompletableFuture<Response> postHttpRequestAsync(HttpUrl url, String data) {
    RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, data);
    Request request = buildApiRequest(url, builder -> builder.post(body)).build();
    log.debug("Sending json request to = {}, data = {}", url.toString(), data);
    return executeHttpRequestAsync(okHttpClient, request);
  }

  private CompletableFuture<Response> postHttpRequestAsync(HttpUrl url, MultipartBody data) {
    Request request = buildApiRequest(url, builder -> builder.post(data)).build();
    log.debug("Sending form data request to = {}, data = {}", url.toString(), data);
    return executeHttpRequestAsync(okHttpClient, request);
  }

  private CompletableFuture<Response> getHttpRequestAsync(HttpUrl url) {
    Request request = buildApiRequest(url, Request.Builder::get).build();
    return executeHttpRequestAsync(okHttpClient, request);
  }

  private CompletableFuture<Response> deleteHttpRequestAsync(HttpUrl url) {
    Request request = buildApiRequest(url, Request.Builder::delete).build();
    return executeHttpRequestAsync(okHttpClient, request);
  }

  private <T> T handleResponse(Response response, @Nullable Type type) {
    try (Response res = response) {
      ResponseBody body = res.body();

      if (body == null) {
        throw new RuntimeException("Response body is null");
      }

      String bodyString = body.string();

      if (!response.isSuccessful()) {
        JsonObject json = gson.fromJson(bodyString, JsonObject.class);
        log.error(bodyString);
        throw new RuntimeException(json.get("message").getAsString());
      }

      if (type == null) {
        return null;
      }

      return gson.fromJson(bodyString, type);
    } catch (IOException e) {
      throw new RuntimeException("Error reading response body");
    }
  }

  public @NonNull CompletableFuture<List<Hiscore>> getAllHiscoresAsync() {
    HttpUrl url = buildApiUrl("players");
    return getHttpRequestAsync(url)
        .thenApplyAsync(
            response -> handleResponse(response, new TypeToken<List<Hiscore>>() {}.getType()));
  }
}
