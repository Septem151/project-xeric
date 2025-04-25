package io.septem150.xeric.data.task;

import io.septem150.xeric.data.PlayerInfo;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CollectTask extends Task {
  private Set<Integer> itemIds;
  private int amount;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (itemIds.isEmpty()) {
      return playerInfo.getCollectionLog().size() >= amount;
    }
    Set<Integer> itemIds = playerInfo.getCollectionLog().getItemIds();
    int amountRemaining = amount;
    for (int itemId : itemIds) {
      if (this.itemIds.contains(itemId)) {
        amountRemaining--;
      }
      if (amountRemaining == 0) break;
    }
    return amountRemaining <= 0;
  }
}
