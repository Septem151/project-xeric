package io.septem150.xeric.data.task;

import io.septem150.xeric.data.clog.ClogItem;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CollectTask extends Task {
  private Set<Integer> itemIds;
  private int amount;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (itemIds == null) {
      log.warn("clog task with id {} has null itemIds!", getId());
      return false;
    }
    if (itemIds.isEmpty()) {
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

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    if (this.getIcon() != null) {
      return ResourceUtil.getImage(this.getIcon(), ICON_SIZE, ICON_SIZE, true);
    }
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(3390, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
