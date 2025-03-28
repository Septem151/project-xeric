package io.septem150.xeric.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericPlugin;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class LocalTaskStore implements TaskStore {
  private static final String TASKS_RES_PATH = "data/rank_tasks.json";

  private final List<Task> tasksList;
  private final Map<Integer, Task> tasksMap;

  @Inject
  public LocalTaskStore(@Named("xericGson") Gson gson) {
    try (InputStream in = ProjectXericPlugin.class.getResourceAsStream(TASKS_RES_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", TASKS_RES_PATH));
      }
      Type type = new TypeToken<List<Task>>() {}.getType();
      tasksList = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
      tasksMap = Maps.uniqueIndex(tasksList, Task::getId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NonNull List<Task> getAll() {
    return tasksList;
  }

  @Override
  public Optional<Task> getById(int id) {
    return Optional.ofNullable(tasksMap.get(id));
  }

  @Override
  public Optional<Task> getByName(@NonNull String name) {
    return tasksList.stream().filter(task -> task.getName().equalsIgnoreCase(name)).findFirst();
  }

  @Override
  public Optional<Task> getByName(@NonNull Pattern pattern) {
    return tasksList.stream().filter(task -> pattern.matcher(task.getName()).matches()).findFirst();
  }

  @Override
  public @NonNull List<Task> getByType(@NonNull String type) {
    return tasksList.stream()
        .filter(task -> task.getType().equalsIgnoreCase(type))
        .collect(Collectors.toList());
  }

  @Override
  public @NonNull List<Task> getByTier(int tier) {
    return tasksList.stream().filter(task -> task.getTier() == tier).collect(Collectors.toList());
  }
}
