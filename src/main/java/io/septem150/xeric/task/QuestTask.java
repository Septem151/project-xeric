package io.septem150.xeric.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class QuestTask implements Task {
  private final String name;
  private final TaskTier tier;
  private final String imagePath;
  private final TaskType type = TaskType.QUEST;

  @Setter private boolean completed;
}
