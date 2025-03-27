package io.septem150.xeric.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.septem150.xeric.ClanRank;
import io.septem150.xeric.ProjectXericConfig;
import io.septem150.xeric.task.Task;
import io.septem150.xeric.task.TaskStore;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Varbits;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @__(@Inject))
@Setter
@Getter
public class DataManager {
  private static final String RSPROFILE_DATA_KEY = "data";
  private static final int MODICONS_ARCHIVE_ID = 423;
  private static final Map<Integer, Integer> MODICONS_MAP =
      Map.ofEntries(
          Map.entry(0, 32), // normal
          Map.entry(1, 2), // ironman
          Map.entry(2, 3), // ultimate
          Map.entry(3, 10), // hardcore
          Map.entry(4, 41), // gim
          Map.entry(5, 42), // hcgim
          Map.entry(6, 43) // ugim
          );

  private @NonNull PlayerData playerData = new PlayerData();
  private BufferedImage accountTypeImage;

  @Named("xericGson")
  private final Gson gson;

  private final ConfigManager configManager;
  private final SpriteManager spriteManager;
  private final Client client;

  private final TaskStore taskStore;

  public void setPlayerData(PlayerData playerData) {
    this.playerData = playerData;
    int iconId = MODICONS_MAP.getOrDefault(client.getVarbitValue(Varbits.ACCOUNT_TYPE), 0);
    accountTypeImage =
        ImageUtil.resizeImage(spriteManager.getSprite(MODICONS_ARCHIVE_ID, iconId), 14, 14, false);
  }

  public ClanRank getPlayerRank() {
    return ClanRank.fromPoints(getPlayerPoints());
  }

  public int getPlayerPoints() {
    int totalPoints = 0;
    for (Task task : taskStore.getAll()) {
      if (task.checkCompletion(playerData)) {
        totalPoints += task.getTier();
      }
    }
    return totalPoints;
  }

  public void serializeTasks() {
    Type type = new TypeToken<List<Task>>() {}.getType();
    log.debug("Tasks: {}", gson.toJson(taskStore.getAll(), type));
  }

  public void clearPlayerData() {
    playerData.clear();
    accountTypeImage = null;
  }

  public String getPlayerJson() {
    return gson.toJson(playerData);
  }

  public boolean isPlayerDataFresh() {
    return playerData.getUsername() != null
        && playerData.getTimestamp().isAfter(Instant.now().minus(Duration.ofHours(1)));
  }

  public void setRSProfileData() {
    configManager.setRSProfileConfiguration(
        ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, getPlayerJson());
  }

  public @Nullable PlayerData getRSProfileData() {
    try {
      String profileData =
          configManager.getRSProfileConfiguration(
              ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY, String.class);
      return gson.fromJson(profileData, PlayerData.class);
    } catch (JsonSyntaxException ex) {
      log.warn("Malformed saved player data, removing");
      configManager.unsetRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY);
      return null;
    }
  }

  public void clearRSProfileData() {
    configManager.unsetRSProfileConfiguration(ProjectXericConfig.GROUP, RSPROFILE_DATA_KEY);
  }

  public int getTasksCompleted() {
    int tasksCompleted = 0;
    for (Task task : taskStore.getAll()) {
      if (task.checkCompletion(playerData)) {
        tasksCompleted++;
      }
    }
    return tasksCompleted;
  }

  public int getPointsToNextRank() {
    return getPlayerRank().getNextRank().getPointsNeeded() - getPlayerPoints();
  }

  public String getHighestTierCompleted() {
    int highestTier = 0;
    for (int tier = 1; tier <= 10; tier++) {
      List<Task> tasks = taskStore.getByTier(tier);
      if (tasks.isEmpty()) break;
      boolean completed = true;
      for (Task task : tasks) {
        if (!task.checkCompletion(playerData)) {
          completed = false;
          break;
        }
      }
      if (completed) {
        highestTier = tier;
      } else break;
    }
    return highestTier > 0 ? String.format("Tier %d", highestTier) : "None";
  }

  public boolean isHerbloreUnlocked() {
    return playerData.getQuests().getOrDefault(String.valueOf(Quest.DRUIDIC_RITUAL.getId()), 0)
        == 2;
  }

  public boolean isBoxTrapUnlocked() {
    return playerData.getQuests().getOrDefault(String.valueOf(Quest.EAGLES_PEAK.getId()), 0) > 0;
  }

  public boolean isOffIslandSlayerUnlocked() {
    return false;
  }
}
