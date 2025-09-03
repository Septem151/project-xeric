package io.septem150.xeric.panel.summary;

import io.septem150.xeric.data.ProjectXericManager;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskStore;
import io.septem150.xeric.data.task.TaskType;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;

@Slf4j
public class TaskTierPanel extends JPanel {
  @Getter private final int tier;
  private List<TaskPanel> taskPanels = new ArrayList<>();

  private final transient TaskStore taskStore;
  private final transient ProjectXericManager manager;
  private final transient SpriteManager spriteManager;

  private final JLabel tierAndCountLabel = new JLabel();
  private final JPanel tasksContainerPanel = new JPanel();

  private boolean loaded;

  public TaskTierPanel(
      int tier,
      @NonNull TaskStore taskStore,
      @NonNull ProjectXericManager manager,
      @NonNull SpriteManager spriteManager) {
    this.tier = tier;
    this.taskStore = taskStore;
    this.manager = manager;
    this.spriteManager = spriteManager;
  }

  private void initComponents() {
    // set up tier label and get count
    tierAndCountLabel.setText(
        String.format(
            "Tier %d Tasks (%d/%d)",
            tier,
            (int) taskPanels.stream().filter(TaskPanel::isCompleted).count(),
            taskPanels.size()));
    tierAndCountLabel.setHorizontalAlignment(SwingConstants.LEFT);
    tierAndCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    tierAndCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

    // set up tasks container
    tasksContainerPanel.setOpaque(false);
    tasksContainerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    tasksContainerPanel.setLayout(new BoxLayout(tasksContainerPanel, BoxLayout.Y_AXIS));
    tasksContainerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // setup parent panel and add components to layout
    setOpaque(false);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(tierAndCountLabel);
    add(Box.createRigidArea(new Dimension(0, 5)));
    add(tasksContainerPanel);
  }

  public void startUp() {
    taskPanels =
        taskStore.getAll().stream()
            .filter(task -> task.getTier() == tier)
            .sorted(Comparator.comparing(Task::getType).thenComparing(Task::getName))
            .map(task -> new TaskPanel(task, manager, spriteManager))
            .collect(Collectors.toList());
    if (!loaded) {
      removeAll();
      initComponents();
      loaded = true;
    }
    for (TaskPanel taskPanel : taskPanels) {
      taskPanel.startUp();
    }
    refresh();
  }

  private void refresh() {
    tierAndCountLabel.setText(
        String.format(
            "Tier %d Tasks (%d/%d)",
            tier,
            (int) taskPanels.stream().filter(TaskPanel::isCompleted).count(),
            taskPanels.size()));
    tasksContainerPanel.removeAll();
    for (int i = 0; i < taskPanels.size(); i++) {
      TaskPanel taskPanel = taskPanels.get(i);
      taskPanel.refresh();
      if (taskPanel.isVisible()) {
        tasksContainerPanel.add(taskPanel);
        if (i < taskPanels.size() - 1) {
          tasksContainerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
      }
    }
    revalidate();
  }

  public void applyFilter(String search, boolean showCompleted) {
    invalidate();
    int countShown = 0;
    for (TaskPanel taskPanel : taskPanels) {
      taskPanel.refresh();
      if ((taskPanel.getTaskType().equalsIgnoreCase(search)
              || (Arrays.stream(TaskType.values())
                      .noneMatch(taskType -> taskType.getName().equalsIgnoreCase(search)))
                  && taskPanel.getTaskName().toLowerCase().contains(search.toLowerCase()))
          && (showCompleted || !taskPanel.isCompleted())) {
        taskPanel.setVisible(true);
        countShown++;
        continue;
      }
      taskPanel.setVisible(false);
    }
    setVisible(countShown != 0);
    refresh();
  }
}
