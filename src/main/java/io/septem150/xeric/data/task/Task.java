package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public abstract class Task implements Serializable {
  @EqualsAndHashCode.Include private int id;
  private String hash;
  private String icon;
  private String name;
  private TaskType type;
  private int tier;

  public abstract boolean checkCompletion(@NonNull PlayerInfo playerInfo);
}
