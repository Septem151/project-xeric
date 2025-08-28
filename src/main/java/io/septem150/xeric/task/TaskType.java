package io.septem150.xeric.task;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TaskType {
  @SerializedName("ca")
  CA("ca"),
  @SerializedName("collect")
  CLOG("collect"),
  @SerializedName("diary")
  DIARY("diary"),
  @SerializedName("kc")
  HISCORE("kc"),
  @SerializedName("level")
  LEVEL("level"),
  @SerializedName("quest")
  QUEST("quest");

  private final String name;
}
