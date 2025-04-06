package io.septem150.xeric;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import io.septem150.xeric.data.AccountType;
import io.septem150.xeric.data.ClanRank;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.SpriteManager;

@Slf4j
@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @__(@Inject))
public final class ProjectXericManager {
  private static final String RSPROFILE_DATA_KEY = "data";

  private static final int EASY_TIER_ENUM_ID = 3981;
  private static final int MEDIUM_TIER_ENUM_ID = 3982;
  private static final int HARD_TIER_ENUM_ID = 3983;
  private static final int ELITE_TIER_ENUM_ID = 3984;
  private static final int MASTER_TIER_ENUM_ID = 3985;
  private static final int GM_TIER_ENUM_ID = 3986;
  private static final int CA_STRUCT_ID_PARAM_ID = 1306;
  private static final int[] SCRIPT_4834_VARP_IDS =
      new int[] {
        3116, 3117, 3118, 3119, 3120, 3121, 3122, 3123, 3124, 3125, 3126, 3127, 3128, 3387, 3718,
        3773, 3774, 4204, 4496
      };
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

  @Named("xericGson")
  private final Gson gson;

  private final PlayerInfo playerInfo;
  private final ProjectXericConfig config;
  private final ConfigManager configManager;
  private final SpriteManager spriteManager;
  private final Client client;
  private final ClientThread clientThread;
  private final ScheduledExecutorService executor;
  private final EventBus eventBus;
  private final TaskStore taskStore;
  private final Map<AccountType, BufferedImage> accountTypeImages = new HashMap<>();
  private final Map<Integer, Task> tasks = new HashMap<>();

  private boolean initialized;
  private BufferedImage accountTypeImage;

  public void init() {
    if (initialized) return;
    initialized = true;
    eventBus.register(this);
    clientThread.invokeLater(
        () -> {
          for (AccountType accountType : AccountType.values()) {
            accountTypeImages.put(accountType, accountType.getImage(spriteManager));
          }
          executor.execute(
              () -> {
                tasks.clear();
                List<Task> allTasks = taskStore.getAll();
                for (Task task : allTasks) {
                  tasks.put(task.getId(), task);
                }
              });
        });
  }

  public @NonNull BufferedImage getAccountTypeImage() {
    return Objects.requireNonNull(accountTypeImage);
  }

  public @Nullable String getUsername() {
    return playerInfo.getUsername();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(getPoints());
  }

  public int getPoints() {
    int totalPoints = 0;
    for (Task task : playerInfo.getTasks()) {
      totalPoints += task.getTier();
    }
    return totalPoints;
  }

  public boolean isStoringClogData() {
    return playerInfo.getCollectionLog().getLastOpened() != null;
  }

  public void clearPlayer() {
    playerInfo.clear();
    accountTypeImage = null;
  }

