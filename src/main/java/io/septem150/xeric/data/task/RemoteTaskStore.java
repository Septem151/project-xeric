package io.septem150.xeric.data.task;

import io.septem150.xeric.data.ProjectXericApiClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class RemoteTaskStore implements TaskStore {
  private final ProjectXericApiClient apiClient;
  private @Nullable List<Task> tasksList;
  private @Nullable CompletableFuture<List<Task>> future;

  @Inject
  public RemoteTaskStore(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public @NonNull List<Task> getAll() {
    if (tasksList != null) {
      return tasksList;
    }
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
        future =
            apiClient
                .getAllTasksAsync()
                .thenApply(
                    tasks -> {
                      tasksList = tasks;
                      return tasks;
                    });
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
