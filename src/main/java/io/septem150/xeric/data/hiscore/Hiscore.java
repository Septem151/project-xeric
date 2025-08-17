package io.septem150.xeric.data.hiscore;

import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.ClanRank;
import java.util.List;
import java.util.Objects;
import lombok.Data;

@Data
public class Hiscore {
  private Integer id;
  private String username;

  private String accountType;

  private List<String> accountExceptions;
  private List<Integer> tasks;
  private Integer points;
  private boolean slayerException;

  public ClanRank getRank() {
    return ClanRank.fromPoints(points);
  }

  public AccountType getAccountType() {
    return AccountType.fromName(accountType);
  }

  public boolean isSlayerException() {
    return slayerException || (accountExceptions != null && accountExceptions.contains("Slayer"));
  }

  public void setAccountExceptions(List<String> accountExceptions) {
    if (accountExceptions != null && accountExceptions.contains("Slayer")) {
      slayerException = true;
    }
    this.accountExceptions = Objects.requireNonNullElseGet(accountExceptions, List::of);
  }

  public List<String> getAccountExceptions() {
    if (accountExceptions == null) {
      if (slayerException) return List.of("Slayer");
      return List.of();
    }
    return accountExceptions;
  }
}
