package io.septem150.xeric;

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
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.data.AccountType;
import io.septem150.xeric.data.ClogItem;
import io.septem150.xeric.data.CollectionLog;
import io.septem150.xeric.data.CombatAchievement;
import io.septem150.xeric.data.DiaryProgress;
import io.septem150.xeric.data.KillCount;
import io.septem150.xeric.data.Level;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.QuestProgress;
import io.septem150.xeric.data.StoredInfo;
import io.septem150.xeric.data.task.LocalTaskStore;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.data.task.TaskTypeAdapter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Project Xeric plugin.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@Slf4j
@PluginDescriptor(name = "Project Xeric")
public final class ProjectXericPlugin extends Plugin {
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

  @Inject private Client client;
  @Inject private ClientThread clientThread;
  @Inject private ProjectXericConfig config;
  @Inject private ConfigManager configManager;
  @Inject private PlayerInfo playerInfo;
  @Inject private HiscoreClient hiscoreClient;
  @Inject private ScheduledExecutorService executor;
  @Inject private TaskStore taskStore;

  @Inject
  private @Named("xericGson") Gson gson;

  private ProjectXericPanel panel;

  private boolean checkTasks;
  private boolean clogOpened;
  private boolean fetchGeneral;
  private boolean fetchLevels;
  private boolean generalLoaded;
  private boolean hiscoresLoaded;
  private boolean levelsLoaded;
  private int fetchHiscores;
  private int updateClog;
  private long lastAccount;

