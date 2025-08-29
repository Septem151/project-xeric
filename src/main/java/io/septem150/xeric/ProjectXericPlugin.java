package io.septem150.xeric;

import static io.septem150.xeric.data.CombatAchievement.*;
import static io.septem150.xeric.data.clog.CollectionLog.*;
import static io.septem150.xeric.util.RegexUtil.*;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import io.septem150.xeric.data.*;
import io.septem150.xeric.data.clog.ClogItem;
import io.septem150.xeric.data.diary.AchievementDiary;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.PlayerData;
import io.septem150.xeric.task.*;
import io.septem150.xeric.util.RuntimeTypeAdapterFactory;
import io.septem150.xeric.util.WorldUtil;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfile;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import okhttp3.*;

@Slf4j
@PluginDescriptor(name = ProjectXericConfig.PLUGIN_NAME)
public class ProjectXericPlugin extends Plugin {
  @Inject private Client client;
  @Inject private ClientThread clientThread;
  @Inject private ScheduledExecutorService executor;
  @Inject private OkHttpClient httpClient;
  @Inject private ConfigManager configManager;
  @Inject private HiscoreManager hiscoreManager;
  @Inject private ProjectXericConfig config;

  @Inject
  @Named("xericGson")
  private Gson gson;

  @Inject private PlayerData playerData;
  private ProjectXericPanel panel;

  private Map<Integer, ClogItem> allItemsById;
  private Map<String, ClogItem> allItemsByName;
  private Map<Integer, CombatAchievement> allCombatAchievements;
  private Map<Integer, Task> allTasks;
  private int ticksTilClientReady;
  private int ticksTilUpdate;
  private Set<TaskType> pendingUpdates;

