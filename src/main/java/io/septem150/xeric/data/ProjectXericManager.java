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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
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
import io.septem150.xeric.data.task.LevelTask;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.panel.PanelUpdate;
import io.septem150.xeric.util.WorldUtil;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InterfaceID.TrailRewardscreen;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ProjectXericManager {
  private static final Pattern COMBAT_TASK_REGEX =
      Pattern.compile("Congratulations, you've completed an? \\w+ combat task:.*");
  private static final Pattern CLOG_REGEX =
      Pattern.compile("New item added to your collection log: (?<item>.*)");
  private static final Pattern DIARY_REGEX =
      Pattern.compile(
          "Well done! You have completed an? \\w+ task in the .* area\\. Your Achievement"
              + " Diary has been updated");
  private static final Pattern KC_REGEX =
      Pattern.compile(
          "Your (?:subdued |completed )?(?<name>.*) (?:kill )?count is: (?<count>\\d+)\\.");
  private static final Pattern DELVE_KC_REGEX =
      Pattern.compile("Deep delves completed: (?<count>\\d+)");
  private static final Pattern DELVE_REGEX =
      Pattern.compile(
          "Delve level: (?<wave>\\d+|8\\+ \\((?<deepWave>\\d+)\\)) duration:"
              + " (?<duration>(?:\\d+:)?\\d+:\\d+)(?:\\.\\d+)?(?: \\(new personal best\\)|\\."
              + " Personal best: (?<pb>(?:\\d+:)?\\d+:\\d+)(?:\\.\\d+)?)");
  private static final Pattern CLUE_REGEX =
      Pattern.compile("You have completed (?<count>\\d+) (?<tier>.*) Treasure Trails?\\.");
  private static final Pattern QUEST_REGEX =
      Pattern.compile("Congratulations, you've completed a quest:.*");

  private final Client client;
  private final ClientThread clientThread;
  private final EventBus eventBus;
  private final ProjectXericConfig config;
  private final ConfigManager configManager;
  private final ItemManager itemManager;
  private final ScheduledExecutorService executor;
  private final HiscoreManager hiscoreManager;
  private final TaskStore taskStore;
  private final Gson gson;

  private long lastAccountId;
  private Set<Integer> remainingCaStructIds;
  private Set<Integer> completedTaskIds;
  private Set<Integer> clogItemIds;
  private Set<Task> remainingTasks;
  private Set<Task> levelTasks;
  private boolean clogOpened;
  private Multiset<Integer> inventoryItems;
  private String obtainedItemName;
  private int updateGeneral;
  private int updateTasks;
  private int updateLevels;
  @Getter private PlayerInfo playerInfo;

  @Inject
  public ProjectXericManager(
      Client client,
      ClientThread clientThread,
      EventBus eventBus,
      ProjectXericConfig config,
      ConfigManager configManager,
      ItemManager itemManager,
      ScheduledExecutorService executor,
      HiscoreManager hiscoreManager,
      TaskStore taskStore,
      @Named("xericGson") Gson gson) {
    this.client = client;
    this.clientThread = clientThread;
    this.eventBus = eventBus;
    this.config = config;
    this.configManager = configManager;
    this.itemManager = itemManager;
    this.executor = executor;
    this.hiscoreManager = hiscoreManager;
    this.taskStore = taskStore;
    this.gson = gson;
  }

  public void startUp() {
    lastAccountId = -1L;
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
    clogItemIds = null;
    completedTaskIds = null;
    levelTasks = null;
    inventoryItems = null;
    obtainedItemName = null;
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
    CompletableFuture.allOf(updatePlayerTasks(), updatePlayerLevels())
        .thenRun(
            () -> {
              updateLevels = 2;
              updateTasks = 2;
              eventBus.post(new PanelUpdate());
            });
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (client.getGameState() == GameState.LOGGED_IN && WorldUtil.isValidWorldType(client)) {
      updateGeneral = 1;
    } else if (client.getGameState() == GameState.LOGIN_SCREEN) {
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
    }
  }

  @Subscribe
  public void onGameTick(GameTick event) {
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
      String name = kcMatcher.group("name");
      if ("Lunar Chest".equals(name)) {
        name += "s";
      } else if ("Hueycoatl".equals(name)) {
        name = "The " + name;
      }
      int count = Integer.parseInt(kcMatcher.group("count"));
      KillCount kc = playerInfo.getKillCounts().getOrDefault(name, null);
      if (kc != null) {
        kc.setCount(count);
        if (updateTasks <= 0) updateTasks = 1;
      }
      return;
    }
    Matcher clueMatcher = CLUE_REGEX.matcher(message);
    if (clueMatcher.matches()) {
      int count = Integer.parseInt(clueMatcher.group("count"));
      String tier = clueMatcher.group("tier");
      KillCount kc =
          playerInfo.getKillCounts().getOrDefault(String.format("Clue Scrolls (%s)", tier), null);
      if (kc != null) {
        kc.setCount(count);
        if (updateTasks <= 0) updateTasks = 1;
      }
      return;
    }
    Matcher clogMatcher = CLOG_REGEX.matcher(message);
    if (clogMatcher.matches()) {
      obtainedItemName = Text.removeTags(clogMatcher.group("item"));

      ItemContainer inventory = client.getItemContainer(InventoryID.INV);
      if (inventory == null) {
        obtainedItemName = null;
        inventoryItems = null;
        return;
      }

      // Get inventory prior to onItemContainerChanged event
      Arrays.stream(inventory.getItems())
          .forEach(item -> inventoryItems.add(item.getId(), item.getQuantity()));

      // Defer to onItemContainerChanged or onLootReceived
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
  public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
    if (itemContainerChanged.getContainerId() != InventoryID.INV) {
      return;
    }

    if (obtainedItemName == null) {
      inventoryItems = HashMultiset.create();
      return;
    }

    if (inventoryItems == null) {
      inventoryItems = HashMultiset.create();
    }

    // Need to build a diff of inventory items prior to item appearing in inventory and current
    // inventory items
    // Necessary to find item that may have non-unique name (Ancient page, decorative armor) that
    // may already be in inventory
    ItemContainer inventory = itemContainerChanged.getItemContainer();
    Multiset<Integer> currentInventoryItems = HashMultiset.create();
    Arrays.stream(inventory.getItems())
        .forEach(item -> currentInventoryItems.add(item.getId(), item.getQuantity()));
    Multiset<Integer> invDiff = Multisets.difference(currentInventoryItems, inventoryItems);

    ItemStack obtainedItemStack = null;
    for (Multiset.Entry<Integer> item : invDiff.entrySet()) {
      ItemComposition itemComp = itemManager.getItemComposition(item.getElement());
      if (itemComp.getName().equals(obtainedItemName)) {
        obtainedItemStack = new ItemStack(item.getElement(), item.getCount());

        break;
      }
    }

    if (obtainedItemStack == null) {
      // Opening clue casket triggers onItemContainerChanged event before clue items
      // appear in inventory. Fall through to onLootReceived to find obtained item(s)
      if (client.getWidget(TrailRewardscreen.ITEMS) != null) {
        return;
      }

      obtainedItemName = null;
      inventoryItems = HashMultiset.create();
      return;
    }

    updateObtainedItem(obtainedItemStack);
  }

  @Subscribe
  public void onLootReceived(LootReceived lootReceived) {
    if (obtainedItemName == null) {
      inventoryItems = null;
      return;
    }

    ItemStack obtainedItem = null;
    Collection<ItemStack> items = lootReceived.getItems();
    for (ItemStack item : items) {
      ItemComposition itemComp = itemManager.getItemComposition(item.getId());
      if (itemComp.getName().equals(obtainedItemName)) {
        obtainedItem = item;
      }
    }

    if (obtainedItem == null) {
      obtainedItemName = null;
      inventoryItems = null;
      return;
    }

    updateObtainedItem(obtainedItem);
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.SLAYER_CONFIG_KEY)) {
      playerInfo.setSlayerException(Boolean.parseBoolean(event.getNewValue()));
      eventBus.post(new PanelUpdate());
    }
  }

  private void updateObtainedItem(ItemStack itemStack) {
    if (clogItemIds.contains(itemStack.getId())) {
      ClogItem clogItem = ClogItem.from(client, itemStack.getId());
      playerInfo.getCollectionLog().add(clogItem);
      if (updateTasks <= 0) updateTasks = 1;
    }
    obtainedItemName = null;
    inventoryItems = HashMultiset.create();
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
                      int points =
                          task.getSlayerPoints() != null && config.slayer()
                              ? task.getSlayerPoints()
                              : task.getTier();
                      client.addChatMessage(
                          ChatMessageType.GAMEMESSAGE,
                          ProjectXericConfig.NAME,
                          String.format(
                              "Xeric task completed for %d point%s: %s.",
                              points,
                              points > 1 ? "s" : "",
                              ColorUtil.wrapWithColorTag(task.getName(), Color.decode("#006600"))),
                          "");
                    });
              }
            }
          }
          if (refresh) {
            playerInfo.setTasks(new ArrayList<>(completedTasks));
            eventBus.post(new PanelUpdate());
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
    clogItemIds = requestAllClogItems();
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

  private CompletableFuture<Void> updatePlayerLevels() {
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
      // so we can iterate the enum to find a bunch of structs
      for (int caTierStructId : caTiersEnum.getIntVals()) {
        StructComposition caTierStruct = client.getStructComposition(caTierStructId);
        // and with the struct we can get info about the ca
        // like its id, which we can use to get if its completed or not
        allCaTaskStructIds.add(caTierStructId);
        // we can use the cs2 vm to invoke script 4834 to do the lookup for us
        // client.runScript(4834, id);
        // boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;

        // or we can reimplement it ourselves
        // from script 4834
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
        String subTabName = clogSubTabStruct.getStringValue(689);
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
}
