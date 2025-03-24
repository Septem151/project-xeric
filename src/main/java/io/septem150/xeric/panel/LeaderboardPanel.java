package io.septem150.xeric.panel;

import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

@Singleton
public class LeaderboardPanel extends PanelBase {
  static final String TOOLTIP = "Leaderboard";
  static final String TAB_ICON = "/skill_icons/overall.png";

  @Inject
  private LeaderboardPanel() {
    super();
    BorderLayout layout = new BorderLayout();
    setLayout(layout);

    JPanel playerListPanel = new JPanel();
    BoxLayout playerListLayout = new BoxLayout(playerListPanel, BoxLayout.Y_AXIS);
    playerListPanel.setLayout(playerListLayout);

    add(playerListPanel, BorderLayout.CENTER);
  }
}
