package io.septem150.xeric.data.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RankService {
  private List<ClanRank> standardRanks = new ArrayList<>();
  private List<ClanRank> nonStandardRanks = new ArrayList<>();

  @Inject
  public RankService() {}

  public void setRanks(List<ClanRank> ranks) {
    standardRanks =
        ranks.stream()
            .filter(r -> "standard".equals(r.getBoard()))
            .sorted(Comparator.comparingInt(ClanRank::getMinPoints))
            .collect(Collectors.toList());
    nonStandardRanks =
        ranks.stream()
            .filter(r -> "non_standard".equals(r.getBoard()))
            .sorted(Comparator.comparingInt(ClanRank::getMinPoints))
            .collect(Collectors.toList());
  }

  @Nullable public ClanRank getRank(int points, String board) {
    List<ClanRank> ranks = getRanksForBoard(board);
    ClanRank obtained = null;
    for (ClanRank rank : ranks) {
      if (points < rank.getMinPoints()) break;
      obtained = rank;
    }
    return obtained;
  }

  @Nullable public ClanRank getNextRank(int points, String board) {
    List<ClanRank> ranks = getRanksForBoard(board);
    for (ClanRank rank : ranks) {
      if (rank.getMinPoints() > points) return rank;
    }
    return null;
  }

  public void reset() {
    standardRanks = new ArrayList<>();
    nonStandardRanks = new ArrayList<>();
  }

  private List<ClanRank> getRanksForBoard(String board) {
    return "non_standard".equals(board) ? nonStandardRanks : standardRanks;
  }
}
