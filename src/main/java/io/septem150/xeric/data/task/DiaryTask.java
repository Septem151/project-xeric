package io.septem150.xeric.data.task;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.diary.DiaryProgress;
import io.septem150.xeric.data.diary.KourendDiary;
import io.septem150.xeric.data.player.PlayerInfo;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DiaryTask extends Task {
  @SerializedName("detail")
  private KourendDiary diary;

  @Override
  public boolean checkCompletion(@NonNull PlayerInfo playerInfo) {
    if (diary == null) {
      log.warn("diary task with id {} has null diary!", getId());
      return false;
    }
    DiaryProgress diaryProgress = playerInfo.getDiaries().get(diary);
    if (diaryProgress == null) return false;
    return diaryProgress.isCompleted();
  }
}
