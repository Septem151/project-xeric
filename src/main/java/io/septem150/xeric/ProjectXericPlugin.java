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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
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
import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.hiscore.HiscoreClient;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.Text;

/**
 * Project Xeric plugin.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@Slf4j
@PluginDescriptor(name = "Project Xeric")
public final class ProjectXericPlugin extends Plugin {
  private static final Pattern CLOG_REGEX =
      Pattern.compile("New item added to your collection log: (.*)");
  private static final Pattern COMBAT_TASK_REGEX =
      Pattern.compile("Congratulations, you've completed an? \\w+ combat task:.*");
  private static final Pattern QUEST_REGEX =
      Pattern.compile("Congratulations, you've completed a quest:.*");
  private static final Pattern DIARY_REGEX =
      Pattern.compile(
          "Well done! You have completed an? \\w+ task in the .* area\\. Your Achievement"
              + " Diary has been updated");
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
  private static final int CLOG_TOP_TABS_ENUM_ID = 2102;
  private static final int CLOG_SUB_TABS_PARAM_ID = 683;
  private static final int CLOG_SUB_TAB_ITEMS_PARAM_ID = 690;
  private static final int ITEM_REPLACEMENT_MAPPING_ENUM_ID = 3721;
  private static final int[] UNUSED_PROSPECTOR_ITEM_IDS = new int[] {29472, 29474, 29476, 29478};

  @Inject private Client client;
  @Inject private ClientThread clientThread;
  @Inject private ProjectXericConfig config;
  @Inject private ConfigManager configManager;
  @Inject private PlayerInfo playerInfo;
  @Inject private HiscoreClient hiscoreClient;
  @Inject private ScheduledExecutorService executor;
  @Inject private TaskStore taskStore;
  @Inject private ItemManager itemManager;

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

  private String obtainedItemName;
  private Multiset<Integer> inventoryItems;
  private Set<Integer> caTaskIds;
  private Set<Integer> clogItemIds;

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
            if (clogItemIds == null) {
              clogItemIds = requestAllClogItems();
              log.debug("clog item IDs: {}", gson.toJson(clogItemIds));
            }
            if (caTaskIds == null) {
              caTaskIds = requestAllCaTaskIds();
              log.debug("ca task IDs: {}", gson.toJson(caTaskIds));
            }
          }
        });
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    reset();
    clogItemIds = null;
    caTaskIds = null;
    panel.stop();
    //    sessionManager.reset();
  }

  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    if (GameState.LOGGED_IN == event.getGameState()) {
      if (!isValidWorldType()) {
        log.debug("Not logged in to main worlds, ignoring");
        reset();
        return;
      }
      long account = client.getAccountHash();
      if (account != lastAccount) {
        log.debug("Account change: {} -> {}", lastAccount, account);
        reset();
        lastAccount = account;
        if (clogItemIds == null) {
          clogItemIds = requestAllClogItems();
          log.debug("clog item IDs: {}", gson.toJson(clogItemIds));
        }
        if (caTaskIds == null) {
          caTaskIds = requestAllCaTaskIds();
          log.debug("ca task IDs: {}", gson.toJson(caTaskIds));
        }
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
      for (int caTaskId : caTaskIds) {
        boolean unlocked =
            (client.getVarpValue(SCRIPT_4834_VARP_IDS[caTaskId / 32]) & (1 << (caTaskId % 32)))
                != 0;
        if (unlocked) {
          StructComposition struct = client.getStructComposition(caTaskId);
          CombatAchievement combatAchievement = new CombatAchievement();
          combatAchievement.setId(caTaskId);
          combatAchievement.setName(struct.getStringValue(CA_STRUCT_NAME_PARAM_ID));
          combatAchievement.setPoints(struct.getIntValue(CA_STRUCT_TIER_PARAM_ID));
          combatAchievements.add(combatAchievement);
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
      log.debug("Checking tasks");
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

  @Subscribe
  public void onStatChanged(StatChanged event) {
    if (!(generalLoaded && hiscoresLoaded && levelsLoaded)) {
      return;
    }
    for (Level level : playerInfo.getLevels()) {
      if (level.getName().equals(event.getSkill().getName())) {
        if (level.getValue() != event.getLevel()) {
          log.debug(
              "Gained a level: {} : {} -> {}", level.getName(), level.getValue(), event.getLevel());
          level.setValue(event.getLevel());
          checkTasks = true;
        }
        break;
      }
    }
  }

  @Subscribe
  public void onChatMessage(ChatMessage event) {
    if (event.getType() != ChatMessageType.GAMEMESSAGE) {
      return;
    }
    String message = Text.removeTags(event.getMessage());
    Matcher clogMatcher = CLOG_REGEX.matcher(message);
    if (clogMatcher.matches()) {
      log.debug(clogMatcher.group(1));
      log.debug(Text.removeTags(clogMatcher.group(1)));

      obtainedItemName = Text.removeTags(clogMatcher.group(1));

      ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
      if (inventory == null) {
        obtainedItemName = null;
        inventoryItems = null;
        return;
      }

      // Get inventory prior to onItemContainerChanged event
      Arrays.stream(inventory.getItems())
          .forEach(item -> inventoryItems.add(item.getId(), item.getQuantity()));

      // Defer to onItemContainerChanged or onLootReceived
      return;
    }
    Matcher caTaskMatcher = COMBAT_TASK_REGEX.matcher(message);
    if (caTaskMatcher.matches()) {
      log.debug("Updating CA's");
      for (int caTaskId : caTaskIds) {
        boolean unlocked =
            (client.getVarpValue(SCRIPT_4834_VARP_IDS[caTaskId / 32]) & (1 << (caTaskId % 32)))
                != 0;
        if (unlocked) {
          StructComposition struct = client.getStructComposition(caTaskId);
          CombatAchievement combatAchievement = new CombatAchievement();
          combatAchievement.setId(caTaskId);
          combatAchievement.setName(struct.getStringValue(CA_STRUCT_NAME_PARAM_ID));
          combatAchievement.setPoints(struct.getIntValue(CA_STRUCT_TIER_PARAM_ID));
          if (!playerInfo.getCombatAchievements().contains(combatAchievement)) {
            playerInfo.getCombatAchievements().add(combatAchievement);
            checkTasks = true;
          }
        }
      }
      return;
    }
    Matcher questMatcher = QUEST_REGEX.matcher(message);
    if (questMatcher.matches()) {
      log.debug("Updating Quests");
      playerInfo.setQuests(
          QuestProgress.trackedQuests.stream()
              .map(quest -> QuestProgress.from(client, quest))
              .collect(Collectors.toList()));
      checkTasks = true;
      return;
    }
    Matcher diaryMatcher = DIARY_REGEX.matcher(message);
    if (diaryMatcher.matches()) {
      log.debug("Updating diaries");
      playerInfo.setDiaries(
          DiaryProgress.trackedDiaries.stream()
              .map(diary -> DiaryProgress.from(client, diary))
              .collect(Collectors.toList()));
      checkTasks = true;
    }
  }

  @Subscribe
  private void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
    if (itemContainerChanged.getContainerId() != InventoryID.INVENTORY.getId()) {
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
      if (client.getWidget(ComponentID.CLUESCROLL_REWARD_ITEM_CONTAINER) != null) {
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
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      configManager.unsetRSProfileConfiguration(
          ProjectXericConfig.GROUP, ProjectXericConfig.DATA_KEY);
      SwingUtilities.invokeLater(panel::reload);
      ChatMessageBuilder cmb = new ChatMessageBuilder();
      cmb.append("Congratulations, you've completed a grandmaster combat task: ");
      cmb.append(Color.decode("#2a651e"), "Defence Matters");
      cmb.append(" (6 points).");
      client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", cmb.build(), null, true);
      cmb = new ChatMessageBuilder();
      cmb.append(
          Color.decode("#dc143c"),
          "Well done! You have completed an easy task in the Lumbridge & Draynor area. Your"
              + " Achievement Diary has been updated");
      client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", cmb.build(), null, true);
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

  private void updateObtainedItem(ItemStack itemStack) {
    log.debug("Called updateObtainedItem");
    if (clogItemIds.contains(itemStack.getId())) {
      ClogItem clogItem = ClogItem.from(client, itemStack.getId());
      playerInfo.getCollectionLog().add(clogItem);
      log.debug("Item added to player clog: {}", clogItem.getName());
      // delay by a tick in case this causes lag
      updateClog = 2;
    }
    obtainedItemName = null;
    inventoryItems = HashMultiset.create();
  }

  private Set<Integer> requestAllCaTaskIds() {
    Set<Integer> caTaskIds = new HashSet<>();
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
        caTaskIds.add(taskId);
        // we can use the cs2 vm to invoke script 4834 to do the lookup for us
        // client.runScript(4834, id);
        // boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;

        // or we can reimplement it ourselves
        // from script 4834
      }
    }
    return caTaskIds;
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
