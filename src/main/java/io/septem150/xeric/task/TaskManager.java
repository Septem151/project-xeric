package io.septem150.xeric.task;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TaskManager {
  private final TaskStore taskStore;

  @Inject
  public TaskManager(TaskStore taskStore) {
    this.taskStore = taskStore;
  }

  public List<Task> getAllTasks() {
    return taskStore.getAll();
  }
}
