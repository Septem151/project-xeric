package io.septem150.xeric.task;

import io.septem150.xeric.data.diary.AchievementDiary;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.player.PlayerData;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DiaryTask extends Task {
  private AchievementDiary diary;

  @Override
  public boolean isCompleted(PlayerData playerData) {
    if (diary == null) {
      log.warn("diary task with id {} has null diary!", getId());
      return false;
    }
    DiaryProgress diaryProgress = playerData.getDiaries().get(diary);
    if (diaryProgress == null) return false;
    return diaryProgress.isCompleted();
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(1299, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
