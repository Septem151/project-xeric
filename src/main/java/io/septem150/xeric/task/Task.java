package io.septem150.xeric.task;

import lombok.Data;

@Data
public class Task {
  private String icon;
  private String id;
  private String name;
  private String type;
  private int tier;
}
