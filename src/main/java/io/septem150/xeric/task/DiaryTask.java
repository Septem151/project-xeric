package io.septem150.xeric.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class DiaryTask implements Task {
  private final String name;
  private final TaskTier tier;
  private final String imagePath;
  private final TaskType type = TaskType.DIARY;

  @Setter private boolean completed;
}
