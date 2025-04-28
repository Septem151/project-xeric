package io.septem150.xeric.panel.leaderboard;

import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

@Slf4j
@Singleton
public class LeaderboardPanel extends JPanel {
  public static final String TOOLTIP = "Leaderboard";
  public static final String TAB_ICON = "/skill_icons/overall.png";

  private final PlayerListPanel playerListPanel;

  @Inject
  private LeaderboardPanel(PlayerListPanel playerListPanel) {
    this.playerListPanel = playerListPanel;
    makeLayout();
    makeStaticData();
  }

  private void makeLayout() {
    removeAll();
    setLayout(new BorderLayout());
    add(playerListPanel, BorderLayout.CENTER);
  }

  private void makeStaticData() {
    setBackground(ColorScheme.DARK_GRAY_COLOR);
  }

  public void reload() {
    makeLayout();
    makeStaticData();
    playerListPanel.reload();
    revalidate();
  }
}
