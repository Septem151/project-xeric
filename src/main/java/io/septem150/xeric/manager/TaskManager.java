package io.septem150.xeric.manager;

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.StoredInfo;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.util.WorldUtil;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class TaskManager {
  private List<Task> tasks;

  private boolean active;
  private int ticksTilUpdate = -1;
  private List<Task> allTasks;

  private final Client client;
  private final EventBus eventBus;
  private final @Named("xericGson") Gson gson;
  private final TaskStore taskStore;

  public void startUp() {
    if (active) return;
    eventBus.register(this);
    active = true;
    ticksTilUpdate = 3; // wait 3 ticks before updating
  }

  public void shutDown() {
    if (!active) return;
    eventBus.unregister(this);
    active = false;
    ticksTilUpdate = -1; // never update
    tasks = null;
    allTasks = null;
  }

  public void reset() {
    ticksTilUpdate = 3; // wait 3 ticks before updating
    tasks = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (!WorldUtil.isValidWorldType(client)) return;
    if (allTasks == null) {
      allTasks = taskStore.getAll();
    }
    tasks =
        allTasks.stream()
            .filter(task -> task.checkCompletion(playerInfo))
            .collect(Collectors.toList());
    playerInfo.setTasks(tasks);
    log.debug("updated player tasks");
  }

  public void load(@Nullable StoredInfo storedInfo) {
    if (storedInfo == null) return;
    tasks =
        storedInfo.getTasks().stream()
            .map(taskId -> taskStore.getById(taskId).orElseThrow())
            .collect(Collectors.toList());
    ticksTilUpdate = 3;
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
  public void onPlayerUpdate(PlayerUpdate event) {
    // double equality check probs not necessary
    if (event.getSource() == this || event.getSource().equals(this)) return;
    ticksTilUpdate = 0;
  }
}
