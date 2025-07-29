package io.septem150.xeric.data.hiscore;

import io.septem150.xeric.data.ProjectXericApiClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class RemoteHiscoreStore implements HiscoreStore {
  private final ProjectXericApiClient apiClient;

  private final List<Hiscore> hiscoresList;
  private final Map<Integer, Hiscore> hiscoresMap;

  private Instant lastRefresh;

  @Inject
  public RemoteHiscoreStore(ProjectXericApiClient apiClient) {
    this.apiClient = apiClient;
    hiscoresList = new ArrayList<>();
    hiscoresMap = new HashMap<>();
  }

  @Override
  public @NonNull List<Hiscore> getAll() {
    if (lastRefresh == null || lastRefresh.isBefore(Instant.now().minusSeconds(30))) {
      hiscoresList.clear();
      hiscoresMap.clear();
      try {
        apiClient
            .getAllHiscoresAsync()
            .get()
            .forEach(
                hiscore -> {
                  hiscoresList.add(hiscore);
                  hiscoresMap.put(hiscore.getId(), hiscore);
                });
        lastRefresh = Instant.now();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    return hiscoresList;
  }
}
