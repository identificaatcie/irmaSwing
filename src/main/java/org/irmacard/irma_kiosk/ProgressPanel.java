package org.irmacard.irma_kiosk;

import javax.swing.*;
import java.awt.*;

/**
 * Created by wietse on 30/11/15.
 */
public class ProgressPanel extends JPanel{

    private JTextArea progress;
    private JButton done;

    public ProgressPanel(IRMAKiosk irmaKiosk)
    {
        done = new JButton("Done");
        JButton done = new JButton("Enter");
        done.setMinimumSize(new Dimension(200,100));
        done.setPreferredSize(new Dimension(200,100));
        done.setMaximumSize(new Dimension(200,100));
        done.setFont(IrmaFrame.irmaFont);
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress = new JTextArea();
        this.add(progress);
        this.add(done);
        progress.setFont(IrmaFrame.irmaFont);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setVisible(true);
        done.addActionListener(irmaKiosk);
    }

    public void addLine(String line)
    {
        progress.append(line + "\n");
        invalidate();
    }
}
