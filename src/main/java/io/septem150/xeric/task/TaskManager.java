package io.septem150.xeric.task;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Singleton
@Getter
@RequiredArgsConstructor(onConstructor_ = @__(@Inject))
public class TaskManager {
  private final TaskStore taskStore;

  public List<Task> getAllTasks() {
    return taskStore.getAll();
  }
}
