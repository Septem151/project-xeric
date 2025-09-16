package io.septem150.xeric.data.diary;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DiaryProgress {
  @NonNull private final KourendDiary diary;
  @EqualsAndHashCode.Exclude private int count;
  @EqualsAndHashCode.Exclude private boolean completed;

  public static final List<KourendDiary> TRACKED_DIARIES =
      List.of(
          KourendDiary.KOUREND_EASY,
          KourendDiary.KOUREND_MEDIUM,
          KourendDiary.KOUREND_HARD,
          KourendDiary.KOUREND_ELITE);
}
