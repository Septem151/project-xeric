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
  private List<Rank> standardRanks = new ArrayList<>();
  private List<Rank> nonStandardRanks = new ArrayList<>();

  @Inject
  public RankService() {}

  public void setRanks(List<Rank> ranks) {
    standardRanks =
        ranks.stream()
            .filter(r -> "standard".equals(r.getBoard()))
            .sorted(Comparator.comparingInt(Rank::getMinPoints))
            .collect(Collectors.toList());
    nonStandardRanks =
        ranks.stream()
            .filter(r -> "non_standard".equals(r.getBoard()))
            .sorted(Comparator.comparingInt(Rank::getMinPoints))
            .collect(Collectors.toList());
  }

  @Nullable public Rank getRank(int points, String board) {
    List<Rank> ranks = getRanksForBoard(board);
    Rank obtained = null;
    for (Rank rank : ranks) {
      if (points < rank.getMinPoints()) break;
      obtained = rank;
    }
    return obtained;
  }

  @Nullable public Rank getNextRank(int points, String board) {
    List<Rank> ranks = getRanksForBoard(board);
    for (Rank rank : ranks) {
      if (rank.getMinPoints() > points) return rank;
    }
    return null;
  }

  public void reset() {
    standardRanks = new ArrayList<>();
    nonStandardRanks = new ArrayList<>();
  }

  private List<Rank> getRanksForBoard(String board) {
    return "non_standard".equals(board) ? nonStandardRanks : standardRanks;
  }
}
