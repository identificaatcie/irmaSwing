package org.irmacard.irma_kiosk;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PinScreen extends JFrame {
	IRMAKiosk c;
	JTextField pinInput;
	public PinScreen(IRMAKiosk irmaKiosk) {
		c = irmaKiosk;
		this.setVisible(true);
		JPanel panel = new JPanel();
		fixPanel(panel);
		pinInput = new JTextField();
		pinInput.setEditable(false);
		pinInput.setPreferredSize(new Dimension(200,50));
		JLabel text = new JLabel("Enter your pincode");
		text.setFont(text.getFont().deriveFont(22.0f));
		text.setPreferredSize(new Dimension(200,200));
		FlowLayout j = new FlowLayout();
		this.setLayout(j);
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		double height = s.getHeight();		
		j.setAlignment(FlowLayout.CENTER);
		j.setVgap(((int)height/3));
		this.add(text);
		this.add(pinInput);
		this.setLayout(j);
		this.setSize(500, 500);
	}

	private void fixPanel(JPanel panel) {
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
		buttonlist.add(new JButton("enter"));
		buttonlist.add(new JButton("0"));
		buttonlist.add(new JButton("C"));
		for (JButton button : buttonlist)
		{
			button.setPreferredSize(new Dimension(90,130));
			button.addActionListener(c);
			panel.add(button);
			button.setFont(button.getFont().deriveFont(18.0f));
		}
		this.add(panel);
	}
	public JTextField getTextfield()
	{
		return pinInput;
	}
}
