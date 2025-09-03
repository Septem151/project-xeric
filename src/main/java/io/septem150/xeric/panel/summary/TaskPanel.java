package io.septem150.xeric.panel.summary;

import io.septem150.xeric.data.ProjectXericManager;
import io.septem150.xeric.data.task.Task;
import java.awt.*;
import javax.swing.*;
import lombok.NonNull;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;

public class TaskPanel extends JPanel {
  private final Task task;

  private final transient ProjectXericManager manager;
  private final transient SpriteManager spriteManager;

  private final JLabel nameAndIconLabel = new JLabel();
  private final JCheckBox completedCheckbox = new JCheckBox();

  private boolean loaded;

  public TaskPanel(
      @NonNull Task task,
      @NonNull ProjectXericManager manager,
      @NonNull SpriteManager spriteManager) {
    this.task = task;
    this.manager = manager;
    this.spriteManager = spriteManager;
  }

  private void initComponents() {
    // set up name and icon label
    nameAndIconLabel.setText(task.getName());
    nameAndIconLabel.setIconTextGap(5);
    nameAndIconLabel.setIcon(new ImageIcon(task.getIcon(spriteManager)));
    nameAndIconLabel.setHorizontalAlignment(SwingConstants.LEFT);
    nameAndIconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));

    // set up completed checkbox
    completedCheckbox.setEnabled(false);
    completedCheckbox.setSelected(manager.getPlayerInfo().getTasks().contains(task));
    completedCheckbox.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

    // setup parent panel and add components to layout
    setBackground(ColorScheme.DARKER_GRAY_COLOR);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(nameAndIconLabel);
    add(Box.createHorizontalGlue());
    add(completedCheckbox, BorderLayout.EAST);
  }

  public void startUp() {
    if (!loaded) {
      removeAll();
      initComponents();
      loaded = true;
    }
    refresh();
  }

  public void refresh() {
    completedCheckbox.setSelected(manager.getPlayerInfo().getTasks().contains(task));
    revalidate();
  }

  public boolean isCompleted() {
    return completedCheckbox.isSelected();
  }

  public String getTaskName() {
    return task.getName();
  }

  public String getTaskType() {
    return task.getType().getName();
  }
}
