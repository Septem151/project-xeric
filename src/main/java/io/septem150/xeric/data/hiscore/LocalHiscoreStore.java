package io.septem150.xeric.data.hiscore;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericPlugin;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;

@Singleton
public final class LocalHiscoreStore implements HiscoreStore {
  private static final String HISCORES_RES_PATH = "data/hiscores.json";

  private final List<Hiscore> hiscoresList;
  private final Map<Integer, Hiscore> hiscoresMap;

  @Inject
  public LocalHiscoreStore(@Named("xericGson") Gson gson) {
    try (InputStream in = ProjectXericPlugin.class.getResourceAsStream(HISCORES_RES_PATH)) {
      if (in == null) {
        throw new FileNotFoundException(
            String.format("Unable to access resource '%s'", HISCORES_RES_PATH));
      }
      Type type = new TypeToken<List<Hiscore>>() {}.getType();
      hiscoresList = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
      hiscoresMap = Maps.uniqueIndex(hiscoresList, Hiscore::getId);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public @NonNull List<Hiscore> getAll() {
    return hiscoresList;
  }
  //
  //  @Override
  //  public Optional<Hiscore> getById(int id) {
  //    return Optional.ofNullable(hiscoresMap.get(id));
  //  }
  //
  //  @Override
  //  public Optional<Hiscore> getByUsername(@NonNull String username) {
  //    return hiscoresList.stream()
  //        .filter(hiscore -> hiscore.getUsername().equalsIgnoreCase(username))
  //        .findFirst();
  //  }
  //
  //  @Override
  //  public Optional<Hiscore> getByUsername(@NonNull Pattern pattern) {
  //    return hiscoresList.stream()
  //        .filter(hiscore -> pattern.matcher(hiscore.getUsername()).matches())
  //        .findFirst();
  //  }
}
