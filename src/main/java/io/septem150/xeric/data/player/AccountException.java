package io.septem150.xeric.data.player;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum AccountException {
  @SerializedName("Herblore")
  HERBLORE("Herblore"),
  @SerializedName("Boxtraps")
  BOXTRAPS("Boxtraps"),
  @SerializedName("Slayer")
  SLAYER("Slayer"),
  @SerializedName("Other")
  OTHER("Other");

  @NonNull private final String name;

  @Override
  public String toString() {
    return name;
  }
}
