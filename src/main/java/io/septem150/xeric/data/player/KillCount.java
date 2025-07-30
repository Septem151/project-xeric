package io.septem150.xeric.data.player;

import java.util.List;
import lombok.Data;
import net.runelite.client.hiscore.HiscoreSkill;

@Data
public class KillCount {
  private String name;
  private int count;

  public static final List<HiscoreSkill> hiscoreSkills =
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
