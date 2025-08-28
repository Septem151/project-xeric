package io.septem150.xeric.data.diary;

import java.util.List;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DiaryProgress {
  @NonNull private final AchievementDiary diary;
  @EqualsAndHashCode.Exclude private int count;
  @EqualsAndHashCode.Exclude private boolean completed;

  public static final List<AchievementDiary> TRACKED_DIARIES =
      List.of(
          AchievementDiary.KOUREND_EASY,
          AchievementDiary.KOUREND_MEDIUM,
          AchievementDiary.KOUREND_HARD,
          AchievementDiary.KOUREND_ELITE);
}
