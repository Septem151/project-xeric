package io.septem150.xeric.panel;

import io.septem150.xeric.util.ResourceManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

/** Side Panel UI for the Project Xeric plugin. */
@Singleton
public class ProjectXericPanel extends PluginPanel {
  private static final String SIDEPANEL_TOOLTIP = "Project Xeric";
  private static final String SIDEPANEL_ICON = "sidepanel_icon.png";
  private static final int SIDEPANEL_PRIORITY = 3;

  private final ClientToolbar clientToolbar;
  private final NavigationButton navigationButton;
  private final SummaryPanel summaryPanel;

  @Inject
  private ProjectXericPanel(
      EventBus eventBus,
      ClientToolbar clientToolbar,
      SummaryPanel summaryPanel,
      LeaderboardPanel leaderboardPanel) {
    this.clientToolbar = clientToolbar;
    navigationButton =
        NavigationButton.builder()
            .tooltip(SIDEPANEL_TOOLTIP)
            .icon(ResourceManager.getImage(SIDEPANEL_ICON))
            .priority(SIDEPANEL_PRIORITY)
            .panel(this)
            .build();
    this.summaryPanel = summaryPanel;

    setLayout(new BorderLayout());
    setBackground(ColorScheme.DARK_GRAY_COLOR);
    setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel layoutPanel = new JPanel();
    layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));

    JPanel titlePanel = createTitlePanel();
    layoutPanel.add(titlePanel);

    JPanel display = new JPanel();
    MaterialTabGroup tabGroup = new MaterialTabGroup(display);
    tabGroup.setLayout(new GridLayout(1, 0, 7, 7));

    MaterialTab summaryTab =
        createTab(SummaryPanel.TOOLTIP, SummaryPanel.TAB_ICON, summaryPanel, tabGroup);
    eventBus.register(summaryPanel);
    createTab(LeaderboardPanel.TOOLTIP, LeaderboardPanel.TAB_ICON, leaderboardPanel, tabGroup);
    eventBus.register(leaderboardPanel);
    layoutPanel.add(tabGroup);

    add(layoutPanel, BorderLayout.NORTH);
    add(display, BorderLayout.CENTER);

    tabGroup.select(summaryTab);
  }

  /**
   * Creates a new {@link MaterialTab} with a given image and tooltip text. The {@code imageName} is
   * used as the tab's icon via {@link ResourceManager#getImage(String imageName)}.
   *
   * @param tooltip the tooltip to display on hover.
   * @param imageName the name of an image, including extension.
   * @param content a class extending from {@link PluginPanel} to display when the tab is selected.
   * @param tabGroup the {@link MaterialTabGroup} to assign the newly created tab to.
   * @return a new {@link MaterialTab} with the desired properties.
   */
  private MaterialTab createTab(
      String tooltip, String imageName, PluginPanel content, MaterialTabGroup tabGroup) {
    MaterialTab tab =
        new MaterialTab(new ImageIcon(ResourceManager.getImage(imageName)), tabGroup, content);
    tab.setToolTipText(tooltip);
    tabGroup.addTab(tab);
    return tab;
  }

  /**
   * Creates a {@link JPanel} that contains the plugin's title and social media buttons.
   *
   * @return a new {@link JPanel}.
   */
  private JPanel createTitlePanel() {
    JPanel titlePanel = new JPanel(new BorderLayout());
    titlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));

    JLabel title = new JLabel("Project Xeric");
    title.setForeground(Color.WHITE);
    titlePanel.add(title, BorderLayout.WEST);

    JPanel infoButtons = new JPanel(new GridLayout(1, 2, 10, 0));

    JButton discordButton =
        createTitleButton(
            "Join the Zeah Ironman Discord Server",
            "discord_icon.png",
            "https://discord.gg/q73k9Dn");
    infoButtons.add(discordButton);

    JButton githubButton =
        createTitleButton(
            "View the plugin's Source Code on GitHub",
            "github_icon.png",
            "https://github.com/Septem151/project-xeric");
    infoButtons.add(githubButton);

    titlePanel.add(infoButtons, BorderLayout.EAST);

    return titlePanel;
  }

  /**
   * Creates a {@link JButton} with a given image, tooltip text, and URL. The {@code imageName} is
   * used as the tab's icon via {@link ResourceManager#getImage(String imageName)}. Attempts to open
   * a new browser tab to the provided URL on click.
   *
   * @param tooltip the tooltip to display on hover.
   * @param imageName the name of an image, including extension.
   * @param url the URL to open when the button is clicked.
   * @return a new {@link JButton} with the desired properties.
   */
  private JButton createTitleButton(String tooltip, String imageName, String url) {
    JButton button = new JButton(new ImageIcon(ResourceManager.getImage(imageName, 16, 16)));
    SwingUtil.removeButtonDecorations(button);
    button.setToolTipText(tooltip);
    button.setBackground(ColorScheme.DARK_GRAY_COLOR);
    button.setUI(new BasicButtonUI());
    button.setFocusable(false);
    button.addActionListener(event -> LinkBrowser.browse(url));
    button.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            button.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            button.setBackground(ColorScheme.DARK_GRAY_COLOR);
          }
        });
    return button;
  }

  /** Adds this Side Panel to the RuneLite client toolbar */
  public void init() {
    clientToolbar.addNavigation(navigationButton);
  }

  /** Removes this Side Panel from the RuneLite client toolbar */
  public void stop() {
    clientToolbar.removeNavigation(navigationButton);
  }

  public void reload() {
    summaryPanel.reload();
    revalidate();
  }
}
