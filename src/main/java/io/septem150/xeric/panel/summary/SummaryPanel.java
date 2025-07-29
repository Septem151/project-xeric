package io.septem150.xeric.panel.summary;

import io.septem150.xeric.ProjectXericManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
@Singleton
public class SummaryPanel extends JPanel {
  public static final String TOOLTIP = "Player Summary";
  public static final String TAB_ICON = "summary_tab_icon.png";

  private final ProjectXericManager manager;
  private final TaskListPanel taskListPanel;
  private final IdCard idCard;

  @Inject
  private SummaryPanel(ProjectXericManager manager, TaskListPanel taskListPanel, IdCard idCard) {
    this.manager = manager;
    this.taskListPanel = taskListPanel;
    this.idCard = idCard;
    makeLayout();
    makeStaticData();
  }

  private static final String LOGGED_OUT_CONSTRAINT = "LOGGED_OUT";
  private static final String LOGGED_IN_CONSTRAINT = "LOGGED_IN";
  private final CardLayout layout = new CardLayout();
  private final JLabel loginLabel = new JLabel();
  private final JLabel clogLabel = new JLabel();

  private void makeLayout() {
    removeAll();
    setLayout(layout);
    JPanel loggedOutPanel = new JPanel(new BorderLayout());
    loginLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
    loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loggedOutPanel.add(loginLabel, BorderLayout.NORTH);
    add(loggedOutPanel, LOGGED_OUT_CONSTRAINT);
    JPanel loggedInPanel = new JPanel(new BorderLayout());
    idCard.setBorder(new EmptyBorder(0, 0, 10, 0));
    loggedInPanel.add(idCard, BorderLayout.NORTH);
    JPanel inner1 = new JPanel(new BorderLayout());
    clogLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
    clogLabel.setHorizontalAlignment(SwingConstants.CENTER);
    inner1.add(clogLabel, BorderLayout.NORTH);
    inner1.add(taskListPanel, BorderLayout.CENTER);
    loggedInPanel.add(inner1, BorderLayout.CENTER);
    add(loggedInPanel, LOGGED_IN_CONSTRAINT);
  }

  private void makeStaticData() {
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    loginLabel.setText(
        "<html><body style='text-align: center'>Log in to start tracking Xeric"
            + " Tasks.</body></html>");
    loginLabel.setFont(FontManager.getRunescapeSmallFont());
    loginLabel.setForeground(ColorScheme.BRAND_ORANGE);
    clogLabel.setText(
        "<html><body style='text-align: center'>Open the Collection Log to start tracking Xeric"
            + " Tasks.</body></html>");
    clogLabel.setFont(FontManager.getRunescapeSmallFont());
    clogLabel.setForeground(ColorScheme.BRAND_ORANGE);
  }

  public void startUp() {}

  public void refresh() {
    log.debug("reloading SummaryPanel");
    makeLayout();
    makeStaticData();
    if (manager.getPlayerInfo().getUsername() == null) {
      layout.show(this, LOGGED_OUT_CONSTRAINT);
    } else {
      layout.show(this, LOGGED_IN_CONSTRAINT);
      clogLabel.setVisible(manager.getPlayerInfo().getCollectionLog().getLastOpened() == null);
      idCard.reload();
      taskListPanel.reload();
    }
    revalidate();
  }
}
