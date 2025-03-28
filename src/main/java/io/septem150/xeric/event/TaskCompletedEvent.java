package io.septem150.xeric.event;

import io.septem150.xeric.data.task.Task;
import lombok.Data;
import lombok.NonNull;

/** Event for completing a task. */
@Data
public class TaskCompletedEvent {
  /** The task completed. */
  private @NonNull final Task task;

  /** Whether this event should trigger an update. */
  private final boolean sendUpdate;
}
