package io.septem150.xeric.task;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class LocalTaskStore implements TaskStore {
  private static final String TASKS_RES_PATH = "io/septem150/xeric/data/rank_tasks.json";

  private final List<Task> tasksList;
  private final Map<String, Task> tasksMap;

  @Inject
  public LocalTaskStore(Gson gson) {
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(TASKS_RES_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", TASKS_RES_PATH));
      }
      TypeToken<List<RankTier>> type = new TypeToken<>() {};
      List<RankTier> rankTiers =
          gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type.getType());
      tasksList =
          rankTiers.stream()
              .flatMap(tier -> tier.getTasks().stream().peek(task -> task.setTier(tier.getTier())))
              .collect(Collectors.toList());
      tasksMap = Maps.uniqueIndex(tasksList, Task::getId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Task> getAll() {
    return tasksList;
  }

  @Override
  public Optional<Task> getById(@NonNull String id) {
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
  public List<Task> getByType(@NonNull String type) {
    return tasksList.stream()
        .filter(task -> task.getType().equalsIgnoreCase(type))
        .collect(Collectors.toList());
  }

  @Override
  public List<Task> getByTier(int tier) {
    return tasksList.stream().filter(task -> task.getTier() == tier).collect(Collectors.toList());
  }
}
