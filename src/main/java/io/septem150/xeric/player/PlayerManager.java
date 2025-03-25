package io.septem150.xeric.player;

import io.septem150.xeric.ClientManager;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerManager {
  private static final List<Quest> whitelistedQuests =
      List.of(
          Quest.DRUIDIC_RITUAL,
          Quest.EAGLES_PEAK,
          Quest.RUNE_MYSTERIES,
          Quest.A_KINGDOM_DIVIDED,
          Quest.GETTING_AHEAD,
          Quest.THE_GARDEN_OF_DEATH,
          Quest.CHILDREN_OF_THE_SUN,
          Quest.TWILIGHTS_PROMISE,
          Quest.THE_HEART_OF_DARKNESS,
          Quest.X_MARKS_THE_SPOT,
          Quest.CLIENT_OF_KOUREND,
          Quest.THE_QUEEN_OF_THIEVES,
          Quest.THE_DEPTHS_OF_DESPAIR,
          Quest.THE_ASCENT_OF_ARCEUUS,
          Quest.THE_FORSAKEN_TOWER,
          Quest.TALE_OF_THE_RIGHTEOUS,
          Quest.PERILOUS_MOONS,
          Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE,
          Quest.AT_FIRST_LIGHT,
          Quest.DEATH_ON_THE_ISLE,
          Quest.MEAT_AND_GREET,
          Quest.ETHICALLY_ACQUIRED_ANTIQUITIES);
  private static final List<Quest> bannedQuests =
      Arrays.stream(Quest.values())
          .filter(quest -> !whitelistedQuests.contains(quest))
          .collect(Collectors.toList());

  private final Client client;
  private final ClientManager clientManager;
  private final ItemManager itemManager;

  @Getter private String username;
  @Getter private boolean herbloreException;
  @Getter private boolean boxtrapException;
  @Getter private boolean slayerException;
  private boolean allowedOnHiscores;

  public boolean loadPlayer() {
    this.username = client.getLocalPlayer().getName();
    if (isLoggedOut()) {
      return false;
    }
    herbloreException = Quest.DRUIDIC_RITUAL.getState(client) == QuestState.FINISHED;
    boxtrapException = Quest.EAGLES_PEAK.getState(client) != QuestState.NOT_STARTED;
    allowedOnHiscores = true;
    for (Quest quest : bannedQuests) {
      QuestState questState = quest.getState(client);
      if (questState != QuestState.NOT_STARTED) {
        allowedOnHiscores = false;
        break;
      }
    }
    Set<Integer> clogItems = clientManager.requestAllClogItems();
    return true;
  }

  public void unloadPlayer() {
    username = null;
    herbloreException = false;
    boxtrapException = false;
    slayerException = false;
    allowedOnHiscores = true;
  }

  public boolean isLoggedOut() {
    return !client.isClientThread() || username == null;
  }
}
