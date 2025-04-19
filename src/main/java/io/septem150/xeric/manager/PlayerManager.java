package io.septem150.xeric.manager;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class PlayerManager {
  private long lastAccount = -1L;

  private final QuestManager questManager;
  private final DiaryManager diaryManager;
  private final LevelManager levelManager;
  private final ClogManager clogManager;
  private final KCManager kcManager;
  private final CAManager caManager;

  private final Client client;
  private final ClientThread clientThread;

  public void startUp() {
    questManager.startUp();
    diaryManager.startUp();
    levelManager.startUp();
    clogManager.startUp();
    kcManager.startUp();
    caManager.startUp();
    clientThread.invokeLater(
        () -> {
          if (client.getGameState() == GameState.LOGGED_IN) {
            // TODO: Check if on right world type
            lastAccount = client.getAccountHash();
          }
        });
  }

  public void shutDown() {
    questManager.shutDown();
    diaryManager.shutDown();
    levelManager.shutDown();
    clogManager.shutDown();
    kcManager.shutDown();
    caManager.shutDown();
  }

  public void reset() {
    questManager.reset();
    diaryManager.reset();
    levelManager.reset();
    clogManager.reset();
    kcManager.reset();
    caManager.reset();
  }
}
