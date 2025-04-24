package io.septem150.xeric.manager;

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

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.CombatAchievement;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.util.WorldUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class CAManager {
  private static final Pattern COMBAT_TASK_REGEX =
      Pattern.compile("Congratulations, you've completed an? \\w+ combat task:.*");
  private List<CombatAchievement> cas;
  private Set<Integer> caTaskStructIds;

  private boolean active;
  private int ticksTilUpdate = -1;

  private final Client client;
  private final EventBus eventBus;
  private final Gson gson;

  @Inject
  public CAManager(Client client, EventBus eventBus, @Named("xericGson") Gson gson) {
    this.client = client;
    this.eventBus = eventBus;
    this.gson = gson;
  }

  public void startUp() {
    if (active) return;
    cas = new ArrayList<>();
    eventBus.register(this);
    active = true;
    ticksTilUpdate = 1; // wait 1 tick before updating
  }

  public void shutDown() {
    if (!active) return;
    eventBus.unregister(this);
    active = false;
    ticksTilUpdate = -1; // never update
    cas = null;
  }

  public void reset() {
    ticksTilUpdate = 1; // wait 1 tick before updating
    cas = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (!WorldUtil.isValidWorldType(client)) return;
    if (caTaskStructIds == null) {
      caTaskStructIds = requestAllCaTaskStructIds(client);
    }
    cas = new ArrayList<>();
    for (int caTaskStructId : caTaskStructIds) {
      StructComposition struct = client.getStructComposition(caTaskStructId);
      int caTaskId = struct.getIntValue(CA_STRUCT_ID_PARAM_ID);
      boolean unlocked =
          (client.getVarpValue(SCRIPT_4834_VARP_IDS[caTaskId / 32]) & (1 << (caTaskId % 32))) != 0;
      if (unlocked) {
        CombatAchievement combatAchievement = new CombatAchievement();
        combatAchievement.setId(caTaskId);
        combatAchievement.setName(struct.getStringValue(CA_STRUCT_NAME_PARAM_ID));
        combatAchievement.setPoints(struct.getIntValue(CA_STRUCT_TIER_PARAM_ID));
        cas.add(combatAchievement);
      }
    }
    playerInfo.setCombatAchievements(cas);
    log.debug("updated player CAs");
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    if (!active) return;
    if (ticksTilUpdate == 0) {
      eventBus.post(new PlayerUpdate(this, this::update));
    }
    if (ticksTilUpdate >= 0) {
      ticksTilUpdate--;
    }
  }

  @Subscribe
  public void onChatMessage(ChatMessage event) {
    if (!active || event.getType() != ChatMessageType.GAMEMESSAGE) return;
    String message = Text.removeTags(event.getMessage());
    Matcher caTaskMatcher = COMBAT_TASK_REGEX.matcher(message);
    if (caTaskMatcher.matches()) {
      ticksTilUpdate = 0;
    }
  }

  private Set<Integer> requestAllCaTaskStructIds(Client client) {
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
}
