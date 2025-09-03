package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.awt.image.BufferedImage;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import net.runelite.api.QuestState;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class QuestTask extends Task {
  private String quest;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getQuests().stream()
        .anyMatch(
            questProgress ->
                questProgress.getQuest().getName().equals(quest)
                    && questProgress.getState() == QuestState.FINISHED);
  }

  @Override
  public BufferedImage getIcon(SpriteManager spriteManager) {
    return ImageUtil.resizeImage(
        Objects.requireNonNull(spriteManager.getSprite(899, 0)), 20, 20, true);
  }
}