  private void reset() {
    checkTasks = true;
    clogOpened = false;
    fetchGeneral = true;
    fetchHiscores = 2;
    fetchLevels = true;
    generalLoaded = false;
    hiscoresLoaded = false;
    lastAccount = -1L;
    levelsLoaded = false;
    updateClog = 0;
  }

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    panel.init();
    SwingUtilities.invokeLater(panel::reload);
    reset();
    clientThread.invokeLater(
        () -> {
          if (client.getGameState() == GameState.LOGGED_IN && isValidWorldType()) {
            lastAccount = client.getAccountHash();
          }
        });
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    reset();
    panel.stop();
    //    sessionManager.reset();
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (!isValidWorldType()) {
      log.debug("Not logged in to main worlds, ignoring");
      reset();
      return;
    }
    if (GameState.LOGGED_IN == event.getGameState()) {
      long account = client.getAccountHash();
      if (account != lastAccount) {
        log.debug("Account change: {} -> {}", lastAccount, account);
        reset();
        lastAccount = account;
      }
    } else if (GameState.LOGIN_SCREEN == event.getGameState()) {
      StoredInfo storedInfo = new StoredInfo();
      storedInfo.setSlayerException(playerInfo.isSlayerException());
      storedInfo.setClogItems(
          playerInfo.getCollectionLog().getItems().stream()
              .map(ClogItem::getId)
              .collect(Collectors.toList()));
      storedInfo.setLastUpdated(playerInfo.getCollectionLog().getLastOpened());
      storedInfo.setTasks(
          playerInfo.getTasks().stream().map(Task::getId).collect(Collectors.toList()));
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY, gson.toJson(storedInfo));
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.SLAYER)) {
      playerInfo.setSlayerException(Boolean.parseBoolean(event.getNewValue()));
      SwingUtilities.invokeLater(panel::reload);
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (updateClog > 0 && --updateClog == 0) {
      checkTasks = true;
    }
    if (fetchHiscores > 0 && --fetchHiscores == 0) {
      HiscoreEndpoint hiscoreEndpoint =
          AccountType.fromVarbValue(client.getVarbitValue(Varbits.ACCOUNT_TYPE))
              .getHiscoreEndpoint();
      executor.execute(
          () -> {
            try {
              HiscoreResult result =
                  hiscoreClient.lookup(client.getLocalPlayer().getName(), hiscoreEndpoint);
              final List<KillCount> killCounts = new ArrayList<>();
              KillCount.hiscoreSkills.forEach(
                  hiscoreSkill -> {
                    KillCount killCount = new KillCount();
                    killCount.setCount(Math.max(0, result.getSkill(hiscoreSkill).getLevel()));
                    killCount.setName(hiscoreSkill.getName());
                    killCounts.add(killCount);
                  });
              log.info("Loaded killCounts:\n{}", gson.toJson(killCounts));
              playerInfo.setKillCounts(killCounts);
              hiscoresLoaded = true;
            } catch (IOException exc) {
              log.warn(
                  "IOException while looking up hiscores for player '{}'",
                  client.getLocalPlayer().getName());
            }
          });
    }
    if (fetchLevels) {
      List<Level> levels =
          Arrays.stream(Skill.values())
              .map(skill -> Level.from(client, skill))
              .collect(Collectors.toList());
      log.info("Loaded levels:\n{}", gson.toJson(levels));
      playerInfo.setLevels(levels);
      levelsLoaded = true;
      fetchLevels = false;
    }
    if (fetchGeneral) {
      playerInfo.setAccountType(
          AccountType.fromVarbValue(client.getVarbitValue(Varbits.ACCOUNT_TYPE)));
      log.info("Loaded accountType: {}", playerInfo.getAccountType());
      playerInfo.setDiaries(
          DiaryProgress.trackedDiaries.stream()
              .map(diary -> DiaryProgress.from(client, diary))
              .collect(Collectors.toList()));
      log.info("Loaded diaries:\n{}", gson.toJson(playerInfo.getDiaries()));
      playerInfo.setQuests(
          QuestProgress.trackedQuests.stream()
              .map(quest -> QuestProgress.from(client, quest))
              .collect(Collectors.toList()));
      log.info("Loaded quests:\n{}", gson.toJson(playerInfo.getQuests()));
      playerInfo.setUsername(client.getLocalPlayer().getName());
      log.info("Loaded username: {}", playerInfo.getUsername());
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
      log.info("Loaded combatAchievements:\n{}", gson.toJson(combatAchievements));
      StoredInfo storedInfo = null;
      try {
        storedInfo =
            gson.fromJson(
                configManager.getRSProfileConfiguration(
                    ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY),
                StoredInfo.class);
      } catch (JsonSyntaxException exc) {
        log.warn("malformed stored data found, will ignore and overwrite.");
        configManager.unsetRSProfileConfiguration(
            ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY);
      }
      if (storedInfo == null) {
        storedInfo = new StoredInfo();
        storedInfo.setSlayerException(config.slayer());
        storedInfo.setClogItems(new ArrayList<>());
        storedInfo.setTasks(new ArrayList<>());
        configManager.setRSProfileConfiguration(
            ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY, gson.toJson(storedInfo));
        log.info("Saved storedInfo:\n{}", gson.toJson(storedInfo));
      } else {
        log.info("Loaded storedInfo:\n{}", gson.toJson(storedInfo));
      }
      CollectionLog collectionLog = new CollectionLog();
      collectionLog.setLastOpened(storedInfo.getLastUpdated());
      collectionLog.setItems(
          storedInfo.getClogItems().stream()
              .map(itemId -> ClogItem.from(client, itemId))
              .collect(Collectors.toList()));
      playerInfo.setCollectionLog(collectionLog);
      log.info("Loaded collectionLog:\n{}", gson.toJson(playerInfo.getCollectionLog()));
      playerInfo.setSlayerException(storedInfo.isSlayerException());
      log.info("Loaded slayerException: {}", playerInfo.isSlayerException());
      playerInfo.setTasks(
          storedInfo.getTasks().stream()
              .map(taskId -> taskStore.getById(taskId).orElseThrow())
              .collect(Collectors.toList()));
      log.info("Loaded tasks:\n{}", gson.toJson(playerInfo.getTasks()));
      generalLoaded = true;
      fetchGeneral = false;
    }
    if (checkTasks && generalLoaded && hiscoresLoaded && levelsLoaded) {
      List<Task> tasks =
          taskStore.getAll().stream()
              .filter(task -> task.checkCompletion(playerInfo))
              .collect(Collectors.toList());
      if (playerInfo.getTasks().size() != tasks.size()) {
        playerInfo.setTasks(tasks);
        log.info("Updated tasks:\n{}", gson.toJson(tasks));
      }
      SwingUtilities.invokeLater(panel::reload);
      checkTasks = false;
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

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() != COLLECTION_LOG_SETUP_SCRIPT_ID) {
      return;
    }
    if (!clogOpened) {
      // wait 2 ticks to update
      updateClog = 3;
      playerInfo.getCollectionLog().setLastOpened(Instant.now());
      // taken from WikiSync, not really sure what script is being run,
      // but it appears that simulating a click on the Search button
      // loads the script that checks for clog items
      client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
      client.runScript(2240);
      clogOpened = true;
    }
  }

  public void onStatChanged(StatChanged event) {
    if (!(generalLoaded && hiscoresLoaded && levelsLoaded)) {
      return;
    }
    for (Level level : playerInfo.getLevels()) {
      if (level.getName().equals(event.getSkill().getName())) {
        if (level.getValue() != event.getLevel()) {
          level.setValue(event.getLevel());
          checkTasks = true;
        }
        break;
      }
    }
  }

  @Subscribe
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      //      sessionManager.reset();
      //      log.info(gson.toJson(playerInfo));
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY);
      SwingUtilities.invokeLater(panel::reload);
    }
  }

  @Override
  public void configure(Binder binder) {
    super.configure(binder);
    binder.bind(TaskStore.class).to(LocalTaskStore.class);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  public Gson provideGson(Gson gson) {
    return gson.newBuilder()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(Task.class, new TaskTypeAdapter())
        .create();
  }

  private boolean isValidWorldType() {
    if (!client.isClientThread()) {
      return false;
    }
    return Sets.intersection(invalidWorldTypes, client.getWorldType()).isEmpty();
  }
}
