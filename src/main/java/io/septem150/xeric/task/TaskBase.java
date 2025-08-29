package io.septem150.xeric.task;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.data.TaskType;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import lombok.*;
import net.runelite.client.game.SpriteManager;

@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class TaskBase implements Serializable {
  protected static final int ICON_SIZE = 20;

  @EqualsAndHashCode.Include private int id;
  private String name;
  private String icon;
  private TaskType type;
  private int tier;

  public abstract boolean isCompleted(PlayerData playerData);

  public abstract BufferedImage getIcon(SpriteManager spriteManager);
}
