package io.septem150.xeric.data.hiscore;

import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.ClanRank;
import lombok.Data;

@Data
public class Hiscore {
  private int id;
  private String username;
  private boolean slayerException;
  private int points;
  private String accountType;

  public ClanRank getRank() {
    return ClanRank.fromPoints(points);
  }

  public AccountType getAccountType() {
    try {
      return AccountType.valueOf(accountType);
    } catch (IllegalArgumentException exc) {
      return AccountType.DEFAULT;
    }
  }
}
