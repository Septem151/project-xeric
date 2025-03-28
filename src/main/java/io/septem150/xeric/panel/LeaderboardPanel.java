package io.septem150.xeric.panel;

import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import net.runelite.api.Client;

@Singleton
public class LeaderboardPanel extends PanelBase {
  public static final String TOOLTIP = "Leaderboard";
  public static final String TAB_ICON = "/skill_icons/overall.png";

  @Inject
  private LeaderboardPanel(Client client) {
    super();
    BorderLayout layout = new BorderLayout();
    setLayout(layout);

    JPanel playerListPanel = new JPanel();
    BoxLayout playerListLayout = new BoxLayout(playerListPanel, BoxLayout.Y_AXIS);
    playerListPanel.setLayout(playerListLayout);

    add(playerListPanel, BorderLayout.CENTER);
  }
}
