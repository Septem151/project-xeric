package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CATask extends Task {
  private int total;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getCombatAchievements().size() >= total;
  }
}
