package io.septem150.xeric.data.task;

import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.diary.KourendDiary;
import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DiaryTask extends Task {
  private KourendDiary diary;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (diary == null) {
      log.warn("diary task with id {} has null diary!", getId());
      return false;
    }
    DiaryProgress diaryProgress = playerInfo.getDiaries().get(diary);
    if (diaryProgress == null) return false;
    return diaryProgress.isCompleted();
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(1299, 0)), ICON_SIZE, ICON_SIZE, true);
  }
}
