package io.septem150.xeric;

import com.google.gson.Gson;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.data.DataManager;
import io.septem150.xeric.data.PlayerData;
import io.septem150.xeric.panel.ProjectXericPanel;
import io.septem150.xeric.task.LocalTaskStore;
import io.septem150.xeric.task.Task;
import io.septem150.xeric.task.TaskStore;
import io.septem150.xeric.task.TaskTypeAdapter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * Project Xeric plugin.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@Slf4j
@PluginDescriptor(name = "Project Xeric")
public class ProjectXericPlugin extends Plugin {
  private static final int CLOG_TOP_TABS_ENUM_ID = 2102;
  private static final int CLOG_SUB_TABS_PARAM_ID = 683;
  private static final int CLOG_SUB_TAB_ITEMS_PARAM_ID = 690;
  private static final int ITEM_REPLACEMENT_MAPPING_ENUM_ID = 3721;
  private static final int[] UNUSED_PROSPECTOR_ITEM_IDS = new int[] {29472, 29474, 29476, 29478};

  private static final int EASY_TIER_ENUM_ID = 3981;
  private static final int MEDIUM_TIER_ENUM_ID = 3982;
  private static final int HARD_TIER_ENUM_ID = 3983;
  private static final int ELITE_TIER_ENUM_ID = 3984;
  private static final int MASTER_TIER_ENUM_ID = 3985;
  private static final int GM_TIER_ENUM_ID = 3986;
  private static final int CA_STRUCT_TIER_PARAM_ID = 1310;
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

  private static final int DIARY_KOUREND_EASY_COUNT = 7933;
  private static final int DIARY_KOUREND_MEDIUM_COUNT = 7934;
  private static final int DIARY_KOUREND_HARD_COUNT = 7935;
  private static final int DIARY_KOUREND_ELITE_COUNT = 7936;

  private static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
  private static final int COLLECTION_LOG_TRANSMIT_SCRIPT_ID = 4100;

  @Inject private Client client;
  @Inject private ClientThread clientThread;
  @Inject private ScheduledExecutorService executor;
  @Inject private ProjectXericConfig config;
  @Inject private DataManager dataManager;

  @Inject
  @Named("xericGson")
  private Gson gson;

  private ProjectXericPanel panel;

