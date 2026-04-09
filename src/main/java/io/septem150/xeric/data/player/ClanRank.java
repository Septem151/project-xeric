package io.septem150.xeric.data.player;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class ClanRank {
  private String name;
  private String board;

  @SerializedName("min_points")
  private int minPoints;

  private String icon;
}
