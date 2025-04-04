package io.septem150.xeric.data;

import static io.septem150.xeric.data.CollectionLog.COLLECTION_LOG_SETUP_SCRIPT_ID;
import static io.septem150.xeric.data.CollectionLog.COLLECTION_LOG_TRANSMIT_SCRIPT_ID;
import static io.septem150.xeric.data.CombatAchievement.CA_STRUCT_ID_PARAM_ID;
import static io.septem150.xeric.data.CombatAchievement.CA_STRUCT_NAME_PARAM_ID;
import static io.septem150.xeric.data.CombatAchievement.CA_STRUCT_TIER_PARAM_ID;
import static io.septem150.xeric.data.CombatAchievement.EASY_TIER_ENUM_ID;
import static io.septem150.xeric.data.CombatAchievement.ELITE_TIER_ENUM_ID;
import static io.septem150.xeric.data.CombatAchievement.GM_TIER_ENUM_ID;
import static io.septem150.xeric.data.CombatAchievement.HARD_TIER_ENUM_ID;
import static io.septem150.xeric.data.CombatAchievement.MASTER_TIER_ENUM_ID;
import static io.septem150.xeric.data.CombatAchievement.MEDIUM_TIER_ENUM_ID;
import static io.septem150.xeric.data.CombatAchievement.SCRIPT_4834_VARP_IDS;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.event.PlayerInfoUpdated;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreResult;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
@Slf4j
public class SessionManager {
  private final PlayerInfo playerInfo;
  private final ProjectXericConfig config;
  private final TaskStore taskStore;
  private final Client client;
  private final ClientThread clientThread;
  private final ConfigManager configManager;
  private final @Named("xericGson") Gson gson;
  private final EventBus eventBus;
  private final ScheduledExecutorService executor;
  private final HiscoreClient hiscoreClient;

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

  private boolean handlingLogin;
  private boolean generalLoaded;
  private boolean levelsLoaded;
  private boolean killCountsLoaded;
  private int tickCollectionLogOpened = -1;

  public void init() {
    eventBus.register(this);
    reset();
    if (client.getGameState() == GameState.LOGGED_IN && isValidWorldType()) {
      clientThread.invokeLater(this::handleLogin);
    }
  }

  public void reset() {
    handlingLogin = false;
    generalLoaded = false;
    levelsLoaded = false;
    killCountsLoaded = false;
    playerInfo.clear();
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (!isValidWorldType()) {
      return;
    }
    switch (event.getGameState()) {
      case LOGGED_IN:
        if (!handlingLogin) {
          clientThread.invokeLater(this::handleLogin);
        }
        break;
      case LOGIN_SCREEN:
        clientThread.invokeLater(this::handleLogout);
        break;
      default:
    }
  }

