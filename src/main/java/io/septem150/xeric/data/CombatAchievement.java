package io.septem150.xeric.data;

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
  public static final int CA_STRUCT_TIER_PARAM_ID = 1310;
  public static final int CA_STRUCT_NAME_PARAM_ID = 1312;
  public static final int[] SCRIPT_4834_VARP_IDS =
      new int[] {
        3116, 3117, 3118, 3119, 3120, 3121, 3122, 3123, 3124, 3125, 3126, 3127, 3128, 3387, 3718,
        3773, 3774, 4204, 4496
      };

  private int id;
  private String name;
  private int points;
}
