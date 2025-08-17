package io.septem150.xeric.data.hiscore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericPlugin;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class LocalHiscoreStore implements HiscoreStore {
  private static final String HISCORES_RES_PATH = "data/hiscores.json";

  private final Gson gson;

  private @Nullable List<Hiscore> hiscoresList;
  private @Nullable CompletableFuture<List<Hiscore>> future;

  @Inject
  public LocalHiscoreStore(@Named("xericGson") Gson gson) {
    this.gson = gson;
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
        try (InputStream in = ProjectXericPlugin.class.getResourceAsStream(HISCORES_RES_PATH)) {
          if (in == null) {
            throw new FileNotFoundException(
                String.format("Unable to access resource '%s'", HISCORES_RES_PATH));
          }
          Type type = new TypeToken<List<Hiscore>>() {}.getType();
          hiscoresList = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
          future = CompletableFuture.completedFuture(hiscoresList);
        } catch (Exception err) {
          future = CompletableFuture.failedFuture(err);
        }
      }
    } else {
      future = CompletableFuture.completedFuture(hiscoresList);
    }
    return future;
  }

  /**
   * No idea if this works -shrug-
   *
   * @param hiscore player info to send as an updated or new hiscore entry
   * @return the updated or new hiscore entry with the id field populated
   */
  @Override
  public @NonNull CompletableFuture<Hiscore> postAsync(@NonNull Hiscore hiscore) {
    if (hiscoresList == null) {
      // this should never be the case, but should be fine to block while loading
      hiscoresList = getAll();
    }
    boolean updated = false;
    for (int i = 0; i < hiscoresList.size(); i++) {
      if ((hiscore.getId() != null && hiscore.getId().equals(hiscoresList.get(i).getId()))
          || (hiscore.getUsername().equalsIgnoreCase(hiscoresList.get(i).getUsername()))) {
        hiscore.setId(hiscoresList.get(i).getId());
        hiscoresList.set(i, hiscore);
        updated = true;
        break;
      }
    }
    if (!updated) {
      hiscore.setId(hiscoresList.size() + 1);
      hiscoresList.add(hiscore);
      hiscoresList.sort((Comparator.comparing(hs -> Objects.requireNonNull(hs.getId()))));
    }
    return CompletableFuture.completedFuture(hiscore);
  }

  @Override
  public void reset() {
    hiscoresList = null;
  }
}
