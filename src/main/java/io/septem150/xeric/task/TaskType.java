package io.septem150.xeric.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Property of a {@link Task} representing what type of activity the task requires for completion.
 *
 * <p>Examples include:
 *
 * <ul>
 *   <li>{@link TaskType#COLLECT Collection Log Tasks}
 *   <li>{@link TaskType#COMBAT Combat Achievement Tasks}
 *   <li>{@link TaskType#DIARY Achievement Diary Tasks}
 *   <li>{@link TaskType#LEVEL Level Milestone Tasks}
 *   <li>{@link TaskType#QUEST Quest Completion Tasks}
 * </ul>
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 * @see io.septem150.xeric.task.Task
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TaskType {
  COLLECT("Collect"),
  COMBAT("Combat"),
  DIARY("Diary"),
  LEVEL("Level"),
  QUEST("Quest");

  private final String name;
}
