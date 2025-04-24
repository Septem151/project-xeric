package io.septem150.xeric.data.task;

import io.septem150.xeric.data.ClogItem;
import io.septem150.xeric.data.PlayerInfo;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class CollectTask extends Task {
  private List<Integer> itemIds;
  private int amount;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (itemIds.isEmpty()) {
      return playerInfo.getCollectionLog().size() >= amount;
    }
    List<Integer> itemIds =
        playerInfo.getCollectionLog().getItems().stream()
            .map(ClogItem::getId)
            .collect(Collectors.toList());
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
