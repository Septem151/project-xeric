package io.septem150.xeric.manager;

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.QuestProgress;
import io.septem150.xeric.util.WorldUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class QuestManager {
  private static final Pattern QUEST_REGEX =
      Pattern.compile("Congratulations, you've completed a quest:.*");
  private List<QuestProgress> quests;

  private boolean active;
  private int ticksTilUpdate = -1;

  private final Client client;
  private final EventBus eventBus;
  private final Gson gson;

  @Inject
  public QuestManager(Client client, EventBus eventBus, @Named("xericGson") Gson gson) {
    this.client = client;
    this.eventBus = eventBus;
    this.gson = gson;
  }

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
    quests = null;
  }

  public void reset() {
    ticksTilUpdate = 1; // wait 1 tick before updating
    quests = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (!WorldUtil.isValidWorldType(client)) return;
    quests =
        QuestProgress.trackedQuests.stream()
            .map(quest -> QuestProgress.from(client, quest))
            .collect(Collectors.toList());
    playerInfo.setQuests(quests);
    log.debug("updated player quests");
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
    Matcher questMatcher = QUEST_REGEX.matcher(message);
    if (questMatcher.matches()) {
      ticksTilUpdate = 0;
    }
  }
}
