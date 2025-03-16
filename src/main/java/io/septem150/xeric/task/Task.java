package io.septem150.xeric.task;

/**
 * An objective that a player must complete.
 *
 * @author <a href="mailto:carson.mullins@proton.me">Septem 150</a>
 */
public interface Task {

  /**
   * Whether the task is complete.
   *
   * @return {@code true} if complete, {@code false} if not
   */
  boolean isCompleted();

  /**
   * Sets whether the task is complete.
   *
   * @param completed {@code true} if complete, {@code false} if not
   */
  void setCompleted(boolean completed);

  /**
   * Gets the Side Panel image path for the task's image.
   *
   * @return the path to the task's image relative to the resource directory
   */
  String getImagePath();

  /**
   * Gets the name of the task.
   *
   * @return the task's name
   */
  String getName();

  /**
   * Gets the {@link TaskTier Tier} of the task.
   *
   * @return the task's tier
   * @see TaskTier
   */
  TaskTier getTier();

  /**
   * Gets the {@link TaskType Type} of the task.
   *
   * @return the task's type
   * @see TaskType
   */
  TaskType getType();
}
