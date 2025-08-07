package io.septem150.xeric.data.task;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;

public interface TaskStore {
  @NonNull List<Task> getAll();

  @NonNull CompletableFuture<List<Task>> getAllAsync();

  @NonNull CompletableFuture<List<Task>> getAllAsync(boolean cached);

  void reset();
}
