package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.AchievementDiary;
import io.septem150.xeric.data.player.ClanRank;
import io.septem150.xeric.data.player.PlayerData;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.event.PanelRefreshRequest;
import io.septem150.xeric.event.TaskCompletedEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import net.runelite.api.EnumComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
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

  private static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
  private static final int COLLECTION_LOG_TRANSMIT_SCRIPT_ID = 4100;

  @Named("xericGson")
  private final Gson gson;

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
  private @NonNull PlayerData playerData = new PlayerData();
  private BufferedImage accountTypeImage;
  private int tickCollectionLogOpened = -1;

  public void init() {
    if (initialized) return;
    eventBus.register(this);
    clientThread.invokeLater(
        () -> {
          for (AccountType accountType : AccountType.values()) {
            accountTypeImages.put(accountType, accountType.getImage(client, spriteManager));
          }
          executor.execute(
              () -> {
                tasks.clear();
                List<Task> allTasks = taskStore.getAll();
                for (Task task : allTasks) {
                  tasks.put(task.getId(), task);
                }
                initialized = true;
              });
        });
  }

  public @NonNull BufferedImage getAccountTypeImage() {
    return Objects.requireNonNull(accountTypeImage);
  }

  public @Nullable String getUsername() {
    return playerData.getUsername();
  }

  public ClanRank getRank() {
    return ClanRank.fromPoints(getPoints());
  }

  public int getPoints() {
    int totalPoints = 0;
    for (int taskId : playerData.getTasks()) {
      Task task = tasks.get(taskId);
      if (task == null) continue;
      totalPoints += task.getTier();
    }
    return totalPoints;
  }

  public boolean isStoringClogData() {
    return playerData.isStoringClogData();
  }

  public void clearPlayer() {
    playerData.clear();
    accountTypeImage = null;
  }

  public void saveRSProfile() {
    if (getUsername() == null) {
      return;
    }
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, gson.toJson(playerData));
  }

  public void clearRSProfile() {
    configManager.unsetRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY);
  }

  public int getTasksCompleted() {
    return playerData.getTasks().size();
  }

  public int getPointsToNextRank() {
    return getRank().getNextRank().getPointsNeeded() - getPoints();
  }

  public String getHighestTierCompleted() {
    int highestTier = 0;
    int maxTiers =
        tasks.values().stream().map(Task::getTier).mapToInt(Integer::intValue).max().orElse(0);
    for (int tier = 1; tier <= maxTiers; tier++) {
      if (playerData.getTasks().isEmpty()) break;
      boolean completed = true;
      for (Task task : tasks.values()) {
        if (!playerData.getTasks().contains(task.getId())) {
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
    return playerData.getQuests().getOrDefault(String.valueOf(Quest.DRUIDIC_RITUAL.getId()), 0)
        == 2;
  }

  public boolean isBoxTrapUnlocked() {
    return playerData.getQuests().getOrDefault(String.valueOf(Quest.EAGLES_PEAK.getId()), 0) > 0;
  }

  public boolean isOffIslandSlayerUnlocked() {
    return config.slayer();
  }

  public List<Task> getAllTasks() {
    return new ArrayList<>(tasks.values());
  }

  public boolean isTaskCompleted(Task task) {
    return playerData.getTasks().contains(task.getId());
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals("slayer")) {
      // do some points recalculating here eventually?
      eventBus.post(new PanelRefreshRequest());
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (tickCollectionLogOpened == -1) {
      return;
    }
    if (tickCollectionLogOpened + 2 <= client.getTickCount()) {
      tickCollectionLogOpened = -1;
      checkForTaskCompletions();
      eventBus.post(new PanelRefreshRequest());
    }
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    switch (event.getGameState()) {
      case LOGGED_IN:
        clientThread.invokeLater(this::handleLogin);
        break;
      case LOGIN_SCREEN:
        saveRSProfile();
        break;
      default:
    }
  }

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() == COLLECTION_LOG_SETUP_SCRIPT_ID && !playerData.isStoringClogData()) {
      playerData.setStoringClogData(true);
      tickCollectionLogOpened = client.getTickCount();
      // taken from WikiSync, not really sure what script is being run,
      // but it appears that simulating a click on the Search button
      // loads the script that checks for clog items
      client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
      client.runScript(2240);
    }
  }

  @Subscribe
  public void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() == COLLECTION_LOG_TRANSMIT_SCRIPT_ID) {
      int itemId = (int) event.getScriptEvent().getArguments()[1];
      List<Integer> clogItems = playerData.getClogItems();
      if (!clogItems.contains(itemId)) {
        clogItems.add(itemId);
        checkForTaskCompletions();
      }
    }
  }

  private void checkForTaskCompletions() {
    for (Task task : tasks.values()) {
      if (playerData.getTasks().contains(task.getId())) continue;
      if (task.checkCompletion(playerData)) {
        playerData.getTasks().add(task.getId());
        eventBus.post(new TaskCompletedEvent(task, false));
      }
    }
  }

  private boolean handleLogin() {
    if (!initialized
        || client.getLocalPlayer() == null
        || client.getLocalPlayer().getName() == null) {
      return false;
    }
    clearPlayer();
    // load local saved player data
    // check if data is stale and update from client if necessary
    try {
      String json =
          configManager.getRSProfileConfiguration(
              ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, String.class);
      if (json != null) {
        playerData = gson.fromJson(json, PlayerData.class);
      }
    } catch (JsonSyntaxException ex) {
      log.warn("Malformed saved player data, removing");
      configManager.unsetRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY);
    }
    // Set account type and username
    playerData.setUsername(client.getLocalPlayer().getName());
    accountTypeImage =
        accountTypeImages.get(
            AccountType.fromVarbValue(client.getVarbitValue(Varbits.ACCOUNT_TYPE)));
    // Set quests
    for (Quest quest : Quest.values()) {
      QuestState questState = quest.getState(client);
      if (questState == QuestState.NOT_STARTED) {
        continue;
      }
      playerData.getQuests().put(String.valueOf(quest.getId()), questState.ordinal());
    }
    // Set diaries
    playerData.setDiaries(
        Arrays.stream(AchievementDiary.values())
            .collect(
                Collectors.toMap(
                    AchievementDiary::toString,
                    achievementDiary -> client.getVarbitValue(achievementDiary.getVarbit()))));
    // player stats don't load for a while, invoke later
    clientThread.invokeLater(
        () -> {
          // Set stats
          Map<String, Integer> levels =
              Arrays.stream(Skill.values())
                  .collect(Collectors.toMap(Skill::getName, client::getRealSkillLevel));
          if (levels.containsValue(0)) {
            return false;
          }
          playerData.setLevels(levels);
          return true;
        });
    // Set CA's
    playerData.setCaTasks(getCATaskCompletions());
    // Clogs get populated from RSProfile OR when player opens Clog
    // Check for any new task completions
    checkForTaskCompletions();
    eventBus.post(new PanelRefreshRequest());
    return true;
  }

  /**
   * Gets all player's completed CA Tasks and maps them to their point value.
   *
   * @return list of CA Task ID to the task's point value.
   * @see <a
   *     href="https://discord.com/channels/301497432909414422/419891709883973642/1347233676945260684">RuneLite
   *     Discord post</a> by @abex
   */
  private List<Integer> getCATaskCompletions() {
    List<Integer> caTaskIds = new ArrayList<>();
    // from [proc,ca_tasks_total]
    // there is an enum per ca tier
    for (int caTiersEnumId :
        new int[] {
          EASY_TIER_ENUM_ID,
          MEDIUM_TIER_ENUM_ID,
          HARD_TIER_ENUM_ID,
          ELITE_TIER_ENUM_ID,
          MASTER_TIER_ENUM_ID,
          GM_TIER_ENUM_ID
        }) {
      EnumComposition caTiersEnum = client.getEnum(caTiersEnumId);
      // so we can iterate the enum to find a bunch of structs
      for (int caTierStructId : caTiersEnum.getIntVals()) {
        StructComposition caTierStruct = client.getStructComposition(caTierStructId);
        // and with the struct we can get info about the ca
        // like its id, which we can use to get if its completed or not
        int taskId = caTierStruct.getIntValue(CA_STRUCT_ID_PARAM_ID);
        // we can use the cs2 vm to invoke script 4834 to do the lookup for us
        // client.runScript(4834, id);
        // boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;

        // or we can reimplement it ourselves
        // from script 4834
        boolean unlocked =
            (client.getVarpValue(SCRIPT_4834_VARP_IDS[taskId / 32]) & (1 << (taskId % 32))) != 0;

        if (unlocked) {
          caTaskIds.add(taskId);
        }
      }
    }
    return caTaskIds;
  }
}
