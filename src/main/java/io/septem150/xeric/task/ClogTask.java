package io.septem150.xeric.task;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.data.ClogItem;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ClogTask extends TaskBase {
  private Set<Integer> itemIds;
  private int amount;

  @Override
  public boolean isCompleted(PlayerData playerData) {
    if (itemIds == null) {
      log.warn("clog task with id {} has null itemIds!", getId());
      return false;
    }
    if (itemIds.isEmpty()) {
      return !playerData.getCollectionLog().getLastUpdated().equals(Instant.EPOCH)
          && playerData.getCollectionLog().size() >= amount;
    }
    Set<Integer> playerItemIds =
        playerData.getCollectionLog().getItems().stream()
            .map(ClogItem::getId)
            .collect(Collectors.toSet());
    int amountRemaining = amount;
    for (int itemId : playerItemIds) {
      if (itemIds.contains(itemId)) {
        amountRemaining--;
      }
      if (amountRemaining == 0) return true;
    }
    return false;
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    if (this.getIcon() != null) {
      return ResourceUtil.getImage(this.getIcon(), ICON_SIZE, ICON_SIZE, true);
    }
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(3390, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
