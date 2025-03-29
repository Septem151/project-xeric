package io.septem150.xeric.panel;

import io.septem150.xeric.ProjectXericManager;
import java.awt.Color;
import java.awt.GridBagLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class IdCardPanel extends JPanel {

  @Inject
  private IdCardPanel(ProjectXericManager manager) {
    super(new GridBagLayout());
    setBorder(new LineBorder(Color.RED, 1));
  }
}
