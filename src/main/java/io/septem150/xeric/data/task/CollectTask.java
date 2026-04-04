package io.septem150.xeric.data.task;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.clog.ClogItem;
import io.septem150.xeric.data.player.PlayerInfo;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CollectTask extends Task {
  @SerializedName("detail")
  private Set<Integer> itemIds;

  @SerializedName("count")
  private int amount;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (itemIds == null || itemIds.isEmpty()) {
      return !playerInfo.getClogLastUpdated().equals(Instant.EPOCH)
          && playerInfo.getClogItems().size() >= amount;
    }
    Set<Integer> playerItemIds =
        playerInfo.getClogItems().stream().map(ClogItem::getId).collect(Collectors.toSet());
    int amountRemaining = amount;
    for (int itemId : playerItemIds) {
      if (itemIds.contains(itemId)) {
        amountRemaining--;
      }
      if (amountRemaining == 0) return true;
    }
    return false;
  }
}
