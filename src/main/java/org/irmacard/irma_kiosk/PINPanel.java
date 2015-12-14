package org.irmacard.irma_kiosk;

import com.google.api.client.json.JsonObjectParser;
import sun.swing.SwingAccessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * Created by wietse on 30/11/15.
 */
public class PINPanel extends JPanel implements ActionListener {

    private JPasswordField PIN;
    private StringBuilder stringBuilder;
    private JLabel pinProgress;

    public PINPanel(IRMAKiosk irmaKiosk)
    {
        stringBuilder = new StringBuilder();
        JLabel instructions = new JLabel("Please enter your PIN code");
        instructions.setMinimumSize(new Dimension(500,200));
        instructions.setPreferredSize(new Dimension(500, 200));
        instructions.setMaximumSize(new Dimension(500, 200));
        instructions.setFont(IrmaFrame.irmaFont);
        /*PIN = new JPasswordField(4);
        PIN.setFont(IrmaFrame.irmaFont);*/
        JPanel pinPanel = pinPanel();
        JButton done = new JButton("Enter");
        done.setMinimumSize(new Dimension(200,100));
        done.setPreferredSize(new Dimension(200,100));
        done.setMaximumSize(new Dimension(200,100));
        done.setFont(IrmaFrame.irmaFont);
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(instructions);
        this.add(pinPanel);
        pinProgress = new JLabel("");
        pinProgress.setMinimumSize(new Dimension(500,200));
        pinProgress.setPreferredSize(new Dimension(500,200));
        pinProgress.setMaximumSize(new Dimension(500,200));
        pinProgress.setFont(IrmaFrame.irmaFontLarge);
        pinProgress.setOpaque(true);
        pinProgress.setBackground(Color.WHITE);
        JPanel lowerBar = new JPanel(new FlowLayout());
        lowerBar.add(pinProgress);
        lowerBar.add(done);;
        this.add(lowerBar);
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setVisible(true);

        done.addActionListener(irmaKiosk);

    }

    private JPanel pinPanel() {
        JPanel panel = new JPanel();
        GridLayout g = new GridLayout(4,3);
        panel.setLayout(g);
        LinkedList<JButton> buttonlist = new LinkedList<JButton>();
        buttonlist.add(new JButton("1"));
        buttonlist.add(new JButton("2"));
        buttonlist.add(new JButton("3"));
        buttonlist.add(new JButton("4"));
        buttonlist.add(new JButton("5"));
        buttonlist.add(new JButton("6"));
        buttonlist.add(new JButton("7"));
        buttonlist.add(new JButton("8"));
        buttonlist.add(new JButton("9"));
        buttonlist.add(new JButton("0"));
        buttonlist.add(new JButton("C"));
        for (JButton button : buttonlist)
        {
            button.setPreferredSize(new Dimension(90,130));
            button.addActionListener(this);
            panel.add(button);
            button.setFont(button.getFont().deriveFont(18.0f));
        }
        return panel;
    }

    public String getPassword()
    {
            return stringBuilder.toString();
    }

    public boolean passwordReady(){
        return stringBuilder.length() == 4;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(actionEvent.getActionCommand() == "C")
        {
            stringBuilder = new StringBuilder();
            pinProgress.setText("");
        }
        else
        {
            if(pinProgress.getText().length() < 4)
            {
                stringBuilder.append(actionEvent.getActionCommand());
                pinProgress.setText(pinProgress.getText() + "*");
            }
        }
    }
}
