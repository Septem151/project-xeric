package io.septem150.xeric.data.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.ProjectXericApiClient.TaskResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class TaskService {
  private static final Path TASKS_CACHE_DIR = ProjectXericConfig.CACHE_DIR.resolve("tasks.json");

  private final Gson gson;

  @Inject
  public TaskService(@Named("xericGson") Gson gson) {
    this.gson = gson;
  }

  @Nullable private String readCacheFile() {
    if (!Files.exists(TASKS_CACHE_DIR)) return null;
    try {
      return Files.readString(TASKS_CACHE_DIR);
    } catch (IOException e) {
      log.warn("Failed to read task cache", e);
      return null;
    }
  }

  @Nullable public String getCachedHash() {
    String content = readCacheFile();
    if (content == null) return null;
    try {
      JsonObject obj = gson.fromJson(content, JsonObject.class);
      return obj.has("hash") ? obj.get("hash").getAsString() : null;
    } catch (Exception e) {
      log.warn("Failed to parse cached task hash", e);
      return null;
    }
  }

  @Nullable public TaskResponse loadFromCache() {
    String content = readCacheFile();
    if (content == null) return null;
    try {
      return gson.fromJson(content, TaskResponse.class);
    } catch (Exception e) {
      log.warn("Failed to parse tasks from cache", e);
      return null;
    }
  }

  public void saveToCache(String body) {
    try {
      Files.createDirectories(ProjectXericConfig.CACHE_DIR);
      Path tempPath = ProjectXericConfig.CACHE_DIR.resolve("tasks.json.tmp");
      Files.write(tempPath, body.getBytes(StandardCharsets.UTF_8));
      Files.move(tempPath, TASKS_CACHE_DIR, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.warn("Failed to save tasks to cache", e);
    }
  }
}
