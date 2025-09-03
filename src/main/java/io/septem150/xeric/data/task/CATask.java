package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CATask extends Task {
  private int total;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getCombatAchievements().size() >= total;
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(3389, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
