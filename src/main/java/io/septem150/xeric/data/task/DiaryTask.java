package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiaryTask extends Task {
  private String diary;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getDiaries().stream()
        .anyMatch(
            diaryProgress ->
                diaryProgress.getDiary().getName().equals(diary) && diaryProgress.isCompleted());
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(Objects.requireNonNull(spriteManager.getSprite(1299, 0)), 20, 20);
  }
}
