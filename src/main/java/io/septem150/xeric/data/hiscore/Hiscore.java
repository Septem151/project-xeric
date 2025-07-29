package io.septem150.xeric.data.hiscore;

import com.google.gson.annotations.SerializedName;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.ClanRank;
import java.util.List;
import lombok.Data;

@Data
public class Hiscore {
  private int id;
  private String username;

  @SerializedName("account_type")
  private String accountType;

  private List<String> exceptions;
  private int points;

  public ClanRank getRank() {
    return ClanRank.fromPoints(points);
  }

  public AccountType getAccountType() {
    return AccountType.fromName(accountType);
  }

  public boolean isSlayerException() {
    return exceptions.contains("Slayer");
  }
}
