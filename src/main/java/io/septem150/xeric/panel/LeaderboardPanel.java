package io.septem150.xeric.panel;

import io.septem150.xeric.util.ResourceUtil;
import java.awt.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.LinkBrowser;

@Slf4j
@Singleton
public class LeaderboardPanel extends JPanel {
  public static final String TOOLTIP = "Leaderboard";
  public static final String TAB_ICON = "/skill_icons/overall.png";

  private final BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
  private final JLabel wipLabel = new JLabel();
  private final JButton womButton = new JButton();

  @Inject
  private LeaderboardPanel() {
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
    wipLabel.setText(
        "<html><body style='text-align: center'>Leaderboard coming soon!<br>Check out the Wise Old"
            + " Man group in the meantime.</body></html>");
    wipLabel.setFont(FontManager.getRunescapeSmallFont());
    wipLabel.setForeground(ColorScheme.BRAND_ORANGE);
    womButton.setIcon(new ImageIcon(ResourceUtil.getImage("wiseoldman_icon.png")));
    womButton.addActionListener(
        event -> LinkBrowser.browse("https://wiseoldman.net/groups/1200/hiscores"));
    womButton.setToolTipText("View the Leaderboard on Wise Old Man");
  }

  public void refresh() {
    revalidate();
  }
}
