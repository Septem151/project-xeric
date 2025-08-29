package io.septem150.xeric.task;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.data.SkillLevel;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LevelTask extends TaskBase {
  private String level;
  private int goal;

  @Override
  public boolean isCompleted(PlayerData playerData) {
    if (level == null) {
      log.warn("level task with id {} has null level!", getId());
      return false;
    }
    switch (level.toUpperCase()) {
      case "TOTAL":
        int total = playerData.getLevels().values().stream().mapToInt(SkillLevel::getLevel).sum();
        return total >= goal;
      case "ANY":
        for (SkillLevel skillLevel : playerData.getLevels().values()) {
          if (skillLevel.getXp() >= goal) return true;
        }
        return false;
      case "MAXED":
        int maxed = 0;
        for (SkillLevel skillLevel : playerData.getLevels().values()) {
          if (skillLevel.getLevel() >= 99) maxed++;
        }
        return maxed >= goal;
      default:
        try {
          SkillLevel skillLevel = playerData.getLevels().get(Skill.valueOf(level.toUpperCase()));
          if (skillLevel == null) return false;
          return skillLevel.getLevel() >= goal;
        } catch (IllegalArgumentException err) {
          log.warn("unknown skill name parsed: {}", level.toUpperCase());
          return false;
        }
    }
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    if (this.getIcon() != null) {
      return ResourceUtil.getImage(this.getIcon(), ICON_SIZE, ICON_SIZE, true);
    }
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(3387, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
