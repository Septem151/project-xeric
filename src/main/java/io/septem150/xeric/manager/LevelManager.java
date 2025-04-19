package io.septem150.xeric.manager;

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.Level;
import io.septem150.xeric.data.PlayerInfo;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class LevelManager {
  private List<Level> levels;

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
    levels = null;
  }

  public void reset() {
    ticksTilUpdate = 1; // wait 1 tick before updating
    levels = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (levels == null) {
      levels =
          Arrays.stream(Skill.values())
              .map(skill -> Level.from(client, skill))
              .collect(Collectors.toList());
      log.info("Loaded levels:\n{}", gson.toJson(levels));
    }
    playerInfo.setLevels(levels);
    log.debug("updated player levels");
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
  public void onStatChanged(StatChanged event) {
    if (!active) return;
    for (Level level : levels) {
      if (level.getName().equals(event.getSkill().getName())) {
        if (level.getValue() != event.getLevel()) {
          log.debug(
              "Gained a level: {} : {} -> {}", level.getName(), level.getValue(), event.getLevel());
          level.setValue(event.getLevel());
          ticksTilUpdate = 0;
        }
        break;
      }
    }
  }
}
