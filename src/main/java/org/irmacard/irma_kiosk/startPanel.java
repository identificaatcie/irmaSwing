package org.irmacard.irma_kiosk;


import java.awt.*;

import javax.swing.*;

public class startPanel extends JPanel {

	/**
	 * 
	 * 
	 */
	private static final long serialVersionUID = 7630860552149910776L;
	private JButton button;
/**
 * @param irmaKiosk
 */

	public startPanel(IRMAKiosk irmaKiosk) {
        JLabel text = new JLabel("Please insert your card and press START");
		JButton button = new JButton("START");
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(boxLayout);
        this.add(text);
		this.add(button);
        this.setVisible(true);
		button.addActionListener(irmaKiosk);
	}


}