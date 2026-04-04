package io.septem150.xeric.data.task;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.player.Level;
import io.septem150.xeric.data.player.PlayerInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LevelTask extends Task {
  @SerializedName("detail")
  private String level;

  @SerializedName("count")
  private int goal;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (level == null) {
      log.warn("level task with id {} has null level!", getId());
      return false;
    }
    switch (level.toUpperCase()) {
      case "TOTAL":
        int total = playerInfo.getLevels().values().stream().mapToInt(Level::getLevel).sum();
        return total >= goal;
      case "ANY":
        for (Level skillLevel : playerInfo.getLevels().values()) {
          if (skillLevel.getXp() >= goal) return true;
        }
        return false;
      case "MAXED":
        int maxed = 0;
        for (Level skillLevel : playerInfo.getLevels().values()) {
          if (skillLevel.getLevel() >= 99) maxed++;
        }
        return maxed >= goal;
      default:
        try {
          Level skillLevel = playerInfo.getLevels().get(Skill.valueOf(level.toUpperCase()));
          if (skillLevel == null) return false;
          return skillLevel.getLevel() >= goal;
        } catch (IllegalArgumentException err) {
          log.warn("unknown skill name parsed: {}", level.toUpperCase());
          return false;
        }
    }
  }
}