  public void saveRSProfile() {
    if (getUsername() == null) {
      return;
    }
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, gson.toJson(playerInfo));
  }

  public void clearRSProfile() {
    configManager.unsetRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY);
  }

  public int getTasksCompleted() {
    return playerInfo.getTasks().size();
  }

  public int getPointsToNextRank() {
    return getRank().getNextRank().getPointsNeeded() - getPoints();
  }

  public @NonNull List<Integer> getAllTiers() {
    return tasks.values().stream()
        .map(Task::getTier)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public String getHighestTierCompleted() {
    int highestTier = 0;
    int maxTiers = Optional.ofNullable(Iterables.getLast(getAllTiers(), null)).orElse(0);
    for (int tier = 1; tier <= maxTiers; tier++) {
      if (playerInfo.getTasks().isEmpty()) break;
      boolean completed = true;
      for (Task task : tasks.values()) {
        if (!playerInfo.getTasks().contains(task)) {
          completed = false;
          break;
        }
      }
      if (completed) {
        highestTier = tier;
      } else break;
    }
    return highestTier > 0 ? String.format("Tier %d", highestTier) : "None";
  }

  public boolean isHerbloreUnlocked() {
    return playerInfo.getQuests().stream()
        .anyMatch(
            questProgress ->
                questProgress.getQuest() == Quest.DRUIDIC_RITUAL
                    && questProgress.getState() == QuestState.FINISHED);
  }

  public boolean isBoxTrapUnlocked() {
    return playerInfo.getQuests().stream()
        .anyMatch(
            questProgress ->
                questProgress.getQuest() == Quest.EAGLES_PEAK
                    && questProgress.getState() != QuestState.NOT_STARTED);
  }

  public boolean isOffIslandSlayerUnlocked() {
    return config.slayer();
  }

  public List<Task> getAllTasks() {
    return new ArrayList<>(tasks.values());
  }

  public boolean isTaskCompleted(Task task) {
    return playerInfo.getTasks().contains(task);
  }

  //  @Subscribe
  //  public void onGameStateChanged(GameStateChanged event) {
  //    switch (event.getGameState()) {
  //      case LOGGED_IN:
  //        clientThread.invokeLater(this::handleLogin);
  //        break;
  //      case LOGIN_SCREEN:
  //        clientThread.invokeLater(this::handleLogout);
  //        break;
  //      default:
  //    }
  //  }

  private void handleLogout() {
    if (!initialized || getUsername() == null || !isStoringClogData()) {
      return;
    }
    //    final String json = gson.toJson(new ClogData(playerData.getClogItems()));
    final String json = gson.toJson(playerInfo);
    configManager.setRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, json);
  }

  private void checkForTaskCompletions() {
    boolean newTasks = false;
    for (Task task : tasks.values()) {
      if (playerInfo.getTasks().contains(task)) continue;
      if (task.checkCompletion(playerInfo)) {
        playerInfo.getTasks().add(task);
        newTasks = true;
      }
    }
    if (newTasks) {
      //      eventBus.post(new PanelUpdate());
    }
  }

  //  private boolean handleLogin() {
  //    if (!initialized
  //        || client.getLocalPlayer() == null
  //        || client.getLocalPlayer().getName() == null) {
  //      return false;
  //    }
  //    clearPlayer();
  //    // load local saved player data
  //    // check if data is stale and update from client if necessary
  //    try {
  //      String json =
  //          configManager.getRSProfileConfiguration(
  //              ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, String.class);
  //      if (json != null) {
  //        playerInfo = gson.fromJson(json, PlayerInfo.class);
  //      }
  //    } catch (JsonSyntaxException ex) {
  //      log.warn("Malformed saved player data, removing");
  //      configManager.unsetRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY);
  //    }
  //    // Set account type and username
  //    playerInfo.setUsername(client.getLocalPlayer().getName());
  //    accountTypeImage =
  //        accountTypeImages.get(
  //            AccountType.fromVarbValue(client.getVarbitValue(Varbits.ACCOUNT_TYPE)));
  //    // Set quests
  //    for (Quest quest : Quest.values()) {
  //      QuestState questState = quest.getState(client);
  //      if (questState == QuestState.NOT_STARTED) {
  //        continue;
  //      }
  //      playerInfo.getQuests().put(String.valueOf(quest.getId()), questState.ordinal());
  //    }
  //    // Set diaries
  //    playerInfo.setDiaries(
  //        KourendDiary.allDiaries.stream()
  //            .collect(
  //                Collectors.toMap(KourendDiary::toString, diary -> diary.getTaskCount(client))));
  //    // player stats don't load for a while, invoke later
  //    clientThread.invokeLater(
  //        () -> {
  //          // Set stats
  //          Map<String, Integer> levels =
  //              Arrays.stream(Skill.values())
  //                  .collect(Collectors.toMap(Skill::getName, client::getRealSkillLevel));
  //          if (levels.containsValue(0)) {
  //            return false;
  //          }
  //          playerInfo.setLevels(levels);
  //          return true;
  //        });
  //    // Set CA's
  //    playerInfo.setCaTasks(getCATaskCompletions());
  //    // Clogs get populated from RSProfile OR when player opens Clog
  //    // Check for any new task completions
  //    checkForTaskCompletions();
  //    eventBus.post(new PlayerInfoUpdated());
  //    return true;
  //  }
}
