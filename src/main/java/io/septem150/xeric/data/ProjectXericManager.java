package io.septem150.xeric.data;

import static io.septem150.xeric.data.clog.CollectionLog.CLOG_SUB_TABS_PARAM_ID;
import static io.septem150.xeric.data.clog.CollectionLog.CLOG_SUB_TAB_ITEMS_PARAM_ID;
import static io.septem150.xeric.data.clog.CollectionLog.CLOG_TOP_TABS_ENUM_ID;
import static io.septem150.xeric.data.clog.CollectionLog.COLLECTION_LOG_SETUP_SCRIPT_ID;
import static io.septem150.xeric.data.clog.CollectionLog.COLLECTION_LOG_TRANSMIT_SCRIPT_ID;
import static io.septem150.xeric.data.clog.CollectionLog.ITEM_REPLACEMENT_MAPPING_ENUM_ID;
import static io.septem150.xeric.data.clog.CollectionLog.UNUSED_PROSPECTOR_ITEM_IDS;
import static io.septem150.xeric.data.player.CombatAchievement.CA_STRUCT_ID_PARAM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.CA_STRUCT_NAME_PARAM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.CA_STRUCT_TIER_PARAM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.EASY_TIER_ENUM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.ELITE_TIER_ENUM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.GM_TIER_ENUM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.HARD_TIER_ENUM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.MASTER_TIER_ENUM_ID;
import static io.septem150.xeric.data.player.CombatAchievement.MEDIUM_TIER_ENUM_ID;
import static io.septem150.xeric.util.RegexUtil.CLOG_REGEX;
import static io.septem150.xeric.util.RegexUtil.CLUE_REGEX;
import static io.septem150.xeric.util.RegexUtil.COMBAT_TASK_REGEX;
import static io.septem150.xeric.util.RegexUtil.COUNT_GROUP;
import static io.septem150.xeric.util.RegexUtil.DELVE_KC_REGEX;
import static io.septem150.xeric.util.RegexUtil.DIARY_REGEX;
import static io.septem150.xeric.util.RegexUtil.KC_REGEX;
import static io.septem150.xeric.util.RegexUtil.NAME_GROUP;
import static io.septem150.xeric.util.RegexUtil.QUEST_REGEX;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.clog.ClogItem;
import io.septem150.xeric.data.clog.CollectionLog;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.CombatAchievement;
import io.septem150.xeric.data.player.KillCount;
import io.septem150.xeric.data.player.Level;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.data.player.QuestProgress;
import io.septem150.xeric.data.task.KCTask;
import io.septem150.xeric.data.task.LevelTask;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.panel.ProjectXericPanel;
import io.septem150.xeric.util.WorldUtil;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ProjectXericManager {
  private static final Color DARK_GREEN = Color.decode("#006600");

  private final Client client;
  private final ClientThread clientThread;
  private final ProjectXericConfig config;
  private final ConfigManager configManager;
  private final ScheduledExecutorService executor;
  private final HiscoreManager hiscoreManager;
  private final TaskStore taskStore;
  private final Gson gson;

  private ProjectXericPanel panel;
  private long lastAccountId;
  private Set<Integer> remainingCaStructIds;
  private Set<Integer> completedTaskIds;
  private Set<ClogItem> allClogItems;
  private Set<Task> remainingTasks;
  private Set<Task> levelTasks;
  private boolean clogOpened;
  private int updateGeneral;
  private int updateTasks;
  private int updateLevels;
  private int ticksTilClientReady;
  @Getter private PlayerInfo playerInfo;

  @Inject
  public ProjectXericManager(
      Client client,
      ClientThread clientThread,
      ProjectXericConfig config,
      ConfigManager configManager,
      ScheduledExecutorService executor,
      HiscoreManager hiscoreManager,
      TaskStore taskStore,
      @Named("xericGson") Gson gson) {
    this.client = client;
    this.clientThread = clientThread;
    this.config = config;
    this.configManager = configManager;
    this.executor = executor;
    this.hiscoreManager = hiscoreManager;
    this.taskStore = taskStore;
    this.gson = gson;
  }

  public void startUp(ProjectXericPanel panel) {
    this.panel = panel;
    lastAccountId = -1L;
    ticksTilClientReady = 2;
    playerInfo = new PlayerInfo();
    clientThread.invokeLater(
        () -> {
          if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client)) {
            updateGeneral = 1;
          }
        });
    taskStore
        .getAllAsync(false)
        .exceptionally(
            err -> {
              throw new RuntimeException(err);
            });
  }

  public void shutDown() {
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP,
        ProjectXericConfig.CLOG_DATA_KEY,
        gson.toJson(playerInfo.getCollectionLog().getItemIds()));
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP,
        ProjectXericConfig.TASKS_DATA_KEY,
        gson.toJson(playerInfo.getTasks().stream().map(Task::getId).collect(Collectors.toSet())));
    playerInfo = null;
    lastAccountId = -1L;
    clogOpened = false;
    remainingCaStructIds = null;
    allClogItems = null;
    completedTaskIds = null;
    levelTasks = null;
    updateGeneral = 0;
    updateTasks = 0;
    updateLevels = 0;
    taskStore.reset();
  }

  public void reset(long accountId) {
    if (lastAccountId == accountId) return;
    lastAccountId = accountId;
    playerInfo = new PlayerInfo();
    updateAccountInfo();
    updateQuests();
    updateDiaries();
    updateCombatAchievements();
    updateCollectionLog();

    // Attempt to load collection log data from RuneLite Profile data first
    try {
      Type type = new TypeToken<Set<Integer>>() {}.getType();
      completedTaskIds =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_DATA_KEY),
              type);
    } catch (JsonSyntaxException exc) {
      log.warn("malformed stored tasks data found, will ignore and overwrite.");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.TASKS_DATA_KEY);
    }
    // Save empty clog to RuneLite Profile if malformed clog data or null key
    if (completedTaskIds == null) {
      completedTaskIds = new HashSet<>();
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP,
          ProjectXericConfig.TASKS_DATA_KEY,
          gson.toJson(completedTaskIds));
    }

    // Update player tasks and levels in the background, as these are both asynchronous processes
    // that require network calls, and we don't want the panel or task-checking logic running until
    // those calls are complete
    CompletableFuture.allOf(updatePlayerTasks(), updatePlayerHiscores())
        .thenRun(
            () -> {
              updateLevels = 2;
              updateTasks = 2;
              clientThread.invoke(
                  () -> {
                    panel.startUpChildren();
                    SwingUtilities.invokeLater(panel::refresh);
                  });
            });
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client)) {
      updateGeneral = 1;
    } else if (client.getGameState() == GameState.LOGIN_SCREEN) {
      ticksTilClientReady = 2;
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP,
          ProjectXericConfig.CLOG_DATA_KEY,
          gson.toJson(playerInfo.getCollectionLog().getItemIds()));
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP,
          ProjectXericConfig.TASKS_DATA_KEY,
          gson.toJson(playerInfo.getTasks().stream().map(Task::getId).collect(Collectors.toSet())));
      clogOpened = false;
      updateLevels = 2;
    } else if (client.getGameState() == GameState.HOPPING) {
      ticksTilClientReady = 2;
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (ticksTilClientReady > 0) {
      ticksTilClientReady--;
      if (ticksTilClientReady == 0) {
        checkClogChatMessageEnabled();
      }
    }
    if (updateGeneral > 0 && --updateGeneral == 0) {
      reset(client.getAccountHash());
      return;
    }
    if (updateLevels > 0 && --updateLevels == 0) {
      if (playerInfo.getLevels().isEmpty()) {
        playerInfo.setLevels(
            Arrays.stream(Skill.values())
                .map(skill -> Level.from(client, skill))
                .collect(Collectors.toMap(Level::getName, level -> level)));
      }
      updateXericTasks(true);
    }
    if (updateTasks > 0 && --updateTasks == 0) {
      updateXericTasks(false);
    }
  }

  @Subscribe
  public void onChatMessage(ChatMessage event) {
    if (event.getType() != ChatMessageType.GAMEMESSAGE) return;
    String message = Text.removeTags(event.getMessage());
    Matcher caTaskMatcher = COMBAT_TASK_REGEX.matcher(message);
    if (caTaskMatcher.matches()) {
      if (updateCombatAchievements() && updateTasks <= 0) updateTasks = 1;
      return;
    }
    Matcher diaryMatcher = DIARY_REGEX.matcher(message);
    if (diaryMatcher.matches()) {
      updateDiaries();
      if (updateTasks <= 0) updateTasks = 1;
      return;
    }
    Matcher questMatcher = QUEST_REGEX.matcher(message);
    if (questMatcher.matches()) {
      updateQuests();
      if (updateTasks <= 0) updateTasks = 1;
      return;
    }
    Matcher kcMatcher = KC_REGEX.matcher(message);
    if (kcMatcher.matches()) {
      String name = KCTask.fixBossName(kcMatcher.group(NAME_GROUP));
      int count = Integer.parseInt(kcMatcher.group(COUNT_GROUP).replace(",", ""));
      KillCount kc = playerInfo.getKillCounts().getOrDefault(name, null);
      if (kc != null) {
        kc.setCount(count);
        if (updateTasks <= 0) updateTasks = 1;
      }
      return;
    }
    Matcher clueMatcher = CLUE_REGEX.matcher(message);
    if (clueMatcher.matches()) {
      String name = KCTask.fixBossName(clueMatcher.group(NAME_GROUP));
      int count = Integer.parseInt(clueMatcher.group(COUNT_GROUP).replace(",", ""));
      KillCount kc = playerInfo.getKillCounts().getOrDefault(name, null);
      if (kc != null) {
        kc.setCount(count);
        if (updateTasks <= 0) updateTasks = 1;
      }
      return;
    }
    Matcher delveMatcher = DELVE_KC_REGEX.matcher(message);
    if (delveMatcher.matches()) {
      String name = HiscoreSkill.DOOM_OF_MOKHAIOTL.getName();
      int count = Integer.parseInt(delveMatcher.group(COUNT_GROUP).replace(",", ""));
      KillCount kc = playerInfo.getKillCounts().getOrDefault(name, null);
      if (kc != null) {
        kc.setCount(count);
        if (updateTasks <= 0) updateTasks = 1;
      }
      return;
    }
    Matcher clogMatcher = CLOG_REGEX.matcher(message);
    if (clogMatcher.matches()) {
      String itemName = Text.removeTags(clogMatcher.group(NAME_GROUP));
      ClogItem newClogItem =
          allClogItems.stream()
              .filter(clogItem -> clogItem.getName().equals(itemName))
              .findFirst()
              .orElse(null);
      if (newClogItem != null) {
        playerInfo.getCollectionLog().add(newClogItem);
        if (updateTasks <= 0) updateTasks = 1;
      }
    }
  }

  @Subscribe
  public void onStatChanged(StatChanged event) {
    if (playerInfo.getLevels().isEmpty()) return;
    Level level = playerInfo.getLevels().getOrDefault(event.getSkill().getName(), null);
    if (level == null) {
      level = new Level();
      level.setName(event.getSkill().getName());
    }
    if (level.getExp() == event.getXp()) return;
    level.setAmount(event.getLevel());
    level.setExp(event.getXp());
    if (updateLevels <= 0 && level.isAccurate()) updateLevels = 5;
  }

  @Subscribe
  void onVarbitChanged(VarbitChanged event) {
    if (ticksTilClientReady > 0) return;
    int varbId = event.getVarbitId();
    int varpId = event.getVarpId();
    if (varbId != -1) {
      if (varbId != VarbitID.OPTION_COLLECTION_NEW_ITEM) return;
      checkClogChatMessageEnabled();
    } else if (varpId != -1) {
      if (varpId != VarPlayerID.COLOSSEUM_CURRENT_GLORY) return;
      int count = event.getValue();
      String gloryName = HiscoreSkill.COLOSSEUM_GLORY.getName();
      KillCount gloryHiscore =
          playerInfo
              .getKillCounts()
              .computeIfAbsent(
                  gloryName,
                  key -> {
                    KillCount kc = new KillCount();
                    kc.setName(key);
                    kc.setCount(0);
                    return kc;
                  });
      if (gloryHiscore.getCount() < count) {
        gloryHiscore.setCount(count);
        if (updateTasks <= 0) updateTasks = 1;
      }
    }
  }

  @Subscribe
  public void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() != COLLECTION_LOG_TRANSMIT_SCRIPT_ID) return;
    int itemId = (int) event.getScriptEvent().getArguments()[1];
    if (playerInfo.getCollectionLog().getItems().stream()
        .noneMatch(item -> item.getId() == itemId)) {
      ClogItem clogItem = ClogItem.from(client, itemId);
      playerInfo.getCollectionLog().getItems().add(clogItem);
    }
  }

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() != COLLECTION_LOG_SETUP_SCRIPT_ID) {
      return;
    }
    if (!clogOpened) {
      playerInfo.getCollectionLog().setLastOpened(Instant.now());
      // taken from WikiSync, not really sure what script is being run,
      // but it appears that simulating a click on the Search button
      // loads the script that checks for obtained clog items (not quantities though)
      client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
      client.runScript(2240);
      clogOpened = true;
      updateTasks = 3;
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.SLAYER_CONFIG_KEY)) {
      playerInfo.setSlayerException(Boolean.parseBoolean(event.getNewValue()));
      SwingUtilities.invokeLater(panel::refresh);
    }
  }

  private void updateXericTasks(boolean onlyLevels) {
    executor.execute(
        () -> {
          log.debug("Called UPDATE TASKS, Only Levels: {}", onlyLevels);
          boolean refresh = false;
          Set<Task> completedTasks = new HashSet<>(playerInfo.getTasks());
          Iterator<Task> iterator = onlyLevels ? levelTasks.iterator() : remainingTasks.iterator();
          log.debug(
              "Iterating over {} tasks", onlyLevels ? levelTasks.size() : remainingTasks.size());
          while (iterator.hasNext()) {
            Task task = iterator.next();
            if (task.checkCompletion(playerInfo)) {
              log.debug("New completed task!\n{}", gson.toJson(task));
              completedTasks.add(task);
              iterator.remove();
              if (onlyLevels) remainingTasks.remove(task);
              else levelTasks.remove(task);
              refresh = true;
              if (config.chatMessages()) {
                clientThread.invokeLater(
                    () -> {
                      int points = config.slayer() ? task.getSlayerPoints() : task.getTier();
                      client.addChatMessage(
                          ChatMessageType.GAMEMESSAGE,
                          ProjectXericConfig.NAME,
                          String.format(
                              "Xeric task completed for %d point%s: %s.",
                              points,
                              points > 1 ? "s" : "",
                              ColorUtil.wrapWithColorTag(task.getName(), DARK_GREEN)),
                          "");
                    });
              }
            }
          }
          if (refresh) {
            playerInfo.setTasks(new ArrayList<>(completedTasks));
            SwingUtilities.invokeLater(panel::refresh);
          }
        });
  }

  private void updateAccountInfo() {
    playerInfo.setUsername(client.getLocalPlayer().getName());
    playerInfo.setAccountType(AccountType.fromVarbValue(client.getVarbitValue(VarbitID.IRONMAN)));
    playerInfo.setSlayerException(config.slayer());
  }

  private void updateQuests() {
    playerInfo.setQuests(
        QuestProgress.trackedQuests.stream()
            .map(quest -> QuestProgress.from(client, quest))
            .collect(Collectors.toList()));
  }

  private void updateDiaries() {
    playerInfo.setDiaries(
        DiaryProgress.trackedDiaries.stream()
            .map(diary -> DiaryProgress.from(client, diary))
            .collect(Collectors.toList()));
  }

  private boolean updateCombatAchievements() {
    if (remainingCaStructIds == null) {
      remainingCaStructIds = requestAllCaTaskStructIds();
    }
    Set<CombatAchievement> cas = new HashSet<>(playerInfo.getCombatAchievements());
    boolean refresh = false;
    Iterator<Integer> iterator = remainingCaStructIds.iterator();
    while (iterator.hasNext()) {
      int caStructId = iterator.next();
      StructComposition struct = client.getStructComposition(caStructId);
      int caTaskId = struct.getIntValue(CA_STRUCT_ID_PARAM_ID);
      client.runScript(4834, caTaskId);
      boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;
      if (unlocked) {
        CombatAchievement combatAchievement = new CombatAchievement();
        combatAchievement.setId(caTaskId);
        combatAchievement.setName(struct.getStringValue(CA_STRUCT_NAME_PARAM_ID));
        combatAchievement.setPoints(struct.getIntValue(CA_STRUCT_TIER_PARAM_ID));
        cas.add(combatAchievement);
        iterator.remove();
        refresh = true;
      }
    }
    if (refresh) {
      playerInfo.setCombatAchievements(new ArrayList<>(cas));
    }
    return refresh;
  }

  private void updateCollectionLog() {
    allClogItems = requestAllClogItems();
    Set<Integer> obtainedClogItemIds = null;
    Instant clogUpdated = null;

    // Attempt to load collection log data from RuneLite Profile data first
    try {
      Type type = new TypeToken<Set<Integer>>() {}.getType();
      obtainedClogItemIds =
          gson.fromJson(
              configManager.getRSProfileConfiguration(
                  ProjectXericConfig.GROUP, ProjectXericConfig.CLOG_DATA_KEY),
              type);
    } catch (JsonSyntaxException exc) {
      log.warn("malformed stored clog data found, will ignore and overwrite.");
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.CLOG_DATA_KEY);
    }
    // Save empty clog to RuneLite Profile if malformed clog data or null key
    if (obtainedClogItemIds == null) {
      obtainedClogItemIds = new HashSet<>();
      configManager.setRSProfileConfiguration(
          ProjectXericConfig.GROUP,
          ProjectXericConfig.CLOG_DATA_KEY,
          gson.toJson(obtainedClogItemIds));
    } else {
      clogUpdated = Instant.now();
    }

    CollectionLog clog = new CollectionLog();
    clog.setLastOpened(clogUpdated);
    clog.setItems(
        obtainedClogItemIds.stream()
            .map(itemId -> ClogItem.from(client, itemId))
            .collect(Collectors.toList()));
    playerInfo.setCollectionLog(clog);
  }

  private void checkClogChatMessageEnabled() {
    // warn if player doesn't have collection log chat drops enabled
    if (client.getVarbitValue(VarbitID.OPTION_COLLECTION_NEW_ITEM) != 1) {
      client.addChatMessage(
          ChatMessageType.GAMEMESSAGE,
          "",
          String.format(
              "[%s] Warning: %s",
              ColorUtil.wrapWithColorTag(ProjectXericConfig.NAME, DARK_GREEN),
              ColorUtil.wrapWithColorTag(
                  "Tasks will not update properly unless you enable the game setting:"
                      + " Collection log - New addition notification",
                  Color.RED)),
          null);
    }
  }

  private CompletableFuture<Void> updatePlayerTasks() {
    return taskStore
        .getAllAsync()
        .exceptionally(
            err -> {
              throw new RuntimeException(err);
            })
        .thenAccept(
            tasks -> {
              remainingTasks = new HashSet<>(tasks);
              List<Task> completedTasks = new ArrayList<>();
              Iterator<Task> iterator = remainingTasks.iterator();
              while (iterator.hasNext()) {
                Task task = iterator.next();
                if (completedTaskIds.contains(task.getId())) {
                  iterator.remove();
                  completedTasks.add(task);
                }
              }
              playerInfo.setTasks(completedTasks);
              levelTasks =
                  remainingTasks.stream()
                      .filter(LevelTask.class::isInstance)
                      .collect(Collectors.toSet());
            });
  }

  private CompletableFuture<Void> updatePlayerHiscores() {
    return CompletableFuture.runAsync(
        () -> {
          HiscoreEndpoint hiscoreEndpoint = playerInfo.getAccountType().getHiscoreEndpoint();
          Map<String, KillCount> kcs = new HashMap<>();
          try {
            HiscoreResult result =
                hiscoreManager.lookup(client.getLocalPlayer().getName(), hiscoreEndpoint);
            KillCount.hiscoreSkills.forEach(
                hiscoreSkill -> {
                  KillCount killCount = new KillCount();
                  killCount.setCount(Math.max(0, result.getSkill(hiscoreSkill).getLevel()));
                  killCount.setName(hiscoreSkill.getName());
                  kcs.put(killCount.getName(), killCount);
                });
            playerInfo.setKillCounts(kcs);
          } catch (IOException exc) {
            log.warn(
                "IOException while looking up hiscores for player '{}'",
                client.getLocalPlayer().getName());
          }
        },
        executor);
  }

  private Set<Integer> requestAllCaTaskStructIds() {
    Set<Integer> allCaTaskStructIds = new HashSet<>();
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
      for (int caTierStructId : caTiersEnum.getIntVals()) {
        allCaTaskStructIds.add(caTierStructId);
      }
    }
    return allCaTaskStructIds;
  }

  /**
   * Parse the enums and structs in the cache to figure out which item ids exist in the collection
   *
   * <p>log.
   *
   * @return a {@link Set} containing the IDs of all collection log items.
   * @see <a
   *     href="https://github.com/weirdgloop/WikiSync/blob/master/src/main/java/com/andmcadams/wikisync/WikiSyncPlugin.java">WikiSyncPlugin</a>
   */
  private Set<ClogItem> requestAllClogItems() {
    Set<ClogItem> clogItems = new HashSet<>();
    // Some items with data saved on them have replacements to fix a duping issue (satchels,
    // flamtaer bag)
    // Enum 3721 contains a mapping of the item ids to replace -> ids to replace them with
    EnumComposition itemReplacementMapping = client.getEnum(ITEM_REPLACEMENT_MAPPING_ENUM_ID);
    // 2102 - Struct that contains the highest level tabs in the collection log (Bosses, Raids, etc)
    // https://chisel.weirdgloop.org/structs/index.html?type=enums&id=2102
    int[] clogTopTabsEnum = client.getEnum(CLOG_TOP_TABS_ENUM_ID).getIntVals();
    for (int clogTopLevelTabStructId : clogTopTabsEnum) {
      // The collection log top level tab structs contain a param that points to the enum
      // that contains the pointers to sub tabs.
      // ex: https://chisel.weirdgloop.org/structs/index.html?type=structs&id=471
      StructComposition clogTopLevelTabStruct =
          client.getStructComposition(clogTopLevelTabStructId);
      // Param 683 contains the pointer to the enum that contains the subtabs ids
      // ex: https://chisel.weirdgloop.org/structs/index.html?type=enums&id=2103
      int[] clogSubTabStructIds =
          client.getEnum(clogTopLevelTabStruct.getIntValue(CLOG_SUB_TABS_PARAM_ID)).getIntVals();
      for (int clogSubTabStructId : clogSubTabStructIds) {
        // The subtab structs are for subtabs in the collection log (Commander Zilyana, Chambers of
        // Xeric, etc.)
        // and contain a pointer to the enum that contains all the item ids for that tab.
        // ex subtab struct: https://chisel.weirdgloop.org/structs/index.html?type=structs&id=476
        // ex subtab enum: https://chisel.weirdgloop.org/structs/index.html?type=enums&id=2109
        StructComposition clogSubTabStruct = client.getStructComposition(clogSubTabStructId);
        int[] clogSubTabItemIds =
            client.getEnum(clogSubTabStruct.getIntValue(CLOG_SUB_TAB_ITEMS_PARAM_ID)).getIntVals();
        for (int clogSubTabItemId : clogSubTabItemIds) {
          ClogItem clogItem = ClogItem.from(client, clogSubTabItemId);
          if (Arrays.stream(itemReplacementMapping.getKeys())
              .anyMatch(key -> key == clogSubTabItemId)) {
            clogItem.setId(itemReplacementMapping.getIntValue(clogSubTabItemId));
          }
          // remove duplicate Prospector outfit
          if (!UNUSED_PROSPECTOR_ITEM_IDS.contains(clogSubTabItemId)) {
            clogItems.add(clogItem);
          }
        }
      }
    }
    return clogItems;
  }
}
