package io.septem150.xeric.task;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.NonNull;

public interface TaskStore {
  List<Task> getAll();

  Optional<Task> getById(String id);

  Optional<Task> getByName(@NonNull String name);

  Optional<Task> getByName(@NonNull Pattern pattern);

  List<Task> getByType(@NonNull String type);

  List<Task> getByTier(int tier);
}
