package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

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

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    if (this.getIcon() != null) {
      return ResourceUtil.getImage(this.getIcon(), ICON_SIZE, ICON_SIZE, true);
    }
    return ImageUtil.resizeImage(Objects.requireNonNull(spriteManager.getSprite(3390, 0)), 20, 20, true);
  }
}
