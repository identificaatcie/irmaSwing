package org.irmacard.irma_kiosk;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;

public class irmaFrame extends JFrame {

	/**
	 * 
	 * 
	 */
	private static final long serialVersionUID = 7630860552149910776L;
	private JButton button; 
/**
 * @param irmaKiosk
 */

	public irmaFrame(IRMAKiosk irmaKiosk) {
		this.setVisible(true);
		button = new JButton("START");
		Dimension d = new Dimension(300,200);
		button.setPreferredSize(d);
		FlowLayout g = new FlowLayout();
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		double height = s.getHeight();		
		g.setAlignment(FlowLayout.CENTER);
		g.setVgap(((int)height/2)-100);
		this.setLayout(g);
		g.addLayoutComponent("button", button);
		this.add(button);
		button.addActionListener(irmaKiosk);
		this.setSize(500, 500);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);		
	}
	public JButton getButton()
	{
		return button;
	}
}