  private Set<Integer> allClogItems;
  private boolean collectionLogOpened;

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    panel = injector.getInstance(ProjectXericPanel.class);
    panel.init();
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
    panel.stop();
    //    playerManager.unloadPlayer();
    dataManager.clearPlayerData();
  }

  @Subscribe
  public void onCommandExecuted(CommandExecuted event) {
    if (event.getCommand().equals("xeric")) {
      log.info(dataManager.getPlayerJson());
      log.info("Player has obtained {} points", dataManager.getPlayerPoints());
      dataManager.serializeTasks();
      dataManager.clearRSProfileData();
    }
  }

  @Subscribe
  public void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() == COLLECTION_LOG_TRANSMIT_SCRIPT_ID) {
      int itemId = (int) event.getScriptEvent().getArguments()[1];
      List<Integer> clogItems = dataManager.getPlayerData().getClogItems();
      if (!clogItems.contains(itemId)) {
        clogItems.add(itemId);
        clogItems.sort(Integer::compareTo);
      }
    }
  }

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() == COLLECTION_LOG_SETUP_SCRIPT_ID && !collectionLogOpened) {
      collectionLogOpened = true;
      // taken from WikiSync, not really sure what script is being run,
      // but it appears that simulating a click on the Search button
      // loads the script that checks for clog items
      client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
      client.runScript(2240);
    }
  }

  /**
   * Action to perform on GameStateChanged event.
   *
   * @param event GameStateChanged the propagated event
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged event) {
    switch (event.getGameState()) {
      case LOGGED_IN:
        clientThread.invokeLater(
            () -> {
              if (client.getLocalPlayer() == null || client.getLocalPlayer().getName() == null) {
                return false;
              }
              // load local saved player data
              // check if data is stale and update from client if necessary
              PlayerData rsProfilePlayerData = dataManager.getRSProfileData();
              if (rsProfilePlayerData != null) {
                dataManager.setPlayerData(rsProfilePlayerData);
              }
              if (dataManager.isPlayerDataFresh()) {
                // no need to update player data if obtained from RSProfile and data is not stale
                if (dataManager.getPlayerData().getClogItems().isEmpty()) {
                  collectionLogOpened = false;
                }
                SwingUtilities.invokeLater(panel::reload);
                return true;
              }
              collectionLogOpened = false;
              dataManager.clearPlayerData();
              final PlayerData playerData = dataManager.getPlayerData();
              playerData.setUsername(client.getLocalPlayer().getName());
              // player stats don't load for a while, invoke later
              clientThread.invokeLater(
                  () -> {
                    Map<String, Integer> levels =
                        Arrays.stream(Skill.values())
                            .collect(Collectors.toMap(Skill::getName, client::getRealSkillLevel));
                    if (levels.containsValue(0)) {
                      return false;
                    }
                    playerData.setLevels(levels);
                    SwingUtilities.invokeLater(panel::reload);
                    return true;
                  });
              for (Quest quest : Quest.values()) {
                QuestState questState = quest.getState(client);
                if (questState == QuestState.NOT_STARTED) {
                  continue;
                }
                playerData.getQuests().put(String.valueOf(quest.getId()), questState.ordinal());
              }
              Map<String, Integer> diaries = playerData.getDiaries();
              diaries.put("easy", client.getVarbitValue(DIARY_KOUREND_EASY_COUNT));
              diaries.put("medium", client.getVarbitValue(DIARY_KOUREND_MEDIUM_COUNT));
              diaries.put("hard", client.getVarbitValue(DIARY_KOUREND_HARD_COUNT));
              diaries.put("elite", client.getVarbitValue(DIARY_KOUREND_ELITE_COUNT));

              if (allClogItems == null || allClogItems.isEmpty()) {
                allClogItems = requestAllClogItems();
              }
              playerData.getCaTasks().addAll(getCATaskCompletions().keySet());
              SwingUtilities.invokeLater(panel::reload);

              dataManager.setRSProfileData();
              return true;
            });
        break;
      case LOGIN_SCREEN:
        if (dataManager.getPlayerData().getUsername() != null) {
          dataManager.setRSProfileData();
        }
        break;
      default:
    }
  }

  /*
   TODO: Move this out of Plugin into its own class, maybe DataManager but then again DataManager already has loads of responsibilities.
  */

  /**
   * Parse the enums and structs in the cache to figure out which item ids exist in the collection
   * log.
   *
   * @return a {@link Set} containing the IDs of all collection log items.
   * @see <a
   *     href="https://github.com/weirdgloop/WikiSync/blob/master/src/main/java/com/andmcadams/wikisync/WikiSyncPlugin.java">WikiSyncPlugin</a>
   */
  public Set<Integer> requestAllClogItems() {
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

  /**
   * Gets all player's completed CA Tasks and maps them to their point value.
   *
   * @return a map of CA Task ID to the task's point value.
   * @see <a
   *     href="https://discord.com/channels/301497432909414422/419891709883973642/1347233676945260684">RuneLite
   *     Discord post</a> by @abex
   */
  public Map<Integer, Integer> getCATaskCompletions() {
    Map<Integer, Integer> caTaskIdsToPoints = new HashMap<>();
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
        // or its tier/points value
        int taskTier = caTierStruct.getIntValue(CA_STRUCT_TIER_PARAM_ID);
        // we can use the cs2 vm to invoke script 4834 to do the lookup for us
        // client.runScript(4834, id);
        // boolean unlocked = client.getIntStack()[client.getIntStackSize() - 1] != 0;

        // or we can reimplement it ourselves
        // from script 4834
        boolean unlocked = isCATaskComplete(taskId);

        if (unlocked) {
          caTaskIdsToPoints.put(taskId, taskTier);
        }
      }
    }
    return caTaskIdsToPoints;
  }

  /**
   * Script 4834 but implemented in Java.
   *
   * @param taskId CA Task ID to check for completion.
   * @return whether the CA is complete (true) or not (false).
   */
  public boolean isCATaskComplete(int taskId) {
    return (client.getVarpValue(SCRIPT_4834_VARP_IDS[taskId / 32]) & (1 << (taskId % 32))) != 0;
  }

  @Subscribe
  public void onGameTick(GameTick gameTick) {}

  @Override
  public void configure(Binder binder) {
    binder.bind(TaskStore.class).to(LocalTaskStore.class);
    super.configure(binder);
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  @Provides
  @Named("xericGson")
  public Gson provideGson(Gson gson) {
    return gson.newBuilder().registerTypeAdapter(Task.class, new TaskTypeAdapter()).create();
  }
}
