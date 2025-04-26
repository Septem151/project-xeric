package io.septem150.xeric.data.task;

import io.septem150.xeric.data.player.PlayerInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiaryTask extends Task {
  private String diary;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    return playerInfo.getDiaries().stream()
        .anyMatch(
            diaryProgress ->
                diaryProgress.getDiary().getName().equals(diary) && diaryProgress.isCompleted());
  }
}
