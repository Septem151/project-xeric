package io.septem150.xeric.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.Quest;
import net.runelite.api.StructComposition;

@Getter
@Slf4j
@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @__(@Inject))
public class ClientManager {
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
          Quest.RUNE_MYSTERIES,
          Quest.MEAT_AND_GREET,
          Quest.ETHICALLY_ACQUIRED_ANTIQUITIES);
  private static final List<Quest> bannedQuests =
      Arrays.stream(Quest.values())
          .filter(quest -> !whitelistedQuests.contains(quest))
          .collect(Collectors.toList());

  private final Client client;

  public boolean isLoggedOut() {
    return !(client.isClientThread()
        && client.getLocalPlayer() != null
        && client.getLocalPlayer().getName() != null);
  }

  public @Nullable String getUsername() {
    if (isLoggedOut()) return null;
    return client.getLocalPlayer().getName();
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
   * Script 4834 but implemented in Java.
   *
   * @param taskId CA Task ID to check for completion.
   * @return whether the CA is complete (true) or not (false).
   */
  public boolean isCATaskComplete(int taskId) {
    return (client.getVarpValue(SCRIPT_4834_VARP_IDS[taskId / 32]) & (1 << (taskId % 32))) != 0;
  }
}
