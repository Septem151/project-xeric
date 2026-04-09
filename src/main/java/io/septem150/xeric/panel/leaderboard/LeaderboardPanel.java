package io.septem150.xeric.panel.leaderboard;

import io.septem150.xeric.util.ResourceUtil;
import java.awt.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.LinkBrowser;

@Slf4j
@Singleton
public class LeaderboardPanel extends JPanel {
  public static final String TOOLTIP = "Leaderboard";
  public static final String TAB_ICON = "/skill_icons/overall.png";

  private final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
  private final JButton leaderboardButton = new JButton();
  private final JButton womButton = new JButton();

  @Inject
  private LeaderboardPanel() {
    makeLayout();
    makeStaticData();
  }

  private void makeLayout() {
    removeAll();
    setLayout(layout);
    setBorder(new EmptyBorder(0, 20, 0, 20));
    leaderboardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(leaderboardButton);
    add(Box.createVerticalStrut(5));
    womButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(womButton);
  }

  private void makeStaticData() {
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    leaderboardButton.setIcon(new ImageIcon(ResourceUtil.getImage("sidepanel_icon.png", 32, 32)));
    leaderboardButton.setText("Clan Leaderboard");
    leaderboardButton.addActionListener(event -> LinkBrowser.browse("https://projectxeric.com"));
    leaderboardButton.setToolTipText("View the Zeah Ironmen Leaderboard");
    womButton.setIcon(new ImageIcon(ResourceUtil.getImage("wiseoldman_icon.png", 32, 32)));
    womButton.setText("Wise Old Man");
    womButton.addActionListener(
        event -> LinkBrowser.browse("https://wiseoldman.net/groups/1200/hiscores"));
    womButton.setToolTipText("View the Wise Old Man Group");
  }

  public void refresh() {
    revalidate();
  }
}
