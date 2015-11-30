package org.irmacard.irma_kiosk;

import com.google.api.client.json.JsonObjectParser;
import sun.swing.SwingAccessor;

import javax.swing.*;

/**
 * Created by wietse on 30/11/15.
 */
public class PINPanel extends JPanel {

    private JPasswordField PIN;

    public PINPanel(IRMAKiosk irmaKiosk)
    {
        JLabel instructions = new JLabel("Please enter your PIN code");
        PIN = new JPasswordField(4);
        JButton done = new JButton("Enter");
        this.add(instructions);
        this.add(PIN);
        this.add(done);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setVisible(true);

        done.addActionListener(irmaKiosk);

    }

    public String getPassword()
    {
        return new String(PIN.getPassword());
    }
}
