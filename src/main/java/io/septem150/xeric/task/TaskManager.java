package io.septem150.xeric.task;

import static io.septem150.xeric.ProjectXericPlugin.GSON;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.Getter;

@Singleton
@Getter
public class TaskManager {
  private static final String TASK_LIST_FILE = "data/tasks.json";
  private final List<Task> tasks = new ArrayList<>();

  public void init() {
    tasks.clear();
    loadResource();
  }

  private void loadResource() {
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(TASK_LIST_FILE)) {
      final Type type = new TypeToken<List<Task>>() {}.getType();
      List<Task> loadedTasks =
          GSON.fromJson(
              new InputStreamReader(Objects.requireNonNull(in), StandardCharsets.UTF_8), type);
      tasks.addAll(loadedTasks);
    } catch (IOException | NullPointerException | JsonIOException | JsonSyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get all tasks for a specific {@link TaskTier}.
   *
   * @param tier the tier to filter tasks by
   * @return a list of tasks of the specified tier
   */
  public List<Task> getTasks(TaskTier tier) {
    return tasks.stream().filter(task -> task.getTier() == tier).collect(Collectors.toList());
  }

  public List<Task> getTasks(TaskType type) {
    return tasks.stream().filter(task -> task.getType() == type).collect(Collectors.toList());
  }

  public List<Task> getTasks(TaskTier tier, TaskType type) {
    return tasks.stream()
        .filter(task -> task.getTier() == tier && task.getType() == type)
        .collect(Collectors.toList());
  }
}
