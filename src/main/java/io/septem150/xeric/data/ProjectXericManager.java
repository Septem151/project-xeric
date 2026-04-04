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
import static io.septem150.xeric.util.RegexUtil.ID_GROUP;
import static io.septem150.xeric.util.RegexUtil.KC_REGEX;
import static io.septem150.xeric.util.RegexUtil.NAME_GROUP;
import static io.septem150.xeric.util.RegexUtil.QUEST_REGEX;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.clog.ClogItem;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.diary.KourendDiary;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.CombatAchievement;
import io.septem150.xeric.data.player.KillCount;
import io.septem150.xeric.data.player.Level;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.data.player.QuestProgress;
import io.septem150.xeric.data.task.KCTask;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskType;
import io.septem150.xeric.panel.ProjectXericPanel;
import io.septem150.xeric.util.WorldUtil;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.Quest;
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
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ProjectXericManager {
  public static final Color DARK_GREEN = Color.decode("#006600");

  private final Client client;
  private final ClientThread clientThread;
  private final ProjectXericConfig config;
  private final ConfigManager configManager;
  private final ScheduledExecutorService executor;
  private final HiscoreManager hiscoreManager;
  private final Gson gson;
  private final PlayerInfo playerInfo;
  private final ProjectXericApiClient apiClient;

  private ProjectXericPanel panel;

  private Map<Integer, CombatAchievement> allCombatAchievements;
  private Map<Integer, ClogItem> allItemsById;
  private Map<String, ClogItem> allItemsByName;
  private Set<TaskType> pendingUpdates;
  private Map<Integer, String> previousTaskHashes;
  private JsonObject pendingPlayerUpdate;
  private int ticksTilUpdate;
  private int ticksTilClientReady;

  @Inject
  public ProjectXericManager(
      Client client,
      ClientThread clientThread,
      ProjectXericConfig config,
      ConfigManager configManager,
      ScheduledExecutorService executor,
      HiscoreManager hiscoreManager,
      @Named("xericGson") Gson gson,
      PlayerInfo playerInfo,
      ProjectXericApiClient apiClient) {
    this.client = client;
    this.clientThread = clientThread;
    this.config = config;
    this.configManager = configManager;
    this.executor = executor;
    this.hiscoreManager = hiscoreManager;
    this.gson = gson;
    this.playerInfo = playerInfo;
    this.apiClient = apiClient;
  }

  public void startUp(ProjectXericPanel panel) {
    this.panel = panel;
    ticksTilClientReady = 3;
    ticksTilUpdate = 0;
    pendingUpdates = new HashSet<>();
    panel.startUp();
    SwingUtilities.invokeLater(panel::refresh);
    // handle login as soon as possible from client in case
    // the player is logged in when installing or starting the plugin
    clientThread.invokeLater(
        () -> {
          if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client))
            handleLogin();
        });
    log.info("{} started!", ProjectXericConfig.NAME);
  }

  public void shutDown() {
    playerInfo.reset();
    ticksTilClientReady = 3;
    ticksTilUpdate = 0;
    pendingUpdates = null;
    allItemsById = null;
    allItemsByName = null;
    allCombatAchievements = null;
    panel.shutDown();
    log.info("{} stopped!", ProjectXericConfig.NAME);
  }

  private void handleLogin() {
    // synchronous operations that can be handled directly on client thread
    updateAccountInfo();
    updateClientCache();
    checkClogChatMessageEnabled();
    playerInfo.loadClogFromRSProfile();
    updateCombatAchievements();
    loadSkillLevels();
    updateQuests();
    updateDiaries();
    // asynchronous operations
    updatePlayerHiscores()
        .thenCompose(unused -> apiClient.fetchTasksAsync())
        .thenAccept(
            result -> {
              previousTaskHashes = result.getPreviousTaskHashes();
              playerInfo.setAllTasks(
                  result.getTaskResponse().getTasks(), result.getTaskResponse().getHash());
            })
        .thenRun(
            () -> {
              playerInfo.loadTasksFromRSProfile();
              boolean hasTaskChanges = playerInfo.isTaskListUpdated() && handleTaskListChange();
              stageAccountUpdate();
              flushPlayerUpdate();
              SwingUtilities.invokeLater(
                  () -> {
                    panel.startUpChildren();
                    panel.refresh();
                  });
              clientThread.invokeLater(
                  () -> {
                    if (hasTaskChanges) {
                      playerInfo.notifyTasksUpdated();
                    }
                    scheduleUpdate(0, Set.of(TaskType.values()));
                  });
            });
  }

  private void handleLogout() {
    flushPlayerUpdate();
    playerInfo.logout();
    ticksTilClientReady = 3;
  }

  private void scheduleUpdate(int tickDelay, Set<TaskType> taskTypes) {
    ticksTilUpdate = ticksTilUpdate > 0 ? ticksTilUpdate : Math.max(0, tickDelay);
    pendingUpdates.addAll(taskTypes);
  }

  @Subscribe
  void onGameStateChanged(GameStateChanged event) {
    if (!playerInfo.isLoggedIn() && event.getGameState() == GameState.LOGGED_IN) {
      clientThread.invokeLater(
          () -> {
            if (ticksTilClientReady > 0) return false;
            if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client))
              handleLogin();
            return true;
          });
    } else if (playerInfo.isLoggedIn() && event.getGameState() == GameState.LOGIN_SCREEN) {
      handleLogout();
    } else if (event.getGameState() == GameState.HOPPING) {
      // wait 3 ticks after hopping before checking varbits and statchanges
      ticksTilClientReady = 3;
    }
  }

  @Subscribe
  void onGameTick(GameTick event) {
    if (ticksTilClientReady > 0) {
      ticksTilClientReady--;
      return;
    }
    if (ticksTilUpdate > 0) {
      ticksTilUpdate--;
      return;
    }
    if (pendingUpdates.isEmpty()) return;
    boolean updated = updateXericTasks();
    flushPlayerUpdate();
    if (updated) {
      SwingUtilities.invokeLater(panel::refresh);
    }
  }

  @Subscribe
  void onChatMessage(ChatMessage event) {
    if (event.getType() != ChatMessageType.GAMEMESSAGE) return;
    String message = Text.removeTags(event.getMessage());
    Matcher caTaskMatcher = COMBAT_TASK_REGEX.matcher(message);
    if (caTaskMatcher.matches()) {
      int id = Integer.parseInt(caTaskMatcher.group(ID_GROUP).replace(",", ""));
      playerInfo.addCombatAchievement(id, allCombatAchievements.get(id));
      scheduleUpdate(0, Set.of(TaskType.CA));
      return;
    }
    Matcher diaryMatcher = DIARY_REGEX.matcher(message);
    if (diaryMatcher.matches()) {
      updateDiaries();
      scheduleUpdate(0, Set.of(TaskType.DIARY));
      return;
    }
    Matcher questMatcher = QUEST_REGEX.matcher(message);
    if (questMatcher.matches()) {
      updateQuests();
      scheduleUpdate(0, Set.of(TaskType.QUEST));
    }
    Matcher kcMatcher = KC_REGEX.matcher(message);
    if (kcMatcher.matches()) {
      String name = KCTask.fixBossName(kcMatcher.group(NAME_GROUP));
      int count = Integer.parseInt(kcMatcher.group(COUNT_GROUP).replace(",", ""));
      playerInfo.addHiscore(name, new KillCount(name, count));
      scheduleUpdate(0, Set.of(TaskType.HISCORE));
      return;
    }
    Matcher clueMatcher = CLUE_REGEX.matcher(message);
    if (clueMatcher.matches()) {
      String name = KCTask.fixBossName(clueMatcher.group(NAME_GROUP));
      int count = Integer.parseInt(clueMatcher.group(COUNT_GROUP).replace(",", ""));
      playerInfo.addHiscore(name, new KillCount(name, count));
      scheduleUpdate(0, Set.of(TaskType.HISCORE));
      return;
    }
    Matcher delveMatcher = DELVE_KC_REGEX.matcher(message);
    if (delveMatcher.matches()) {
      String name = HiscoreSkill.DOOM_OF_MOKHAIOTL.getName();
      int count = Integer.parseInt(delveMatcher.group(COUNT_GROUP).replace(",", ""));
      playerInfo.addHiscore(name, new KillCount(name, count));
      scheduleUpdate(0, Set.of(TaskType.HISCORE));
      return;
    }
    Matcher clogMatcher = CLOG_REGEX.matcher(message);
    if (clogMatcher.matches()) {
      String itemName = clogMatcher.group(NAME_GROUP);
      ClogItem clogItem = allItemsByName.get(itemName);
      playerInfo.addClogItem(clogItem);
      scheduleUpdate(0, Set.of(TaskType.CLOG));
    }
  }

  @Subscribe
  public void onStatChanged(StatChanged event) {
    if (ticksTilClientReady > 0) return;
    Level skillLevel =
        playerInfo.getLevels().computeIfAbsent(event.getSkill(), key -> new Level(key, 0, 1));
    if (skillLevel.getXp() < event.getXp()) {
      skillLevel.setXp(event.getXp());
      skillLevel.setLevel(event.getLevel());
      // only refresh level stats every 6 ticks
      scheduleUpdate(6, Set.of(TaskType.LEVEL));
    }
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
          playerInfo.getHiscores().computeIfAbsent(gloryName, key -> new KillCount(key, 0));
      if (gloryHiscore.getCount() < count) {
        gloryHiscore.setCount(count);
        scheduleUpdate(0, Set.of(TaskType.HISCORE));
      }
    }
  }

  @Subscribe
  void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() != COLLECTION_LOG_TRANSMIT_SCRIPT_ID) return;
    int itemId = (int) event.getScriptEvent().getArguments()[1];
    ClogItem clogItem = allItemsById.get(itemId);
    playerInfo.addClogItem(clogItem);
    // schedule an update in 2 ticks to let all items load
    // this method is called for each new item, but delay starts after first call
    scheduleUpdate(2, Set.of(TaskType.CLOG));
  }

  @Subscribe
  void onScriptPostFired(ScriptPostFired event) {
    if (playerInfo.isClogInterfaceOpened() || event.getScriptId() != COLLECTION_LOG_SETUP_SCRIPT_ID)
      return;
    // taken from WikiSync, not really sure what script is being run,
    // but it appears that simulating a click on the Search button
    // loads the script that checks for obtained clog items (not quantities though)
    client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
    client.runScript(2240);
    playerInfo.setClogInterfaceOpened(true);
  }

  @Subscribe
  void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.SLAYER_CONFIG_KEY)) {
      playerInfo.setSlayerException(Boolean.parseBoolean(event.getNewValue()));
      SwingUtilities.invokeLater(panel::refresh);
    }
  }

  private boolean updateXericTasks(boolean showMessage) {
    log.debug(
        "Called updateTaskCompletions with: {}",
        pendingUpdates.stream().map(TaskType::getName).collect(Collectors.toSet()));
    Set<Task> remainingTasksToCheck =
        playerInfo.getRemainingTasks().stream()
            .filter(task -> pendingUpdates.contains(task.getType()))
            .collect(Collectors.toSet());
    boolean updated = false;
    Set<Task> newlyCompleted = new HashSet<>();
    for (Task task : remainingTasksToCheck) {
      if (task.checkCompletion(playerInfo)) {
        updated = true;
        playerInfo.addCompletedTask(task);
        newlyCompleted.add(task);
        if (showMessage) {
          client.addChatMessage(
              ChatMessageType.GAMEMESSAGE,
              "",
              String.format(
                  "Xeric task completed for %d point%s: %s.",
                  task.getTier(),
                  task.getTier() > 1 ? "s" : "",
                  ColorUtil.wrapWithColorTag(task.getName(), DARK_GREEN)),
              null);
        }
      }
    }
    pendingUpdates.clear();
    if (!newlyCompleted.isEmpty()) {
      stageTaskCompletion(newlyCompleted);
    }
    return updated;
  }

  private boolean updateXericTasks() {
    return updateXericTasks(config.chatMessages());
  }

  private boolean handleTaskListChange() {
    // diff new task hashes against previous cache to find changed tasks
    Set<Task> changedTasks = new HashSet<>();
    if (previousTaskHashes != null) {
      for (Task newTask : playerInfo.getAllTasks()) {
        String oldHash = previousTaskHashes.get(newTask.getId());
        if (oldHash == null || !oldHash.equals(newTask.getHash())) {
          changedTasks.add(newTask);
        }
      }
      previousTaskHashes = null;
    }

    if (changedTasks.isEmpty()) {
      return false;
    }

    // remove completions for changed tasks and queue their types for re-check
    playerInfo.getCompletedTasks().removeAll(changedTasks);
    playerInfo.getRemainingTasks().clear();
    playerInfo
        .getRemainingTasks()
        .addAll(Sets.difference(playerInfo.getAllTasks(), playerInfo.getCompletedTasks()));
    clientThread.invokeLater(
        () ->
            scheduleUpdate(
                0, changedTasks.stream().map(Task::getType).collect(Collectors.toSet())));
    return true;
  }

  private void updateAccountInfo() {
    playerInfo.login(
        client.getLocalPlayer().getName(),
        AccountType.fromVarbValue(client.getVarbitValue(VarbitID.IRONMAN)),
        client.getAccountHash());
    playerInfo.setSlayerException(config.slayer());
  }

  private void updateQuests() {
    // load player quests
    for (Quest quest : QuestProgress.TRACKED_QUESTS) {
      playerInfo.addQuest(quest, new QuestProgress(quest, quest.getState(client)));
    }
  }

  private void updateDiaries() {
    // load player diaries
    for (KourendDiary diary : DiaryProgress.TRACKED_DIARIES) {
      playerInfo
          .getDiaries()
          .put(
              diary,
              new DiaryProgress(
                  diary,
                  client.getVarbitValue(diary.getCountVarb()),
                  client.getVarbitValue(diary.getCompletedVarb()) == 1));
    }
  }

  private void updateCombatAchievements() {
    // load player combat achievements
    Set<CombatAchievement> remainingCombatAchievements =
        Sets.difference(
            new HashSet<>(allCombatAchievements.values()),
            new HashSet<>(playerInfo.getCombatAchievements().values()));
    for (CombatAchievement combatAchievement : remainingCombatAchievements) {
      client.runScript(4834, combatAchievement.getId());
      boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;
      if (unlocked) {
        playerInfo.addCombatAchievement(combatAchievement.getId(), combatAchievement);
      }
    }
  }

  private void loadSkillLevels() {
    // load player levels
    for (Skill skill : Skill.values()) {
      Level skillLevel = playerInfo.getLevels().computeIfAbsent(skill, key -> new Level(key, 0, 1));
      int skillXp = client.getSkillExperience(skill);
      if (skillLevel.getXp() < skillXp) {
        skillLevel.setXp(skillXp);
        skillLevel.setLevel(client.getRealSkillLevel(skill));
      }
    }
  }

  private void checkClogChatMessageEnabled() {
    // warn if player doesn't have collection log chat drops enabled
    if (client.getVarbitValue(VarbitID.OPTION_COLLECTION_NEW_ITEM) % 2 != 1) {
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

  private void stageAccountUpdate() {
    AccountType accountType = playerInfo.getAccountType();
    if (accountType == null
        || accountType.getApiName() == null
        || playerInfo.getAccountHash() == -1) return;
    stageIfChanged(ProjectXericConfig.USERNAME_DATA_KEY, "username", playerInfo.getUsername());
    stageIfChanged(
        ProjectXericConfig.ACCOUNT_TYPE_DATA_KEY, "accountType", accountType.getApiName());
    stageExceptions();
  }

  private void stageExceptions() {
    List<String> exceptions = playerInfo.getExceptions();
    String json = gson.toJson(exceptions);
    String stored = configManager.getRSProfileConfiguration(ProjectXericConfig.GROUP, "exceptions");
    if (!json.equals(stored)) {
      if (pendingPlayerUpdate == null) {
        pendingPlayerUpdate = new JsonObject();
      }
      pendingPlayerUpdate.add("exceptions", gson.toJsonTree(exceptions));
      configManager.setRSProfileConfiguration(ProjectXericConfig.GROUP, "exceptions", json);
    }
  }

  private void stageTaskCompletion(Set<Task> tasks) {
    if (playerInfo.getAccountHash() == -1) return;
    if (pendingPlayerUpdate == null) {
      pendingPlayerUpdate = new JsonObject();
    }
    pendingPlayerUpdate.add(
        "completedTasks",
        gson.toJsonTree(tasks.stream().map(Task::getId).collect(Collectors.toSet())));
  }

  private void stageIfChanged(String configKey, String jsonKey, String value) {
    String stored = configManager.getRSProfileConfiguration(ProjectXericConfig.GROUP, configKey);
    if (value != null && !value.equals(stored)) {
      if (pendingPlayerUpdate == null) {
        pendingPlayerUpdate = new JsonObject();
      }
      pendingPlayerUpdate.addProperty(jsonKey, value);
      configManager.setRSProfileConfiguration(ProjectXericConfig.GROUP, configKey, value);
    }
  }

  private void flushPlayerUpdate() {
    if (pendingPlayerUpdate == null || playerInfo.getAccountHash() == -1) return;
    pendingPlayerUpdate.addProperty("accountHash", playerInfo.getAccountHash());
    pendingPlayerUpdate.addProperty("tasksHash", playerInfo.getTasksHash());
    apiClient.postPlayerData(pendingPlayerUpdate);
    pendingPlayerUpdate = null;
  }

  private void updateClientCache() {
    // check if clog items need to be cached
    if (allItemsById == null || allItemsByName == null) {
      allItemsById = new HashMap<>();
      allItemsByName = new HashMap<>();
      for (int itemId : requestAllClogItems()) {
        ItemComposition itemComposition = client.getItemDefinition(itemId);
        ClogItem clogItem = new ClogItem(itemId, itemComposition.getMembersName());
        allItemsById.put(itemId, clogItem);
        allItemsByName.put(clogItem.getName(), clogItem);
      }
    }
    // check if combat achievements need to be cached
    if (allCombatAchievements == null) {
      allCombatAchievements = new HashMap<>();
      for (int caStructId : requestAllCaTaskStructIds()) {
        StructComposition struct = client.getStructComposition(caStructId);
        int caId = struct.getIntValue(CA_STRUCT_ID_PARAM_ID);
        String caName = struct.getStringValue(CA_STRUCT_NAME_PARAM_ID);
        int caPoints = struct.getIntValue(CA_STRUCT_TIER_PARAM_ID);
        CombatAchievement combatAchievement = new CombatAchievement(caId, caName, caPoints);
        allCombatAchievements.put(caId, combatAchievement);
      }
    }
  }

  private CompletableFuture<Void> updatePlayerHiscores() {
    // load player hiscores from jagex
    final String username = client.getLocalPlayer().getName();
    final AccountType accountType =
        AccountType.fromVarbValue(client.getVarbitValue(VarbitID.IRONMAN));
    CompletableFuture<Void> future = new CompletableFuture<>();
    executor.execute(
        () -> {
          try {
            HiscoreResult hiscoreResult =
                hiscoreManager.lookup(username, accountType.getHiscoreEndpoint());
            for (HiscoreSkill hiscoreSkill : KillCount.HISCORE_SKILLS) {
              String name = hiscoreSkill.getName();
              int count = Math.max(0, hiscoreResult.getSkill(hiscoreSkill).getLevel());
              if (count == 0) {
                // fallback to checking killcount config if player not ranked on hiscore
                Integer configCount =
                    configManager.getRSProfileConfiguration(
                        "killcount", name.toLowerCase(), Integer.class);
                if (configCount != null) count = configCount;
              }
              playerInfo.addHiscore(name, new KillCount(name, count));
            }
            future.complete(null);
          } catch (IOException err) {
            future.completeExceptionally(err);
          }
        });
    return future;
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
  private Set<Integer> requestAllClogItems() {
    Set<Integer> clogItems = new HashSet<>();
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
        for (int clogSubTabItemId : clogSubTabItemIds) clogItems.add(clogSubTabItemId);
      }
    }
    for (int badItemId : itemReplacementMapping.getKeys()) clogItems.remove(badItemId);
    for (int goodItemId : itemReplacementMapping.getIntVals()) clogItems.add(goodItemId);
    // remove duplicate Prospector outfit
    for (int prospectorItemId : UNUSED_PROSPECTOR_ITEM_IDS) clogItems.remove(prospectorItemId);
    return clogItems;
  }
}
