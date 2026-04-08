package io.septem150.xeric.data.task;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

@Slf4j
@Singleton
public class TaskService {
  private static final File CACHE_DIR = new File(RuneLite.RUNELITE_DIR, "project-xeric");
  private static final File CACHE_FILE = new File(CACHE_DIR, "tasks.json");

  private final Gson gson;

  @Inject
  public TaskService(@Named("xericGson") Gson gson) {
    this.gson = gson;
  }

  @Nullable private String readCacheFile() {
    if (!CACHE_FILE.exists()) return null;
    try {
      return Files.readString(CACHE_FILE.toPath());
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
      CACHE_DIR.mkdirs();
      File tempFile = new File(CACHE_DIR, "tasks.json.tmp");
      Files.write(tempFile.toPath(), body.getBytes(StandardCharsets.UTF_8));
      Files.move(tempFile.toPath(), CACHE_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.warn("Failed to save tasks to cache", e);
    }
  }

  public void deleteCache() {
    try {
      Files.deleteIfExists(CACHE_FILE.toPath());
    } catch (IOException e) {
      log.warn("Failed to delete task cache", e);
    }
  }
}
