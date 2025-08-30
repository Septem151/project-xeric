package io.septem150.xeric.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TaskType {
  @SerializedName("collect")
  CLOG("collect"),
  @SerializedName("level")
  LEVEL("level"),
  @SerializedName("kc")
  HISCORE("kc"),
  @SerializedName("ca")
  CA("ca"),
  @SerializedName("quest")
  QUEST("quest"),
  @SerializedName("diary")
  DIARY("diary");

  private final String name;
}
