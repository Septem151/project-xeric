package io.septem150.xeric.data;

import java.util.List;
import lombok.*;
import net.runelite.client.hiscore.HiscoreSkill;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Hiscore {
  @NonNull private final String name;
  @EqualsAndHashCode.Exclude private int count;

  public static final List<HiscoreSkill> HISCORE_SKILLS =
      List.of(
          HiscoreSkill.CHAMBERS_OF_XERIC_CHALLENGE_MODE,
          HiscoreSkill.CHAMBERS_OF_XERIC,
          HiscoreSkill.ALCHEMICAL_HYDRA,
          HiscoreSkill.AMOXLIATL,
          HiscoreSkill.THE_HUEYCOATL,
          HiscoreSkill.SARACHNIS,
          HiscoreSkill.HESPORI,
          HiscoreSkill.SKOTIZO,
          HiscoreSkill.LUNAR_CHESTS,
          HiscoreSkill.SOL_HEREDIT,
          HiscoreSkill.WINTERTODT,
          HiscoreSkill.MIMIC,
          HiscoreSkill.YAMA,
          HiscoreSkill.DOOM_OF_MOKHAIOTL,
          HiscoreSkill.CLUE_SCROLL_ALL,
          HiscoreSkill.CLUE_SCROLL_EASY,
          HiscoreSkill.CLUE_SCROLL_MEDIUM,
          HiscoreSkill.CLUE_SCROLL_HARD,
          HiscoreSkill.CLUE_SCROLL_ELITE,
          HiscoreSkill.CLUE_SCROLL_MASTER,
          HiscoreSkill.COLOSSEUM_GLORY);
}
