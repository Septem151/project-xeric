package io.septem150.xeric.manager;

import com.google.gson.Gson;
import io.septem150.xeric.PlayerUpdate;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.data.AccountType;
import io.septem150.xeric.data.PlayerInfo;
import io.septem150.xeric.data.StoredInfo;
import io.septem150.xeric.util.WorldUtil;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@Singleton
public class PlayerManager {
  private String username;
  private AccountType accountType;
  private boolean slayerException;

  private boolean active;
  private int ticksTilUpdate = -1;
  private long lastAccount = -1L;

  private final Client client;
  private final EventBus eventBus;
  private final Gson gson;
  private final ProjectXericConfig config;

  @Inject
  public PlayerManager(
      Client client, EventBus eventBus, @Named("xericGson") Gson gson, ProjectXericConfig config) {
    this.client = client;
    this.eventBus = eventBus;
    this.gson = gson;
    this.config = config;
  }

  public void startUp() {
    if (active) return;
    eventBus.register(this);
    active = true;
    ticksTilUpdate = 0; // update immediately
  }

  public void shutDown() {
    if (!active) return;
    eventBus.unregister(this);
    active = false;
    ticksTilUpdate = -1; // never update
    username = null;
    accountType = null;
    lastAccount = -1L;
  }

  public void reset() {
    ticksTilUpdate = 0; // update immediately
    username = null;
    accountType = null;
  }

  public void update(PlayerInfo playerInfo) {
    if (!WorldUtil.isValidWorldType(client)) return;
    username = client.getLocalPlayer().getName();
    accountType = AccountType.fromVarbValue(client.getVarbitValue(VarbitID.IRONMAN));
    slayerException = config.slayer();
    playerInfo.setUsername(username);
    playerInfo.setAccountType(accountType);
    playerInfo.setSlayerException(slayerException);
    log.debug("Loaded username: {} ({})", username, accountType.name());
  }

  public void load(@Nullable StoredInfo storedInfo) {
    if (storedInfo == null) return;
    slayerException = storedInfo.isSlayerException();
    ticksTilUpdate = 0;
  }

  public boolean isNewSession() {
    return lastAccount != client.getAccountHash();
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
  public void onGameStateChanged(GameStateChanged event) {
    if (event.getGameState() == GameState.LOGGED_IN) {
      if (isNewSession()) {
        lastAccount = client.getAccountHash();
        ticksTilUpdate = 0;
      }
    } else if (event.getGameState() == GameState.LOGIN_SCREEN) {
      reset();
    }
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (!event.getGroup().equals(ProjectXericConfig.GROUP)) return;
    if (event.getKey().equals(ProjectXericConfig.SLAYER)) {
      slayerException = Boolean.parseBoolean(event.getNewValue());
      ticksTilUpdate = 0;
    }
  }
}
