package io.septem150.xeric.panel.summary;

import io.septem150.xeric.PlayerData;
import io.septem150.xeric.task.TaskBase;
import io.septem150.xeric.util.ResourceUtil;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

@Slf4j
@Singleton
public class TaskListPanel extends JPanel {
  private final JToggleButton showCompletedToggle = new JToggleButton();
  private final JPanel topPanel = new JPanel();
  private final JPanel tasksPanel = new JPanel();
  private final JScrollPane scrollPane = new JScrollPane();

  private final ClientThread clientThread;
  private final SpriteManager spriteManager;
  private final PlayerData playerData;

  private boolean loaded;

  @Inject
  private TaskListPanel(
      ClientThread clientThread, SpriteManager spriteManager, PlayerData playerData) {
    this.clientThread = clientThread;
    this.spriteManager = spriteManager;
    this.playerData = playerData;
  }

  public void refresh() {
    if (!playerData.isLoggedIn()) return;

    if (!loaded) {
      loaded = true;
      initComponents();
      updateTaskList();
    }
  }

  private void initComponents() {
    setLayout(new BorderLayout(0, 5));
    showCompletedToggle.setIcon(
        new ImageIcon(ResourceUtil.getImage("show_completed_disabled.png")));
    showCompletedToggle.setSelectedIcon(
        new ImageIcon(ResourceUtil.getImage("show_completed_enabled.png")));
    showCompletedToggle.addItemListener(event -> updateTaskList());
    topPanel.setLayout(new BorderLayout(5, 0));
    JLabel tempSearchLabel = new JLabel("Search Temporarily Disabled");
    tempSearchLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
    tempSearchLabel.setHorizontalAlignment(SwingConstants.CENTER);
    tempSearchLabel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    tempSearchLabel.setOpaque(true);
    topPanel.add(tempSearchLabel, BorderLayout.CENTER);
    topPanel.add(showCompletedToggle, BorderLayout.EAST);
    add(topPanel, BorderLayout.NORTH);
    tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
    scrollPane.setViewportView(tasksPanel);
    scrollPane.setWheelScrollingEnabled(true);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void updateTaskList() {
    tasksPanel.invalidate();
    tasksPanel.removeAll();
    Arrays.stream(tasksPanel.getComponents()).forEach(tasksPanel::remove);
    List<TaskBase> shownTasks =
        playerData.getAllTasks().stream()
            .filter(
                task ->
                    showCompletedToggle.isSelected()
                        || !playerData.getCompletedTasks().contains(task))
            .collect(Collectors.toList());
    if (shownTasks.isEmpty()) {
      tasksPanel.revalidate();
      return;
    }
    Map<Integer, List<TaskBase>> tasksPerTier =
        shownTasks.stream().collect(Collectors.groupingBy(TaskBase::getTier));
    List<Integer> tiers = tasksPerTier.keySet().stream().sorted().collect(Collectors.toList());
    for (int tierIndex = 0; tierIndex < tiers.size(); tierIndex++) {
      int tier = tiers.get(tierIndex);
      List<TaskBase> taskList = tasksPerTier.get(tier);
      int numCompleted =
          (int)
              playerData.getCompletedTasks().stream()
                  .filter(task -> task.getTier() == tier)
                  .count();
      taskList.sort(Comparator.comparing(TaskBase::getType).thenComparing(TaskBase::getName));
      JPanel tierPanel = new JPanel();
      tierPanel.setLayout(new BoxLayout(tierPanel, BoxLayout.Y_AXIS));
      tierPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
      JLabel tierLabel = new JLabel();
      tierLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
      tierLabel.setText(
          "<html><p style='text-align: left'>"
              + String.format(
                  "Tier %d Tasks (%d/%d)",
                  tier,
                  numCompleted,
                  (int)
                      playerData.getAllTasks().stream()
                          .filter(task -> task.getTier() == tier)
                          .count())
              + "</p>");
      tierPanel.add(tierLabel);
      tierPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      for (int taskIndex = 0; taskIndex < taskList.size(); taskIndex++) {
        TaskBase task = taskList.get(taskIndex);
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.X_AXIS));
        taskPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        taskPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        taskPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel taskLabel = new JLabel(task.getName());
        taskLabel.setHorizontalAlignment(SwingConstants.LEFT);
        clientThread.invokeLater(
            () -> taskLabel.setIcon(new ImageIcon(task.getIcon(spriteManager))));
        taskPanel.add(taskLabel);
        taskPanel.add(Box.createHorizontalGlue());
        JCheckBox completedCheckbox = new JCheckBox();
        completedCheckbox.setEnabled(false);
        completedCheckbox.setSelected(playerData.getCompletedTasks().contains(task));
        completedCheckbox.setBorder(new EmptyBorder(0, 5, 0, 0));
        taskPanel.add(completedCheckbox);
        tierPanel.add(taskPanel);
        if (taskIndex < taskList.size() - 1) {
          tierPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
      }
      if (tierIndex < tiers.size() - 1) {
        tierPanel.add(Box.createRigidArea(new Dimension(0, 10)));
      }
      tasksPanel.add(tierPanel);
    }
    tasksPanel.revalidate();
  }
}
