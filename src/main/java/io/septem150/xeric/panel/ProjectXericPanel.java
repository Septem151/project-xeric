package io.septem150.xeric.panel;

import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

public class ProjectXericPanel extends PluginPanel {
  private final JPanel display = new JPanel();

  private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);

  private final MaterialTab summaryTab;
  private final MaterialTab tasksTab;
  private final MaterialTab infoTab;

  @Getter private final ProjectXericSummaryPanel summaryPanel;

  @Getter private final ProjectXericTasksPanel tasksPanel;

  @Getter private final ProjectXericInfoPanel infoPanel;

  @Inject
  private ProjectXericPanel(
      ProjectXericSummaryPanel summaryPanel,
      ProjectXericTasksPanel tasksPanel,
      ProjectXericInfoPanel infoPanel) {
    super(false);

    this.summaryPanel = summaryPanel;
    this.tasksPanel = tasksPanel;
    this.infoPanel = infoPanel;

    setLayout(new BorderLayout());
    setBackground(ColorScheme.DARK_GRAY_COLOR);

    summaryTab = new MaterialTab("Summary", tabGroup, summaryPanel);
    tasksTab = new MaterialTab("Tasks", tabGroup, tasksPanel);
    infoTab = new MaterialTab("Info", tabGroup, infoPanel);

    tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
    tabGroup.addTab(summaryTab);
    tabGroup.addTab(tasksTab);
    tabGroup.addTab(infoTab);

    add(tabGroup, BorderLayout.NORTH);
    add(display, BorderLayout.CENTER);
  }
}
