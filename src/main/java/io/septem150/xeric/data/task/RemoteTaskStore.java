package io.septem150.xeric.data.task;

import io.septem150.xeric.data.ProjectXericApiClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class RemoteTaskStore implements TaskStore {
  private final ProjectXericApiClient apiClient;

  @Inject
  public RemoteTaskStore(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public @NonNull CompletableFuture<List<Task>> getAllAsync() {
    return apiClient.getAllTasksAsync();
  }
}
