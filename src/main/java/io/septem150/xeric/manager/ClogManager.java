package io.septem150.xeric.manager;

import static io.septem150.xeric.data.CollectionLog.CLOG_SUB_TABS_PARAM_ID;
import static io.septem150.xeric.data.CollectionLog.CLOG_SUB_TAB_ITEMS_PARAM_ID;
import static io.septem150.xeric.data.CollectionLog.CLOG_TOP_TABS_ENUM_ID;
import static io.septem150.xeric.data.CollectionLog.COLLECTION_LOG_SETUP_SCRIPT_ID;
import static io.septem150.xeric.data.CollectionLog.COLLECTION_LOG_TRANSMIT_SCRIPT_ID;
import static io.septem150.xeric.data.CollectionLog.ITEM_REPLACEMENT_MAPPING_ENUM_ID;
import static io.septem150.xeric.data.CollectionLog.UNUSED_PROSPECTOR_ITEM_IDS;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.ClogItem;
import io.septem150.xeric.data.CollectionLog;
import io.septem150.xeric.data.PlayerInfo;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InterfaceID.TrailRewardscreen;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class ClogManager {
  private static final Pattern CLOG_REGEX =
      Pattern.compile("New item added to your collection log: (.*)");
  private CollectionLog clog;

  private boolean active;
  private int ticksTilUpdate = -1;
  private Set<Integer> clogItemIds;
  private boolean clogOpened;
  private Multiset<Integer> inventoryItems;
  private String obtainedItemName;

  private final Client client;
  private final EventBus eventBus;
  private final @Named("xericGson") Gson gson;
  private final ItemManager itemManager;

  public void startUp() {
    if (active) return;
    eventBus.register(this);
    active = true;
    ticksTilUpdate = 1; // wait 1 tick before updating
  }

  public void shutDown() {
    if (!active) return;
    eventBus.unregister(this);
    active = false;
    ticksTilUpdate = -1; // never update
    clog = null;
    clogOpened = false;
  }

  public void reset() {
    ticksTilUpdate = 1; // wait 1 tick before updating
    clog = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (clog == null) {
      if (clogItemIds == null) {
        clogItemIds = requestAllClogItems();
      }
      clog = new CollectionLog();
    }
    playerInfo.setCollectionLog(clog);
    log.debug("updated player clog");
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (!active) return;
    if (ticksTilUpdate > 0) {
      ticksTilUpdate--;
    } else if (ticksTilUpdate == 0) {
      eventBus.post(new PlayerUpdate(this::update));
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
  public void onScriptPreFired(ScriptPreFired event) {
    if (event.getScriptId() == COLLECTION_LOG_TRANSMIT_SCRIPT_ID) {
      int itemId = (int) event.getScriptEvent().getArguments()[1];
      if (clog.getItems().stream().noneMatch(item -> item.getId() == itemId)) {
        ClogItem clogItem = ClogItem.from(client, itemId);
        clog.getItems().add(clogItem);
      }
    }
  }

  @Subscribe
  public void onScriptPostFired(ScriptPostFired event) {
    if (event.getScriptId() != COLLECTION_LOG_SETUP_SCRIPT_ID) {
      return;
    }
    if (!clogOpened) {
      clog.setLastOpened(Instant.now());
      // taken from WikiSync, not really sure what script is being run,
      // but it appears that simulating a click on the Search button
      // loads the script that checks for clog items
      client.menuAction(-1, 40697932, MenuAction.CC_OP, 1, -1, "Search", null);
      client.runScript(2240);
      clogOpened = true;
      // wait 2 ticks to update
      ticksTilUpdate = 3;
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

  private void updateObtainedItem(ItemStack itemStack) {
    log.debug("Called updateObtainedItem");
    if (clogItemIds.contains(itemStack.getId())) {
      ClogItem clogItem = ClogItem.from(client, itemStack.getId());
      clog.add(clogItem);
      log.debug("Item added to player clog: {}", clogItem.getName());
      // delay by a tick in case this causes lag
      ticksTilUpdate = 1;
    }
    obtainedItemName = null;
    inventoryItems = HashMultiset.create();
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
