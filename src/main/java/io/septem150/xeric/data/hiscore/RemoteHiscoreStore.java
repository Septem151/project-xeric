package io.septem150.xeric.data.hiscore;

import io.septem150.xeric.data.ProjectXericApiClient;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public final class RemoteHiscoreStore implements HiscoreStore {
  private final ProjectXericApiClient apiClient;
  private @Nullable List<Hiscore> hiscoresList;
  private @Nullable CompletableFuture<List<Hiscore>> future;

  @Inject
  public RemoteHiscoreStore(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public @NonNull List<Hiscore> getAll() {
    if (hiscoresList != null) {
      return hiscoresList;
    }
    try {
      return getAllAsync().get();
    } catch (InterruptedException | ExecutionException err) {
      throw new RuntimeException(err);
    }
  }

  @Override
  public @NonNull CompletableFuture<Optional<Hiscore>> getByUsername(String username) {
    return getAllAsync()
        .thenApply(
            hiscores ->
                hiscores.stream()
                    .filter(hiscore -> hiscore.getUsername().equalsIgnoreCase(username))
                    .findFirst());
  }

  @Override
  public @NonNull CompletableFuture<List<Hiscore>> getAllAsync() {
    return getAllAsync(true);
  }

  @Override
  public @NonNull CompletableFuture<List<Hiscore>> getAllAsync(boolean cached) {
    if (!cached || hiscoresList == null) {
      if (future == null || future.isDone()) {
        future =
            apiClient
                .getAllHiscoresAsync()
                .thenApply(
                    hiscores -> {
                      hiscoresList = hiscores;
                      return hiscores;
                    });
      }
    } else {
      future = CompletableFuture.completedFuture(hiscoresList);
    }
    return future;
  }

  @Override
  public @NonNull CompletableFuture<Hiscore> postAsync(@NonNull Hiscore hiscore) {
    return apiClient
        .postHiscoreAsync(hiscore)
        .thenApply(
            id -> {
              hiscore.setId(id);
              return hiscore;
            });
  }

  @Override
  public void reset() {
    hiscoresList = null;
  }
}
