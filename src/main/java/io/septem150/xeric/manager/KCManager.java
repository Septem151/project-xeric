package io.septem150.xeric.manager;

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.AccountType;
import io.septem150.xeric.data.KillCount;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.util.WorldUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.hiscore.HiscoreManager;
import net.runelite.client.hiscore.HiscoreResult;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class KCManager {
  private static final Pattern KC_REGEX =
      Pattern.compile("Your (?:subdued|completed)? (.*) (?:kill)? count is: (\\d+)\\.");
  private static final Pattern CLUE_REGEX =
      Pattern.compile("You have completed (\\d+) (.*) Treasure Trails\\.");
  private List<KillCount> kcs;

  private boolean active;
  private int ticksTilUpdate = -1;

  private final Client client;
  private final EventBus eventBus;
  private final Gson gson;
  private final ScheduledExecutorService executor;
  private final HiscoreManager hiscoreManager;

  @Inject
  public KCManager(
      Client client,
      EventBus eventBus,
      @Named("xericGson") Gson gson,
      ScheduledExecutorService executor,
      HiscoreManager hiscoreManager) {
    this.client = client;
    this.eventBus = eventBus;
    this.gson = gson;
    this.executor = executor;
    this.hiscoreManager = hiscoreManager;
  }

  public void startUp() {
    if (active) return;
    eventBus.register(this);
    active = true;
    ticksTilUpdate = 2; // wait 2 ticks before updating
  }

  public void shutDown() {
    if (!active) return;
    eventBus.unregister(this);
    active = false;
    ticksTilUpdate = -1; // never update
    kcs = null;
  }

  public void reset() {
    ticksTilUpdate = 2; // wait 1 tick before updating
    kcs = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (!WorldUtil.isValidWorldType(client)) return;
    if (kcs == null) {
      kcs = new ArrayList<>();
      HiscoreEndpoint hiscoreEndpoint =
          AccountType.fromVarbValue(client.getVarbitValue(VarbitID.IRONMAN)).getHiscoreEndpoint();
      executor.execute(
          () -> {
            try {
              HiscoreResult result =
                  hiscoreManager.lookup(client.getLocalPlayer().getName(), hiscoreEndpoint);
              KillCount.hiscoreSkills.forEach(
                  hiscoreSkill -> {
                    KillCount killCount = new KillCount();
                    killCount.setCount(Math.max(0, result.getSkill(hiscoreSkill).getLevel()));
                    killCount.setName(hiscoreSkill.getName());
                    kcs.add(killCount);
                  });
              ticksTilUpdate = 0;
            } catch (IOException exc) {
              log.warn(
                  "IOException while looking up hiscores for player '{}'",
                  client.getLocalPlayer().getName());
            }
          });
    } else {
      playerInfo.setKillCounts(kcs);
      log.debug("updated player KCs:\n{}", gson.toJson(kcs));
    }
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
    log.debug("Game message: {}", message);
    Matcher kcMatcher = KC_REGEX.matcher(message);
    if (kcMatcher.matches()) {
      String name = kcMatcher.group(1);
      if ("Lunar Chest".equals(name)) {
        name += "s";
      } else if ("Hueycoatl".equals(name)) {
        name = "The " + name;
      }
      int count = Integer.parseInt(kcMatcher.group(2));
      for (KillCount killCount : kcs) {
        if (killCount.getName().equals(name)) {
          killCount.setCount(count);
          break;
        }
      }
      ticksTilUpdate = 0;
      return;
    }
    Matcher clueMatcher = CLUE_REGEX.matcher(message);
    if (clueMatcher.matches()) {
      int count = Integer.parseInt(clueMatcher.group(1));
      String tier = clueMatcher.group(2);
      for (KillCount killCount : kcs) {
        if (killCount.getName().equals(String.format("Clue Scrolls (%s)", tier))) {
          killCount.setCount(count);
        }
      }
      ticksTilUpdate = 0;
    }
  }
}
