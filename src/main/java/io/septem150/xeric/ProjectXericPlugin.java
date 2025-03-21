package io.septem150.xeric;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Provides;
import io.septem150.xeric.clog.ClogItem;
import io.septem150.xeric.clog.ClogManager;
import io.septem150.xeric.clog.ClogPage;
import io.septem150.xeric.clog.ClogStore;
import io.septem150.xeric.clog.InMemoryClogStore;
import io.septem150.xeric.task.InMemoryTaskStore;
import io.septem150.xeric.task.TaskManager;
import io.septem150.xeric.task.TaskStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

/**
 * Project Xeric plugin.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
@Slf4j
@PluginDescriptor(name = "Project Xeric")
public class ProjectXericPlugin extends Plugin {
  @Inject private Client client;
  @Inject private ClientThread clientThread;
  @Inject private ItemManager itemManager;
  @Inject private ProjectXericConfig config;
  @Inject private TaskManager taskManager;
  @Inject private ClogManager clogManager;

  private final List<ClogPage> clogPages = new ArrayList<>();
  private boolean clogItemsInitialized = false;
  private boolean isPohOwner = false;
  private static final int COLLECTION_LOG_ACTIVE_TAB_VARBIT_ID = 6905;
  private static final int ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID = 12061;
  private static final Pattern ADVENTURE_LOG_TITLE_PATTERN =
      Pattern.compile("The Exploits of (.+)");

  @Override
  public void configure(Binder binder) {
    binder.bind(ClogStore.class).to(InMemoryClogStore.class);
    binder.bind(TaskStore.class).to(InMemoryTaskStore.class);
    super.configure(binder);
  }

  @Override
  protected void startUp() throws Exception {
    log.info("Project Xeric started!");
    taskManager.getAllTasks().forEach(task -> log.info(task.toString()));
    clogManager.getAllItems().forEach(clogItem -> log.info(clogItem.toString()));
  }

  @Override
  protected void shutDown() throws Exception {
    log.info("Project Xeric stopped!");
  }

  private boolean isValidWorldType() {
    List<WorldType> invalidTypes =
        ImmutableList.of(
            WorldType.DEADMAN,
            WorldType.NOSAVE_MODE,
            WorldType.SEASONAL,
            WorldType.TOURNAMENT_WORLD);

    for (WorldType worldType : invalidTypes) {
      if (client.getWorldType().contains(worldType)) {
        return false;
      }
    }

    return true;
  }

  private Widget getActiveTab() {
    Widget tabsWidget = client.getWidget(ComponentID.COLLECTION_LOG_TABS);
    if (tabsWidget == null) {
      return null;
    }

    int tabIndex = client.getVarbitValue(COLLECTION_LOG_ACTIVE_TAB_VARBIT_ID);
    return tabsWidget.getStaticChildren()[tabIndex];
  }

  @Subscribe
  public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
    if (widgetLoaded.getGroupId() == InterfaceID.ADVENTURE_LOG) {
      Widget adventureLog = client.getWidget(ComponentID.ADVENTURE_LOG_CONTAINER);
      if (adventureLog == null) {
        return;
      }

      // Children are rendered on tick after widget load. Invoke later to prevent null children on
      // adventure log widget
      clientThread.invokeLater(
          () -> {
            Matcher adventureLogUser =
                ADVENTURE_LOG_TITLE_PATTERN.matcher(
                    Objects.requireNonNull(adventureLog.getChild(1)).getText());
            if (adventureLogUser.find()) {
              isPohOwner = adventureLogUser.group(1).equals(client.getLocalPlayer().getName());
            }
          });
    }
  }

  @Subscribe
  public void onScriptPostFired(ScriptPostFired scriptPostFired) {
    if (scriptPostFired.getScriptId() == ScriptID.COLLECTION_DRAW_LIST) {
      clientThread.invokeLater(this::getPage);
    }
  }

  private void getPage() {
    if (!isValidWorldType()) {
      return;
    }

    boolean openedFromAdventureLog =
        client.getVarbitValue(ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID) != 0;
    if (openedFromAdventureLog && !isPohOwner) {
      return;
    }

    Widget activeTab = getActiveTab();
    if (activeTab == null) {
      return;
    }

    String activeTabName = Text.removeTags(activeTab.getName());
    //    CollectionLogTab collectionLogTab = collectionLogManager.getTabByName(activeTabName);
    //    if (collectionLogTab == null) {
    //      return;
    //    }

    Widget pageHead = client.getWidget(ComponentID.COLLECTION_LOG_ENTRY_HEADER);
    if (pageHead == null) {
      return;
    }

    String pageTitle = pageHead.getDynamicChildren()[0].getText();
    //    if (!collectionLogTab.containsPage(pageTitle)) {
    //      return;
    //    }

    ClogPage pageToUpdate = clogManager.getPageByName(pageTitle);
    //    CollectionLogPage pageToUpdate = collectionLogManager.getPageByName(pageTitle);
    if (pageToUpdate == null) {
      return;
    }

    //    int prevObtainedItemCount = pageToUpdate.getObtainedItemCount();

    updatePage(pageHead, pageToUpdate);
  }

  private void updatePage(Widget pageHead, ClogPage pageToUpdate) {
    Widget itemsContainer = client.getWidget(ComponentID.COLLECTION_LOG_ENTRY_ITEMS);
    if (itemsContainer == null) {
      return;
    }

    List<ClogItem> items = pageToUpdate.getItems();

    Widget[] widgetItems = itemsContainer.getDynamicChildren();
    for (Widget widgetItem : widgetItems) {
      String itemName = itemManager.getItemComposition(widgetItem.getItemId()).getMembersName();
      boolean isObtained = widgetItem.getOpacity() == 0;
      int quantity = isObtained ? widgetItem.getItemQuantity() : 0;
      items.stream()
          .filter(clogItem -> clogItem.getId() == widgetItem.getItemId())
          .findFirst()
          .ifPresent(clogItem -> clogItem.setQuantity(quantity));
    }
    log.info(pageToUpdate.toString());

    //    Widget[] children = pageHead.getDynamicChildren();
    //    if (children.length < 3) {
    //      // Page does not have kill count widgets, mark as updated and early return
    //      pageToUpdate.setUpdated(true);
    //      return;
    //    }
    //
    //    List<CollectionLogKillCount> killCounts = pageToUpdate.getKillCounts();
    //    killCounts.clear();
    //
    //    Widget[] killCountWidgets = Arrays.copyOfRange(children, 2, children.length);
    //    for (Widget killCountWidget : killCountWidgets) {
    //      String killCountString = killCountWidget.getText();
    //      CollectionLogKillCount killCount =
    //          CollectionLogKillCount.fromString(killCountString, killCounts.size());
    //      killCounts.add(killCount);
    //    }
    //
    //    pageToUpdate.setUpdated(true);
  }

  /**
   * Action to perform on GameStateChanged event.
   *
   * @param gameStateChanged GameStateChanged the propagated event
   */
  @Subscribe
  public void onGameStateChanged(GameStateChanged gameStateChanged) {
    if (!isValidWorldType()) {
      return;
    }
    if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
      client.addChatMessage(
          ChatMessageType.GAMEMESSAGE, "", "Project Xeric says " + config.greeting(), null);
    }
  }

  @Provides
  ProjectXericConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(ProjectXericConfig.class);
  }

  //  public void initCollectionLog() {
  //    List<Integer> COLLECTION_LOG_TAB_STRUCT_IDS =
  //        ImmutableList.of(
  //            471, // Bosses
  //            472, // Raids
  //            473, // Clues
  //            474, // Minigames
  //            475 // Other
  //            );
  //    int COLLECTION_LOG_TAB_NAME_PARAM_ID = 682;
  //    int COLLECTION_LOG_TAB_ENUM_PARAM_ID = 683;
  //    int COLLECTION_LOG_PAGE_NAME_PARAM_ID = 689;
  //    int COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID = 690;
  //
  //    for (ClogPage clogPage : clogManager.getAllPages()) {
  //      int tabStructId = clogPage.getTabStructId();
  //      int pageStructId = clogPage.getPageStructId();
  //      StructComposition tabStruct = client.getStructComposition(pageStructId);
  //    }
  //
  //    clogPages.clear();
  //    for (Integer structId : COLLECTION_LOG_TAB_STRUCT_IDS) {
  //      StructComposition tabStruct = client.getStructComposition(structId);
  //      String tabName = tabStruct.getStringValue(COLLECTION_LOG_TAB_NAME_PARAM_ID);
  //      int tabEnumId = tabStruct.getIntValue(COLLECTION_LOG_TAB_ENUM_PARAM_ID);
  //      EnumComposition tabEnum = client.getEnum(tabEnumId);
  //
  //      for (Integer pageStructId : tabEnum.getIntVals()) {
  //        StructComposition pageStruct = client.getStructComposition(pageStructId);
  //        String pageName = pageStruct.getStringValue(COLLECTION_LOG_PAGE_NAME_PARAM_ID);
  //        int pageItemsEnumId = pageStruct.getIntValue(COLLECTION_LOG_PAGE_ITEMS_ENUM_PARAM_ID);
  //        EnumComposition pageItemsEnum = client.getEnum(pageItemsEnumId);
  //        ClogPage page = new ClogPage(structId, pageStructId, pageName);
  //        for (Integer pageItemId : pageItemsEnum.getIntVals()) {
  //          ItemComposition itemComposition = itemManager.getItemComposition(pageItemId);
  //          ClogItem item = ClogItem.fromItemComposition(itemComposition);
  //          page.addItem(item);
  //        }
  //        clogPages.add(page);
  //      }
  //    }
  //    clogItemsInitialized = true;
  //  }
}
