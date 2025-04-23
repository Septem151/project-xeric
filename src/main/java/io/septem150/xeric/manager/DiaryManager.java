package io.septem150.xeric.manager;

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.DiaryProgress;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.util.WorldUtil;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class DiaryManager {
  private static final Pattern DIARY_REGEX =
      Pattern.compile(
          "Well done! You have completed an? \\w+ task in the .* area\\. Your Achievement"
              + " Diary has been updated");
  private List<DiaryProgress> diaries;

  private boolean active;
  private int ticksTilUpdate = -1;

  private final Client client;
  private final EventBus eventBus;
  private final @Named("xericGson") Gson gson;

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
    diaries = null;
  }

  public void reset() {
    ticksTilUpdate = 1; // wait 1 tick before updating
    diaries = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (!WorldUtil.isValidWorldType(client)) return;
    diaries =
        DiaryProgress.trackedDiaries.stream()
            .map(diary -> DiaryProgress.from(client, diary))
            .collect(Collectors.toList());
    playerInfo.setDiaries(diaries);
    log.debug("updated player diaries");
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
    Matcher diaryMatcher = DIARY_REGEX.matcher(message);
    if (diaryMatcher.matches()) {
      diaries =
          DiaryProgress.trackedDiaries.stream()
              .map(diary -> DiaryProgress.from(client, diary))
              .collect(Collectors.toList());
      ticksTilUpdate = 0;
    }
  }
}
