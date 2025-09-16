package io.septem150.xeric.data.player;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import net.runelite.api.Skill;

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class Level {
  @NonNull private final Skill skill;
  private int xp;
  private int level;
}
