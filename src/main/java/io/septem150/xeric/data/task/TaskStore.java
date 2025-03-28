package io.septem150.xeric.data.task;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.NonNull;

public interface TaskStore {
  @NonNull List<Task> getAll();

  Optional<Task> getById(int id);

  Optional<Task> getByName(@NonNull String name);

  Optional<Task> getByName(@NonNull Pattern pattern);

  @NonNull List<Task> getByType(@NonNull String type);

  @NonNull List<Task> getByTier(int tier);
}
