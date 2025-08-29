package io.septem150.xeric.util;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class JLabeledValuePanel extends JPanel {
  private final JLabel value;
  private final JLabel label;

  public JLabeledValuePanel(String value, String label) {
    setLayout(new BorderLayout());

    final JPanel labeledValue = new JPanel();
    labeledValue.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    labeledValue.setLayout(new BoxLayout(labeledValue, BoxLayout.Y_AXIS));
    labeledValue.setAlignmentY(Component.CENTER_ALIGNMENT);

    this.value = new JLabel(Optional.ofNullable(value).orElse(""));
    this.value.setForeground(Color.WHITE);
    this.value.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.value.setFont(FontManager.getDefaultFont().deriveFont(Font.BOLD, 12));
    labeledValue.add(this.value);

    this.label = new JLabel(Optional.ofNullable(label).orElse(""));
    this.label.setForeground(ColorScheme.TEXT_COLOR);
    this.label.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.label.setFont(FontManager.getDefaultFont().deriveFont(Font.PLAIN, 10));
    labeledValue.add(this.label);

    add(labeledValue, BorderLayout.CENTER);
  }

  public JLabeledValuePanel() {
    this(null, null);
  }

  public void setValue(int value) {
    setValue(String.valueOf(value));
  }

  public void setValue(String value) {
    this.value.setText(value);
  }

  public void setLabel(String label) {
    this.label.setText(label);
  }
}
