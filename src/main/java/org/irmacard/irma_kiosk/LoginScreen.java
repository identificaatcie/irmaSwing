package org.irmacard.irma_kiosk;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class LoginScreen extends JFrame {
private IRMAKiosk controller;
	public LoginScreen(IRMAKiosk irmaKiosk) {
		super();
		Dimension d = new Dimension();
		JTextField textfield = new JTextField("Please enter your IRMA card in the cardreader");
		textfield.setEditable(false);
		this.add(textfield); 
		d.setSize(500,500); // lelijke hack voor fullscreen.
		this.setSize(d);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.controller = irmaKiosk;
		}

	public Object getButton() {
		// TODO Auto-generated method stub
		return null; 
	}
	

}
