package io.septem150.xeric.panel;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.data.TaskType;
import io.septem150.xeric.task.TaskBase;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.IconTextField;

@Slf4j
@Singleton
public class TaskListPanel extends JPanel {
  private final PlayerData playerData;
  private final SpriteManager spriteManager;

  private List<TaskTierPanel> tierPanels = new ArrayList<>();

  private final IconTextField searchBar = new IconTextField();
  private final JToggleButton showCompletedButton = new JToggleButton();
  private final JPanel controlsContainerPanel = new JPanel();
  private final JPanel taskTiersContainerPanel = new JPanel();
  private final JScrollPane scrollContainerPanel = new JScrollPane();

  private boolean loaded;

  @Inject
  public TaskListPanel(PlayerData playerData, SpriteManager spriteManager) {
    this.playerData = playerData;
    this.spriteManager = spriteManager;
  }

  private void initComponents() {
    // set up search bar, toggle button, and tasks panel
    searchBar.setIcon(IconTextField.Icon.SEARCH);
    searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
    searchBar
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent documentEvent) {
                refresh();
              }

              @Override
              public void removeUpdate(DocumentEvent documentEvent) {
                refresh();
              }

              @Override
              public void changedUpdate(DocumentEvent documentEvent) {
                // do nothing, event not used
              }
            });
    for (TaskType taskType : TaskType.values()) {
      searchBar.getSuggestionListModel().addElement(taskType.getName().toUpperCase());
    }

    showCompletedButton.setIcon(
        new ImageIcon(ResourceUtil.getImage("show_completed_disabled.png")));
    showCompletedButton.setSelectedIcon(
        new ImageIcon(ResourceUtil.getImage("show_completed_enabled.png")));
    showCompletedButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    showCompletedButton.addActionListener(unused -> refresh());

    // setup parent panel and add components to layout
    controlsContainerPanel.setOpaque(false);
    controlsContainerPanel.setLayout(new BorderLayout(5, 0));
    controlsContainerPanel.add(searchBar, BorderLayout.CENTER);
    controlsContainerPanel.add(showCompletedButton, BorderLayout.EAST);

    taskTiersContainerPanel.setOpaque(false);
    taskTiersContainerPanel.setLayout(new BoxLayout(taskTiersContainerPanel, BoxLayout.Y_AXIS));

    scrollContainerPanel.setOpaque(false);
    scrollContainerPanel.setViewportView(taskTiersContainerPanel);
    scrollContainerPanel.setWheelScrollingEnabled(true);
    scrollContainerPanel.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollContainerPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    setOpaque(false);
    setLayout(new BorderLayout(0, 5));
    add(controlsContainerPanel, BorderLayout.NORTH);
    add(scrollContainerPanel, BorderLayout.CENTER);
  }

  public void startUp() {
    tierPanels =
        playerData.getAllTasks().stream()
            .map(TaskBase::getTier)
            .distinct()
            .sorted()
            .map(tier -> new TaskTierPanel(tier, playerData, spriteManager))
            .collect(Collectors.toList());
    if (!loaded) {
      removeAll();
      initComponents();
      loaded = true;
    }
    for (TaskTierPanel taskTierPanel : tierPanels) {
      taskTierPanel.startUp();
    }
    refresh();
  }

  public void refresh() {
    invalidate();
    scrollContainerPanel.setBorder(BorderFactory.createEmptyBorder());
    setBorder(BorderFactory.createEmptyBorder());
    String search = searchBar.getText();
    taskTiersContainerPanel.removeAll();
    for (int i = 0; i < tierPanels.size(); i++) {
      TaskTierPanel taskTierPanel = tierPanels.get(i);
      taskTierPanel.applyFilter(search, showCompletedButton.isSelected());
      if (taskTierPanel.isVisible()) {
        taskTiersContainerPanel.add(taskTierPanel);
        if (i < tierPanels.size() - 1) {
          taskTiersContainerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
      }
    }
    revalidate();
  }
}
