package org.irmacard.irma_kiosk;


import java.awt.*;

import javax.swing.*;

public class StartPanel extends JPanel {

	private JButton button;
/**
 * @param irmaKiosk
 */

	public StartPanel(IRMAKiosk irmaKiosk) {
        JLabel text = new JLabel("Please insert your card and press START");
        text.setMinimumSize(new Dimension(1000,200));
        text.setPreferredSize(new Dimension(1000,200));
        text.setMaximumSize(new Dimension(1000,200));
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setFont(IrmaFrame.irmaFont);
		JButton button = new JButton("START");
        button.setFont(IrmaFrame.irmaFont);
        button.setMinimumSize(new Dimension(500,500));
        button.setPreferredSize(new Dimension(500,500));
        button.setMaximumSize(new Dimension(500,500));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(boxLayout);
        this.add(text);
		this.add(button);
        this.setVisible(true);
		button.addActionListener(irmaKiosk);
	}


}