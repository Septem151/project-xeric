package io.septem150.xeric.data.hiscore;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;

public interface HiscoreStore {
  @NonNull List<Hiscore> getAll();

  @NonNull CompletableFuture<Optional<Hiscore>> getByUsername(String username);

  @NonNull CompletableFuture<List<Hiscore>> getAllAsync();

  @NonNull CompletableFuture<List<Hiscore>> getAllAsync(boolean cached);

  @NonNull CompletableFuture<Hiscore> postAsync(@NonNull Hiscore hiscore);

  void reset();
}
