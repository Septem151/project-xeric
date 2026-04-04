package io.septem150.xeric.data.task;

import java.util.Set;
import lombok.Getter;

@Getter
public class TaskResponse {
  private String hash;
  private Set<Task> tasks;
}
