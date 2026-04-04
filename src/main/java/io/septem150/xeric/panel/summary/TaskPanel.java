package io.septem150.xeric.panel.summary;

import io.septem150.xeric.data.player.PlayerInfo;
import io.septem150.xeric.data.task.Task;
import io.septem150.xeric.data.task.TaskType;
import io.septem150.xeric.util.ImageService;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import lombok.NonNull;
import net.runelite.client.ui.ColorScheme;

public class TaskPanel extends JPanel {
  private final Task task;

  private final transient PlayerInfo playerInfo;
  private final transient ImageService imageService;

  private final JLabel nameAndIconLabel = new JLabel();
  private final JCheckBox completedCheckbox = new JCheckBox();

  private boolean loaded;

  public TaskPanel(
      @NonNull Task task, @NonNull PlayerInfo playerInfo, @NonNull ImageService imageService) {
    this.task = task;
    this.playerInfo = playerInfo;
    this.imageService = imageService;
  }

  private void initComponents() {
    // set up name and icon label
    nameAndIconLabel.setText(task.getName());
    nameAndIconLabel.setIconTextGap(5);
    nameAndIconLabel.setIcon(new ImageIcon(imageService.getDefaultIcon(task.getType())));
    imageService.loadTaskIcon(
        task,
        image -> SwingUtilities.invokeLater(() -> nameAndIconLabel.setIcon(new ImageIcon(image))));
    nameAndIconLabel.setHorizontalAlignment(SwingConstants.LEFT);
    nameAndIconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));

    // set up completed checkbox
    completedCheckbox.setEnabled(false);
    completedCheckbox.setSelected(playerInfo.getCompletedTasks().contains(task));
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
    completedCheckbox.setSelected(playerInfo.getCompletedTasks().contains(task));
    revalidate();
  }

  public boolean isCompleted() {
    return completedCheckbox.isSelected();
  }

  public String getTaskName() {
    return task.getName();
  }

  public TaskType getTaskType() {
    return task.getType();
  }
}
