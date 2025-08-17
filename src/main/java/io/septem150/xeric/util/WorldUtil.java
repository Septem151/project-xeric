package io.septem150.xeric.util;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.WorldType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorldUtil {
  private static final Set<WorldType> invalidWorldTypes =
      Set.of(
          WorldType.NOSAVE_MODE,
          WorldType.BETA_WORLD,
          WorldType.FRESH_START_WORLD,
          WorldType.DEADMAN,
          WorldType.PVP_ARENA,
          WorldType.QUEST_SPEEDRUNNING,
          WorldType.SEASONAL,
          WorldType.TOURNAMENT_WORLD);

  public static boolean isValidWorldType(Client client) {
    if (!client.isClientThread()) {
      return false;
    }
    return Sets.intersection(invalidWorldTypes, client.getWorldType()).isEmpty();
  }
}
