package org.irmacard.irma_kiosk;


public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IRMAKiosk ik = new IRMAKiosk();
        Thread t = new Thread(ik);
		t.run();

        System.exit(0);
    }

} 
