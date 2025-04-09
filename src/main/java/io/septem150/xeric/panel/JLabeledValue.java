package io.septem150.xeric.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Optional;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class JLabeledValue extends JPanel {
  private final JLabel value;
  private final JLabel label;

  public JLabeledValue(String value, String label) {
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

  public JLabeledValue(int value, String label) {
    this(String.valueOf(value), label);
  }

  public JLabeledValue() {
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
