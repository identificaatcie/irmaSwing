package org.irmacard.irma_kiosk;

import javax.swing.*;

/**
 * Created by wietse on 30/11/15.
 */
public class ProgressPanel extends JPanel{

    private JTextArea progress;
    private JButton done;

    public ProgressPanel(IRMAKiosk irmaKiosk)
    {
        done = new JButton("Done");
        progress = new JTextArea();
        this.add(progress);
        this.add(done);
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
