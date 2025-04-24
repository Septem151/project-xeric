package io.septem150.xeric.data.task;

import io.septem150.xeric.data.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class KCTask extends Task {
  private String boss;
  private int total;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getKillCounts().stream()
        .anyMatch(killCount -> killCount.getName().equals(boss) && killCount.getCount() >= total);
  }
}
