package io.septem150.xeric.data.player;

import lombok.Data;

@Data
public class CombatAchievement {
  public static final int EASY_TIER_ENUM_ID = 3981;
  public static final int MEDIUM_TIER_ENUM_ID = 3982;
  public static final int HARD_TIER_ENUM_ID = 3983;
  public static final int ELITE_TIER_ENUM_ID = 3984;
  public static final int MASTER_TIER_ENUM_ID = 3985;
  public static final int GM_TIER_ENUM_ID = 3986;
  public static final int CA_STRUCT_ID_PARAM_ID = 1306;
  public static final int CA_STRUCT_NAME_PARAM_ID = 1308;
  public static final int CA_STRUCT_TIER_PARAM_ID = 1310;

  private int id;
  private String name;
  private int points;
}
