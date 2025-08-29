package io.septem150.xeric.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AchievementDiary {
  @SerializedName("Easy")
  KOUREND_EASY(7933, 7925),
  @SerializedName("Medium")
  KOUREND_MEDIUM(7934, 7926),
  @SerializedName("Hard")
  KOUREND_HARD(7935, 7927),
  @SerializedName("Elite")
  KOUREND_ELITE(7936, 7928);

  private final int countVarb;
  private final int completedVarb;
}
