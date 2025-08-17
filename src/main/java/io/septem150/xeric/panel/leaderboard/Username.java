package io.septem150.xeric.panel.leaderboard;

import io.septem150.xeric.data.player.AccountType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Username {
  private AccountType accountType;
  private String username;
}
