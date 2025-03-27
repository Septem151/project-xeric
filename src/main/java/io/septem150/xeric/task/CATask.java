package io.septem150.xeric.task;

import io.septem150.xeric.data.PlayerData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CATask extends Task {
  private int total;

  @Override
  public boolean checkCompletion(PlayerData playerData) {
    if (playerData == null) return false;
    return playerData.getCaTasks().size() >= total;
  }
}
