package io.septem150.xeric.data.task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericPlugin;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class LocalTaskStore implements TaskStore {
  private static final String TASKS_RES_PATH = "data/rank_tasks.json";

  private final Gson gson;

  private @Nullable List<Task> tasksList;
  private @Nullable CompletableFuture<List<Task>> future;

  @Inject
  public LocalTaskStore(@Named("xericGson") Gson gson) {
    this.gson = gson;
  }

  @Override
  public @NonNull List<Task> getAll() {
    if (tasksList != null) return tasksList;
    try {
      return getAllAsync().get();
    } catch (InterruptedException | ExecutionException err) {
      throw new RuntimeException(err);
    }
  }

  @Override
  public @NonNull CompletableFuture<List<Task>> getAllAsync() {
    return getAllAsync(true);
  }

  @Override
  public @NonNull CompletableFuture<List<Task>> getAllAsync(boolean cached) {
    if (!cached || tasksList == null) {
      if (future == null || future.isDone()) {
        try (InputStream in = ProjectXericPlugin.class.getResourceAsStream(TASKS_RES_PATH)) {
          if (in == null) {
            throw new FileNotFoundException(
                String.format("Unable to access resource '%s'", TASKS_RES_PATH));
          }
          Type type = new TypeToken<List<Task>>() {}.getType();
          tasksList = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
          future = CompletableFuture.completedFuture(tasksList);
        } catch (Exception err) {
          future = CompletableFuture.failedFuture(err);
        }
      }
    } else {
      future = CompletableFuture.completedFuture(tasksList);
    }
    return future;
  }

  @Override
  public void reset() {
    tasksList = null;
  }
}
