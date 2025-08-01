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
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class LocalTaskStore implements TaskStore {
  private static final String TASKS_RES_PATH = "data/rank_tasks.json";

  private final List<Task> tasksList;

  @Inject
  public LocalTaskStore(@Named("xericGson") Gson gson) {
    try (InputStream in = ProjectXericPlugin.class.getResourceAsStream(TASKS_RES_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", TASKS_RES_PATH));
      }
      Type type = new TypeToken<List<Task>>() {}.getType();
      tasksList = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NonNull CompletableFuture<List<Task>> getAllAsync() {
    return CompletableFuture.completedFuture(tasksList);
  }
}
