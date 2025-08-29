package io.septem150.xeric.task;

import io.septem150.xeric.PlayerData;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CATask extends TaskBase {
  private int total;

  @Override
  public boolean isCompleted(PlayerData playerData) {
    return playerData.getCombatAchievements().size() >= total;
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(3389, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