  @Subscribe
  public void onRuneScapeProfileChanged(RuneScapeProfileChanged event) {
    if (event.getPreviousProfile() == null) return;
    if (!event.getNewProfile().equals(event.getPreviousProfile())) {
      if (!isValidWorldType()) {
        return;
      }
      reset();
      clientThread.invokeLater(this::handleLogin);
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.SLAYER)) {
      playerInfo.setSlayerException(true);
      eventBus.post(new PlayerInfoUpdated());
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (tickCollectionLogOpened == -1) {
      return;
    }
    if (tickCollectionLogOpened + 2 <= client.getTickCount()) {
      tickCollectionLogOpened = -1;
      List<Task> tasks =
          taskStore.getAll().stream()
              .filter(task -> task.checkCompletion(playerInfo))
              .collect(Collectors.toList());
      if (playerInfo.getTasks().size() != tasks.size()) {
        playerInfo.setTasks(tasks);
      }
      eventBus.post(new PlayerInfoUpdated());
    }
  }

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() == COLLECTION_LOG_SETUP_SCRIPT_ID
        && (playerInfo.getCollectionLog().getLastOpened() == null
            || playerInfo
                .getCollectionLog()
                .getLastOpened()
                .isBefore(Instant.now().minus(Duration.ofDays(7))))) {
      playerInfo.getCollectionLog().setLastOpened(Instant.now());
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
      CollectionLog collectionLog = playerInfo.getCollectionLog();
      if (collectionLog.getItems().stream().noneMatch(item -> item.getId() == itemId)) {
        ClogItem clogItem = ClogItem.from(client, itemId);
        collectionLog.getItems().add(clogItem);
      }
    }
  }

  private boolean handleLogin() {
    handlingLogin = true;
    if (!client.isClientThread()
        || client.getLocalPlayer() == null
        || client.getLocalPlayer().getName() == null) return false;

    clientThread.invokeLater(this::queryGeneral);
    clientThread.invokeLater(this::queryLevels);
    clientThread.invokeLater(this::queryKillCounts);
    clientThread.invokeLater(this::waitForLoaded);
    return true;
  }

  private void handleLogout() {
    StoredInfo storedInfo = new StoredInfo();
    storedInfo.setSlayerException(playerInfo.isSlayerException());
    storedInfo.setCollectionLog(playerInfo.getCollectionLog());
    storedInfo.setTasks(playerInfo.getTasks());
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY, gson.toJson(storedInfo));
  }

  private void queryGeneral() {
    playerInfo.setAccountType(
        AccountType.fromVarbValue(client.getVarbitValue(Varbits.ACCOUNT_TYPE)));
    playerInfo.setDiaries(
        DiaryProgress.trackedDiaries.stream()
            .map(diary -> DiaryProgress.from(client, diary))
            .collect(Collectors.toList()));
    playerInfo.setQuests(
        QuestProgress.trackedQuests.stream()
            .map(quest -> QuestProgress.from(client, quest))
            .collect(Collectors.toList()));
    playerInfo.setUsername(client.getLocalPlayer().getName());
    List<CombatAchievement> combatAchievements = new ArrayList<>();
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
        String taskName = caTierStruct.getStringValue(CA_STRUCT_NAME_PARAM_ID);
        int taskTier = caTierStruct.getIntValue(CA_STRUCT_TIER_PARAM_ID);
        // we can use the cs2 vm to invoke script 4834 to do the lookup for us
        // client.runScript(4834, id);
        // boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;

        // or we can reimplement it ourselves
        // from script 4834
        boolean unlocked =
            (client.getVarpValue(SCRIPT_4834_VARP_IDS[taskId / 32]) & (1 << (taskId % 32))) != 0;

        if (unlocked) {
          CombatAchievement combatAchievement = new CombatAchievement();
          combatAchievement.setId(taskId);
          combatAchievement.setName(taskName);
          combatAchievement.setPoints(taskTier);
          combatAchievements.add(combatAchievement);
        }
      }
    }
    playerInfo.setCombatAchievements(combatAchievements);
    StoredInfo storedInfo = null;
    try {
      storedInfo =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY),
              StoredInfo.class);
    } catch (JsonSyntaxException exc) {
      log.warn("malformed stored data found, will ignore and overwrite.");
    }
    if (storedInfo == null) {
      storedInfo = new StoredInfo();
      storedInfo.setCollectionLog(new CollectionLog());
      storedInfo.setSlayerException(config.slayer());
      storedInfo.setTasks(new ArrayList<>());
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY, gson.toJson(storedInfo));
    }
    playerInfo.setCollectionLog(storedInfo.getCollectionLog());
    playerInfo.setSlayerException(storedInfo.isSlayerException());
    generalLoaded = true;
  }

  private boolean queryLevels() {
    List<Level> levels =
        Arrays.stream(Skill.values())
            .map(skill -> Level.from(client, skill))
            .collect(Collectors.toList());
    for (Level level : levels) {
      if (!level.isAccurate()) return false;
    }
    playerInfo.setLevels(levels);
    levelsLoaded = true;
    return true;
  }

  private void queryKillCounts() {
    try {
      HiscoreResult result =
          hiscoreClient.lookup(
              playerInfo.getUsername(), playerInfo.getAccountType().getHiscoreEndpoint());
      final List<KillCount> killCounts = new ArrayList<>();
      KillCount.hiscoreSkills.forEach(
          hiscoreSkill -> {
            KillCount killCount = new KillCount();
            killCount.setCount(Math.max(0, result.getSkill(hiscoreSkill).getLevel()));
            killCount.setName(hiscoreSkill.getName());
            killCounts.add(killCount);
          });
      playerInfo.setKillCounts(killCounts);
    } catch (IOException exc) {
      log.warn("IOException while looking up hiscores for player '{}'", playerInfo.getUsername());
    }
    killCountsLoaded = true;
  }

  private boolean waitForLoaded() {
    if (!generalLoaded || !levelsLoaded || !killCountsLoaded) {
      return false;
    }
    handlingLogin = false;
    List<Task> tasks =
        taskStore.getAll().stream()
            .filter(task -> task.checkCompletion(playerInfo))
            .collect(Collectors.toList());
    if (playerInfo.getTasks().size() != tasks.size()) {
      playerInfo.setTasks(tasks);
    }
    eventBus.post(new PlayerInfoUpdated());
    return true;
  }

  private boolean isValidWorldType() {
    if (!client.isClientThread()) {
      return false;
    }
    return Sets.intersection(invalidWorldTypes, client.getWorldType()).isEmpty();
  }
}
