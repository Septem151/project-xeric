package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.Level;
import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.text.WordUtils;

@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LevelTask extends Task {
  private String level;
  private int goal;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    switch (level.toLowerCase()) {
      case "total":
        int total = playerInfo.getLevels().values().stream().mapToInt(Level::getAmount).sum();
        return total >= goal;
      case "any":
        for (Level playerLevel : playerInfo.getLevels().values()) {
          int currentExp = playerLevel.getExp();
          if (currentExp >= goal) {
            return true;
          }
        }
        return false;
      case "maxed":
        int maxed = 0;
        for (Level playerLevel : playerInfo.getLevels().values()) {
          if (playerLevel.getAmount() >= 99) {
            maxed++;
          }
        }
        return maxed >= goal;
      default:
        Level playerLevel = playerInfo.getLevels().getOrDefault(WordUtils.capitalize(level), null);
        if (playerLevel == null) return false;
        return playerLevel.getAmount() >= goal;
    }
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    if (this.getIcon() != null) {
      return ResourceUtil.getImage(this.getIcon(), ICON_SIZE, ICON_SIZE, true);
    }
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(3387, 0)), 20, 20, true);
  }
}
