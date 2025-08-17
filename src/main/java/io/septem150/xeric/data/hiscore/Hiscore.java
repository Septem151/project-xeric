package io.septem150.xeric.data.hiscore;

import io.septem150.xeric.data.player.AccountException;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.ClanRank;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.NonNull;

@Data
public class Hiscore {
  @Nullable private Integer id;
  @NonNull private String username;
  @NonNull private AccountType accountType;
  @NonNull private List<AccountException> accountExceptions = new ArrayList<>();
  @NonNull private List<Integer> tasks = new ArrayList<>();
  @NonNull private Integer points = 0;

  public ClanRank getRank() {
    return ClanRank.fromPoints(points);
  }

  public boolean isSlayerException() {
    return accountExceptions.contains(AccountException.SLAYER);
  }
}
