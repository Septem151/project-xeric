package io.septem150.xeric.panel.leaderboard;

import io.septem150.xeric.data.hiscore.HiscoreStore;
import io.septem150.xeric.data.player.ClanRank;
import java.awt.BorderLayout;
import java.util.AbstractCollection;
import javax.inject.Inject;
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
  private final HiscoreStore hiscoreStore;

  @Inject
  private LeaderboardPanel(
      HiscoreStore hiscoreStore, ClientThread clientThread, SpriteManager spriteManager) {
    this.hiscoreStore = hiscoreStore;

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
        hiscoreStore.getAll().stream()
            .map(
                hiscore ->
                    new Object[] {
                      ClanRank.fromPoints(hiscore.getPoints()),
                      Username.builder()
                          .username(hiscore.getUsername())
                          .accountType(hiscore.getAccountType())
                          .build(),
                      hiscore.getExceptions(),
                      hiscore.getPoints()
                    })
            .toArray(Object[][]::new));
  }

  public void shutDown() {}

  public void refresh() {
    log.debug("refreshing LeaderboardPanel");
    revalidate();
  }
}
