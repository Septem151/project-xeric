package io.septem150.xeric.panel;

import io.septem150.xeric.ProjectXericPlugin;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;

@Singleton
public final class ProjectXericPanel extends PluginPanel {
  private static final String SIDEPANEL_IMAGE = "images/side_panel_icon.png";
  private static final String SIDEPANEL_TOOLTIP = "Project Xeric";
  private static final int SIDEPANEL_PRIORITY = 2;

  private final EventBus eventBus;
  private final ClientToolbar clientToolbar;
  private final MaterialTabGroup tabGroup;

  private NavigationButton navigationButton;
  private PluginPanel current;
  private final JPanel content;
  private final CardLayout layout;
  private boolean active;

  @Inject
  private ProjectXericPanel(
      EventBus eventBus,
      ClientToolbar clientToolbar,
      SummaryPanel summaryPanel,
      InfoPanel infoPanel) {
    super(false);
    this.eventBus = eventBus;
    this.clientToolbar = clientToolbar;

    content = new JPanel();
    layout = new CardLayout();
    content.setLayout(layout);

    tabGroup = new MaterialTabGroup();
    tabGroup.setLayout(new GridLayout(1, 0, 7, 7));
    tabGroup.setBorder(new EmptyBorder(10, 10, 0, 10));

    setBackground(ColorScheme.DARK_GRAY_COLOR);
    setLayout(new BorderLayout());
    add(tabGroup, BorderLayout.NORTH);
    add(content, BorderLayout.CENTER);

    MaterialTab summaryTab = createTab("Summary", "images/summary_tab_icon.png", summaryPanel);
    tabGroup.addTab(summaryTab);

    MaterialTab infoTab = createTab("Info", "images/info_tab_icon.png", infoPanel);
    tabGroup.addTab(infoTab);

    tabGroup.select(summaryTab);
  }

  public void init() {
    // Set Side Panel's Icon
    final BufferedImage icon =
        ImageUtil.loadImageResource(ProjectXericPlugin.class, SIDEPANEL_IMAGE);

    navigationButton =
        NavigationButton.builder()
            .tooltip(SIDEPANEL_TOOLTIP)
            .icon(icon)
            .priority(SIDEPANEL_PRIORITY)
            .panel(this)
            .build();
    clientToolbar.addNavigation(navigationButton);
  }

  public void stop() {
    clientToolbar.removeNavigation(navigationButton);
  }

  private MaterialTab createTab(String tooltip, String imagePath, PluginPanel panel) {
    MaterialTab tab =
        new MaterialTab(
            new ImageIcon(ImageUtil.loadImageResource(ProjectXericPlugin.class, imagePath)),
            tabGroup,
            panel);
    tab.setToolTipText(tooltip);
    content.add(panel.getWrappedPanel(), imagePath);
    eventBus.register(panel);
    tab.setOnSelectEvent(
        () -> {
          PluginPanel prevPanel = current;
          if (active) {
            if (prevPanel != null) {
              prevPanel.onDeactivate();
            }
            panel.onActivate();
          }
          current = panel;
          layout.show(content, imagePath);
          content.revalidate();
          return true;
        });
    return tab;
  }

  @Override
  public void onActivate() {
    active = true;
    if (current != null) {
      current.onActivate();
    }
  }

  @Override
  public void onDeactivate() {
    active = false;
    if (current != null) {
      current.onDeactivate();
    }
  }
}