  @Override
  protected void startUp() {
    panel = injector.getInstance(ProjectXericPanel.class);
    ticksTilClientReady = 2;
    ticksTilUpdate = 0;
    pendingUpdates = new HashSet<>();
    panel.startUp();
    SwingUtilities.invokeLater(() -> panel.refresh(playerData, allTasks));
    // handle login as soon as possible from client in case
    // the player is logged in when installing or starting the plugin
    clientThread.invokeLater(
        () -> {
          if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client))
            handleLogin();
        });
    log.info("{} started!", ProjectXericConfig.PLUGIN_NAME);
  }

  @Override
  protected void shutDown() {
    playerData.reset();
    ticksTilClientReady = 2;
    ticksTilUpdate = 0;
    pendingUpdates = null;
    allItemsById = null;
    allItemsByName = null;
    allCombatAchievements = null;
    allTasks = null;
    panel.shutDown();
    log.info("{} stopped!", ProjectXericConfig.PLUGIN_NAME);
  }

  private void handleLogin() {
    playerData.login(
        client.getLocalPlayer().getName(),
        AccountType.fromVarbValue(client.getVarbitValue(VarbitID.IRONMAN)));
    playerData.setSlayerException(config.slayer());
    // synchronous operations that can be handled directly on client thread
    updateClientCache();
    checkClogChatMessageEnabled();
    playerData.getCollectionLog().loadFromRSProfile(client, configManager, gson);
    loadCombatAchievements();
    loadSkillLevels();
    loadQuests();
    loadAchievementDiaries();
    // asynchronous operations
    loadHiscoresAsync()
        .thenCompose(unused -> updateTaskCacheAsync())
        .thenRun(
            () -> {
              playerData.loadTasksFromRSProfile(allTasks);
              scheduleUpdate(0, Set.of(TaskType.values()));
              SwingUtilities.invokeLater(() -> panel.refresh(playerData, allTasks));
            });
  }

  private void handleLogout() {
    playerData.logout();
    ticksTilClientReady = 2;
    // reset whether the clog interface has been opened on logout in case
    // the player leaves this client open and then obtains an item on another
    // client before logging in again to this one
    playerData.getCollectionLog().setInterfaceOpened(false);
    playerData.getCollectionLog().saveToRSProfile(configManager, gson);
    playerData.saveTasksToRSProfile();
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
    boolean updated = updateTaskCompletions();
    if (updated) {
      SwingUtilities.invokeLater(() -> panel.refresh(playerData, allTasks));
    }
  }

  @Subscribe
  void onGameStateChanged(GameStateChanged event) {
    if (!playerData.isLoggedIn() && event.getGameState() == GameState.LOGGED_IN) {
      clientThread.invokeLater(
          () -> {
            if (ticksTilClientReady > 0) return false;
            if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client))
              handleLogin();
            return true;
          });
    } else if (playerData.isLoggedIn() && event.getGameState() == GameState.LOGIN_SCREEN) {
      handleLogout();
    } else if (event.getGameState() == GameState.HOPPING) {
      // wait 2 ticks after hopping before checking varbits and statchanges
      ticksTilClientReady = 2;
    }
  }

  @Subscribe
  void onChatMessage(ChatMessage event) {
    if (event.getType() != ChatMessageType.GAMEMESSAGE) return;
    String message = Text.removeTags(event.getMessage());
    Matcher caMatcher = COMBAT_TASK_REGEX.matcher(message);
    if (caMatcher.matches()) {
      int id = Integer.parseInt(caMatcher.group(ID_GROUP).replace(",", ""));
      playerData.getCombatAchievements().put(id, allCombatAchievements.get(id));
      scheduleUpdate(0, Set.of(TaskType.CA));
      return;
    }
    Matcher clogMatcher = CLOG_REGEX.matcher(message);
    if (clogMatcher.matches()) {
      String itemName = clogMatcher.group(NAME_GROUP);
      ClogItem clogItem = allItemsByName.get(itemName);
      playerData.getCollectionLog().add(clogItem);
      scheduleUpdate(0, Set.of(TaskType.CLOG));
      return;
    }
    Matcher diaryMatcher = DIARY_REGEX.matcher(message);
    if (diaryMatcher.matches()) {
      loadAchievementDiaries();
      scheduleUpdate(0, Set.of(TaskType.DIARY));
      return;
    }
    Matcher hiscoreMatcher = KC_REGEX.matcher(message);
    if (hiscoreMatcher.matches()) {
      String name = HiscoreTask.fixBossName(hiscoreMatcher.group(NAME_GROUP));
      int count = Integer.parseInt(hiscoreMatcher.group(COUNT_GROUP).replace(",", ""));
      playerData.getHiscores().put(name, new Hiscore(name, count));
      scheduleUpdate(0, Set.of(TaskType.HISCORE));
      return;
    }
    Matcher clueMatcher = CLUE_REGEX.matcher(message);
    if (clueMatcher.matches()) {
      String name = HiscoreTask.fixBossName(clueMatcher.group(NAME_GROUP));
      int count = Integer.parseInt(clueMatcher.group(COUNT_GROUP).replace(",", ""));
      playerData.getHiscores().put(name, new Hiscore(name, count));
      scheduleUpdate(0, Set.of(TaskType.HISCORE));
      return;
    }
    Matcher delveMatcher = DELVE_KC_REGEX.matcher(message);
    if (delveMatcher.matches()) {
      String name = HiscoreSkill.DOOM_OF_MOKHAIOTL.getName();
      int count = Integer.parseInt(delveMatcher.group(COUNT_GROUP).replace(",", ""));
      playerData.getHiscores().put(name, new Hiscore(name, count));
      scheduleUpdate(0, Set.of(TaskType.HISCORE));
      return;
    }
    Matcher questMatcher = QUEST_REGEX.matcher(message);
    if (questMatcher.matches()) {
      loadQuests();
      scheduleUpdate(0, Set.of(TaskType.QUEST));
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
      Hiscore gloryHiscore =
          playerData.getHiscores().computeIfAbsent(gloryName, key -> new Hiscore(key, 0));
      if (gloryHiscore.getCount() < count) {
        gloryHiscore.setCount(count);
        scheduleUpdate(0, Set.of(TaskType.HISCORE));
      }
    }
  }

  @Subscribe
  void onStatChanged(StatChanged event) {
    if (ticksTilClientReady > 0) return;
    SkillLevel skillLevel =
        playerData.getLevels().computeIfAbsent(event.getSkill(), key -> new SkillLevel(key, 0, 1));
    if (skillLevel.getXp() < event.getXp()) {
      skillLevel.setXp(event.getXp());
      skillLevel.setLevel(event.getLevel());
      // only refresh level stats every 6 ticks
      scheduleUpdate(6, Set.of(TaskType.LEVEL));
    }
  }

  @Subscribe
  void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() != COLLECTION_LOG_TRANSMIT_SCRIPT_ID) return;
    int itemId = (int) event.getScriptEvent().getArguments()[1];
    ClogItem clogItem = allItemsById.get(itemId);
    playerData.getCollectionLog().add(clogItem);
    // schedule an update in 4 ticks to let all items load
    // this method is called for each new item, but delay starts after first call
    scheduleUpdate(2, Set.of(TaskType.CLOG));
  }

  @Subscribe
  void onScriptPostFired(ScriptPostFired event) {
    if (playerData.getCollectionLog().isInterfaceOpened()
        || event.getScriptId() != COLLECTION_LOG_SETUP_SCRIPT_ID) return;
    // taken from WikiSync, not really sure what script is being run,
    // but it appears that simulating a click on the Search button
    // loads the script that checks for obtained clog items
    client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
    client.runScript(2240);
    playerData.getCollectionLog().setInterfaceOpened(true);
  }

  @Subscribe
  void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.CONFIG_GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.CONFIG_KEY_SLAYER)) {
      playerData.setSlayerException(Boolean.parseBoolean(event.getNewValue()));
      SwingUtilities.invokeLater(() -> panel.refresh(playerData, allTasks));
    }
  }

  @Subscribe
  void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      for (RuneScapeProfile rsProfile : configManager.getRSProfiles()) {
        String profileKey = rsProfile.getKey();
        configManager.unsetConfiguration(
            ProjectXericConfig.CONFIG_GROUP, profileKey, ProjectXericConfig.CONFIG_KEY_TASKS);
        configManager.unsetConfiguration(
            ProjectXericConfig.CONFIG_GROUP, profileKey, ProjectXericConfig.CONFIG_KEY_CLOG);
      }
      try {
        shutDown();
        startUp();
      } catch (Exception err) {
        throw new RuntimeException(err);
      }
    }
  }

  private boolean updateTaskCompletions() {
    log.debug(
        "Called updateTaskCompletions with: {}",
        pendingUpdates.stream().map(TaskType::getName).collect(Collectors.toSet()));
    Set<Task> allTasksByType =
        allTasks.values().stream()
            .filter(task -> pendingUpdates.contains(task.getType()))
            .collect(Collectors.toSet());
    Set<Task> remainingTasks = Sets.difference(allTasksByType, playerData.getTasks());
    boolean updated = false;
    for (Task task : remainingTasks) {
      if (task.isCompleted(playerData)) {
        updated = true;
        playerData.getTasks().add(task);
        if (config.chatMessages()) {
          client.addChatMessage(
              ChatMessageType.GAMEMESSAGE,
              "",
              String.format(
                  "Xeric task completed for %d point%s: %s.",
                  task.getTier(),
                  task.getTier() > 1 ? "s" : "",
                  ColorUtil.wrapWithColorTag(task.getName(), Color.decode("#006600"))),
              null);
        }
      }
    }
    pendingUpdates.clear();
    return updated;
  }

  private void loadCombatAchievements() {
    // load player combat achievements
    Set<CombatAchievement> remainingCombatAchievements =
        Sets.difference(
            new HashSet<>(allCombatAchievements.values()),
            new HashSet<>(playerData.getCombatAchievements().values()));
    for (CombatAchievement combatAchievement : remainingCombatAchievements) {
      client.runScript(4834, combatAchievement.getId());
      boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;
      if (unlocked) {
        playerData.getCombatAchievements().put(combatAchievement.getId(), combatAchievement);
      }
    }
  }

  private void loadSkillLevels() {
    // load player levels
    for (Skill skill : Skill.values()) {
      SkillLevel skillLevel =
          playerData.getLevels().computeIfAbsent(skill, key -> new SkillLevel(key, 0, 1));
      int skillXp = client.getSkillExperience(skill);
      if (skillLevel.getXp() < skillXp) {
        skillLevel.setXp(skillXp);
        skillLevel.setLevel(client.getRealSkillLevel(skill));
      }
    }
  }

  private void loadQuests() {
    // load player quests
    for (Quest quest : QuestProgress.TRACKED_QUESTS) {
      playerData.getQuests().put(quest, new QuestProgress(quest, quest.getState(client)));
    }
  }

  private void loadAchievementDiaries() {
    // load player diaries
    for (AchievementDiary diary : DiaryProgress.TRACKED_DIARIES) {
      playerData
          .getDiaries()
          .put(
              diary,
              new DiaryProgress(
                  diary,
                  client.getVarbitValue(diary.getCountVarb()),
                  client.getVarbitValue(diary.getCompletedVarb()) == 1));
    }
  }

  private CompletableFuture<Void> loadHiscoresAsync() {
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
            for (HiscoreSkill hiscoreSkill : Hiscore.HISCORE_SKILLS) {
              String name = hiscoreSkill.getName();
              int count = Math.max(0, hiscoreResult.getSkill(hiscoreSkill).getLevel());
              if (count == 0) {
                // fallback to checking killcount config if player not ranked on hiscore
                Integer configCount =
                    configManager.getRSProfileConfiguration(
                        "killcount", name.toLowerCase(), Integer.class);
                if (configCount != null) count = configCount;
              }
              playerData.getHiscores().put(name, new Hiscore(name, count));
            }
            future.complete(null);
          } catch (IOException err) {
            future.completeExceptionally(err);
          }
        });
    return future;
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

  private CompletableFuture<Void> updateTaskCacheAsync() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    HttpUrl url =
        new HttpUrl.Builder()
            .scheme("https")
            .host("api.projectxeric.com")
            .addPathSegment("v1")
            .addPathSegment(ProjectXericConfig.CONFIG_KEY_TASKS)
            .build();
    Request request =
        new Request.Builder()
            .get()
            .url(url)
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
            .build();
    httpClient
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(@NonNull Call call, @NonNull IOException err) {
                log.warn("api call to tasks failed: {}", err.getMessage());
                future.completeExceptionally(err);
              }

              @Override
              public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (Response res = response) {
                  ResponseBody body = res.body();
                  if (body == null) {
                    throw new IOException("response body empty");
                  }
                  String bodyString = body.string();
                  if (!res.isSuccessful()) {
                    JsonObject json = gson.fromJson(bodyString, JsonObject.class);
                    throw new IOException(json.get("error").getAsString());
                  }
                  Type type = new TypeToken<Set<Task>>() {}.getType();
                  Set<Task> tasks = gson.fromJson(bodyString, type);
                  allTasks =
                      tasks.stream().collect(Collectors.toMap(Task::getId, Function.identity()));
                  future.complete(null);
                } catch (IOException | JsonParseException err) {
                  onFailure(call, new IOException(err));
                }
              }
            });
    return future;
  }

  private void checkClogChatMessageEnabled() {
    // warn if player doesn't have collection log chat drops enabled
    if (client.getVarbitValue(VarbitID.OPTION_COLLECTION_NEW_ITEM) != 1) {
      client.addChatMessage(
          ChatMessageType.GAMEMESSAGE,
          "",
          String.format(
              "[%s] Warning: %s",
              ColorUtil.wrapWithColorTag(ProjectXericConfig.PLUGIN_NAME, Color.decode("#0E5816")),
              ColorUtil.wrapWithColorTag(
                  "Tasks will not update properly unless you enable the game setting:"
                      + " Collection log - New addition notification",
                  Color.RED)),
          null);
    }
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
    // Some items with data saved on them have replacements to fix a duping issue (satchels,
    // flamtaer bag)
    // Enum 3721 contains a mapping of the item ids to replace -> ids to replace them with
    EnumComposition itemReplacementMapping = client.getEnum(ITEM_REPLACEMENT_MAPPING_ENUM_ID);
    for (int badItemId : itemReplacementMapping.getKeys()) clogItems.remove(badItemId);
    for (int goodItemId : itemReplacementMapping.getIntVals()) clogItems.add(goodItemId);
    // remove duplicate Prospector outfit
    for (int prospectorItemId : UNUSED_PROSPECTOR_ITEM_IDS) clogItems.remove(prospectorItemId);
    return clogItems;
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

  private void scheduleUpdate(int tickDelay, Set<TaskType> taskTypes) {
    ticksTilUpdate = ticksTilUpdate > 0 ? ticksTilUpdate : Math.max(0, tickDelay);
    pendingUpdates.addAll(taskTypes);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  Gson provideGson(Gson gson) {
    RuntimeTypeAdapterFactory<Task> taskTypeAdapterFactory =
        RuntimeTypeAdapterFactory.of(Task.class, "type", true)
            .registerSubtype(CATask.class, TaskType.CA.getName())
            .registerSubtype(ClogTask.class, TaskType.CLOG.getName())
            .registerSubtype(DiaryTask.class, TaskType.DIARY.getName())
            .registerSubtype(HiscoreTask.class, TaskType.HISCORE.getName())
            .registerSubtype(LevelTask.class, TaskType.LEVEL.getName())
            .registerSubtype(QuestTask.class, TaskType.QUEST.getName());
    return gson.newBuilder()
        .disableHtmlEscaping()
        .registerTypeAdapterFactory(taskTypeAdapterFactory)
        .create();
  }
}
