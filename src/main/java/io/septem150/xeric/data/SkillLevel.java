package io.septem150.xeric.data;

import lombok.*;
import net.runelite.api.Skill;

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class SkillLevel {
  @NonNull private final Skill skill;
  private int xp;
  private int level;
}
