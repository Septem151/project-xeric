package io.septem150.xeric.data.task;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;

public interface TaskStore {
  @NonNull CompletableFuture<List<Task>> getAllAsync();
}
