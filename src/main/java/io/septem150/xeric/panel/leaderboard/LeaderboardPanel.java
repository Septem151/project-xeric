package io.septem150.xeric.panel.leaderboard;

import com.google.gson.Gson;
import io.septem150.xeric.data.player.AccountType;
import io.septem150.xeric.data.player.ClanRank;
import java.awt.BorderLayout;
import java.util.AbstractCollection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;

@Slf4j
@Singleton
public class LeaderboardPanel extends JPanel {
  public static final String TOOLTIP = "Leaderboard";
  public static final String TAB_ICON = "/skill_icons/overall.png";

  private final JTable hiscoresTable;
  private final HiscoresTableModel hiscoresTableModel;
  private final ClientThread clientThread;
  private final SpriteManager spriteManager;
  private final Gson gson;

  @Inject
  private LeaderboardPanel(
      ClientThread clientThread, SpriteManager spriteManager, @Named("xericGson") Gson gson) {
    this.clientThread = clientThread;
    this.spriteManager = spriteManager;
    this.gson = gson;

    setLayout(new BorderLayout());
    hiscoresTableModel = new HiscoresTableModel();
    hiscoresTable = new JTable(hiscoresTableModel);
    hiscoresTable.setCellSelectionEnabled(false);
    hiscoresTable.setColumnSelectionAllowed(false);
    hiscoresTable.setRowSelectionAllowed(false);
    hiscoresTable.setDefaultRenderer(
        AbstractCollection.class, new ExceptionsRenderer(clientThread, spriteManager));
    hiscoresTable.setDefaultRenderer(ClanRank.class, new RankRenderer(clientThread, spriteManager));
    hiscoresTable.setDefaultRenderer(
        Username.class, new UsernameRenderer(clientThread, spriteManager));
    hiscoresTable.setFillsViewportHeight(true);
    hiscoresTable.getColumnModel().getColumn(0).setPreferredWidth(38);
    hiscoresTable.getColumnModel().getColumn(1).setPreferredWidth(96);
    hiscoresTable.getColumnModel().getColumn(2).setPreferredWidth(38);
    hiscoresTable.getColumnModel().getColumn(3).setPreferredWidth(40);

    JScrollPane scrollPane = new JScrollPane(hiscoresTable);
    scrollPane.setWheelScrollingEnabled(true);

    add(scrollPane, BorderLayout.CENTER);
  }

  public void startUp() {
    hiscoresTableModel.setData(
        new Object[][] {
          {
            ClanRank.fromPoints(885),
            Username.builder().username("Septem 150").accountType(AccountType.IRONMAN).build(),
            List.of("Slayer", "Other"),
            885
          },
          {
            ClanRank.fromPoints(700),
            Username.builder().username("hc in zeah").accountType(AccountType.HARDCORE).build(),
            List.of(),
            700
          },
          {
            ClanRank.fromPoints(650),
            Username.builder().username("Doom Send").accountType(AccountType.IRONMAN).build(),
            List.of("Slayer"),
            650
          },
          {
            ClanRank.fromPoints(389),
            Username.builder().username("ErgotDreams").accountType(AccountType.HARDCORE).build(),
            List.of("Other"),
            389
          },
          {
            ClanRank.fromPoints(277),
            Username.builder().username("SirFroggits").accountType(AccountType.IRONMAN).build(),
            List.of("Other"),
            277
          },
          {
            ClanRank.fromPoints(9),
            Username.builder().username("Zezima").accountType(AccountType.UNRANKED_GIM).build(),
            List.of("Other"),
            9
          },
        });
  }

  public void shutDown() {}

  public void refresh() {
    log.debug("refreshing LeaderboardPanel");
    revalidate();
  }
}
