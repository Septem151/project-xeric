package io.septem150.xeric.panel.leaderboard;

import io.septem150.xeric.data.hiscore.HiscoreStore;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

@Slf4j
@Singleton
public class LeaderboardPanel extends JPanel {
  public static final String TOOLTIP = "Leaderboard";
  public static final String TAB_ICON = "/skill_icons/overall.png";

  //  private final HiscoresTableModel hiscoresTableModel;
  //  private final HiscoreStore hiscoreStore;

  private final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
  private final JLabel wipLabel = new JLabel();
  private final JButton womButton = new JButton();

  @Inject
  private LeaderboardPanel(
      HiscoreStore hiscoreStore, ClientThread clientThread, SpriteManager spriteManager) {
    //    this.hiscoreStore = hiscoreStore;
    //    hiscoresTableModel = new HiscoresTableModel();
    //    final JTable hiscoresTable = new JTable(hiscoresTableModel);
    //    hiscoresTable.setCellSelectionEnabled(false);
    //    hiscoresTable.setColumnSelectionAllowed(false);
    //    hiscoresTable.setRowSelectionAllowed(false);
    //    hiscoresTable.setDefaultRenderer(
    //        AbstractCollection.class, new ExceptionsRenderer(clientThread, spriteManager));
    //    hiscoresTable.setDefaultRenderer(ClanRank.class, new RankRenderer(clientThread,
    // spriteManager));
    //    hiscoresTable.setDefaultRenderer(
    //        Username.class, new UsernameRenderer(clientThread, spriteManager));
    //    hiscoresTable.setFillsViewportHeight(true);
    //    hiscoresTable.getColumnModel().getColumn(0).setPreferredWidth(38);
    //    hiscoresTable.getColumnModel().getColumn(1).setPreferredWidth(96);
    //    hiscoresTable.getColumnModel().getColumn(2).setPreferredWidth(38);
    //    hiscoresTable.getColumnModel().getColumn(3).setPreferredWidth(40);
    //
    //    JScrollPane scrollPane = new JScrollPane(hiscoresTable);
    //    scrollPane.setWheelScrollingEnabled(true);
    //
    //    add(scrollPane, BorderLayout.CENTER);
    makeLayout();
    makeStaticData();
  }

  private void makeLayout() {
    removeAll();
    setLayout(layout);
    wipLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
    wipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(wipLabel);
    womButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(womButton);
  }

  private void makeStaticData() {
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    wipLabel.setText("Leaderboard coming soon!");
    wipLabel.setFont(FontManager.getRunescapeSmallFont());
    wipLabel.setForeground(ColorScheme.BRAND_ORANGE);
    womButton.setIcon(new ImageIcon(ResourceUtil.getImage("wiseoldman_icon.png")));
    womButton.addActionListener(
        event -> LinkBrowser.browse("https://wiseoldman.net/groups/1200/hiscores"));
    womButton.setToolTipText("View the Leaderboard on Wise Old Man");
  }

  public void startUp() {
    //    hiscoresTableModel.setData(
    //        hiscoreStore.getAll().stream()
    //            .map(
    //                hiscore ->
    //                    new Object[] {
    //                      ClanRank.fromPoints(hiscore.getPoints()),
    //                      Username.builder()
    //                          .username(hiscore.getUsername())
    //                          .accountType(hiscore.getAccountType())
    //                          .build(),
    //                      hiscore.getExceptions(),
    //                      hiscore.getPoints()
    //                    })
    //            .toArray(Object[][]::new));
  }

  public void refresh() {
    revalidate();
  }
}
