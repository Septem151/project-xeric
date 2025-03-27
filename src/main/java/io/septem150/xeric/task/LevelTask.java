package io.septem150.xeric.task;

import io.septem150.xeric.data.PlayerData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LevelTask extends Task {
  private String level;
  private int goal;

  @Override
  public boolean checkCompletion(PlayerData playerData) {
    if (playerData == null) return false;
    int playerLevel;
    if ("total".equalsIgnoreCase(level)) {
      playerLevel = playerData.getLevels().values().stream().mapToInt(Integer::intValue).sum();
    } else {
      playerLevel = playerData.getLevels().getOrDefault(level, 0);
    }
    return playerLevel >= goal;
  }
}
