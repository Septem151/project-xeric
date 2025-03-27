package io.septem150.xeric.task;

import io.septem150.xeric.data.PlayerData;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CollectTask extends Task {
  private List<Integer> itemIds;
  private int amount;

  @Override
  public boolean checkCompletion(PlayerData playerData) {
    if (playerData == null) return false;
    int amountRemaining = amount;
    for (int itemId : itemIds) {
      if (playerData.getClogItems().contains(itemId)) {
        amountRemaining--;
      }
    }
    return amountRemaining <= 0;
  }
}
