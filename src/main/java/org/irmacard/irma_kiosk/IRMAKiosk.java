package org.irmacard.irma_kiosk;

import java.awt.event.ActionEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.TerminalCardService;
import org.irmacard.credentials.Attributes;
import org.irmacard.credentials.CredentialsException;
import org.irmacard.credentials.idemix.IdemixCredentials;
import org.irmacard.credentials.idemix.IdemixSecretKey;
import org.irmacard.credentials.idemix.descriptions.IdemixVerificationDescription;
import org.irmacard.credentials.idemix.info.IdemixKeyStore;
import org.irmacard.credentials.idemix.smartcard.CardChangedListener;
import org.irmacard.credentials.idemix.smartcard.IRMACard;
import org.irmacard.credentials.idemix.smartcard.IRMACardHelper;
import org.irmacard.credentials.idemix.smartcard.SmartCardEmulatorService;
import org.irmacard.credentials.info.CredentialDescription;
import org.irmacard.credentials.info.DescriptionStore;
import org.irmacard.credentials.info.InfoException;
import org.irmacard.idemix.IdemixService;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;


import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.List;

/**
 * Created by wietse on 21-9-15.
 */
import java.awt.event.ActionListener;

import javax.swing.JFrame;
public class IRMAKiosk implements ActionListener {
private  JFrame irmaFrame, LoginScreen, logScreen, CredentialScreen, PinScreen;
private StringBuilder pinCode,x ;

private HttpTransport transport;
private JsonObjectParser jsonObjectParser;
private String apikey = "";
private final Boolean debug = true;
private CardService cs;
private IRMACard card;
private JsonObject result;
private String PIN;
private CardTerminal terminal;

public IRMAKiosk() 
	{
		irmaFrame = new irmaFrame(this);
		LoginScreen = new LoginScreen(this);
		PinScreen = new PinScreen(this);
		CredentialScreen = new CredentialScreen(this);
		logScreen = new logScreen(this);
		pinCode = new StringBuilder();
		x = new StringBuilder();


        transport = new NetHttpTransport.Builder().build();

        URI core = new File(System.getProperty("user.dir")).toURI().resolve("irma_configuration/");


        Path apikeyPath = Paths.get(System.getProperty("user.dir") + "/apikey");
        try {
            apikey = Files.readAllLines(apikeyPath).get(0);
        } catch (IOException e) {
            System.out.println("Apikey file could not be read.");
        }


        DescriptionStore.setCoreLocation(core);
        IdemixKeyStore.setCoreLocation(core);

        //Debug setup
        PIN="0000";
        if(debug)
        {
                card = new IRMACard();
                cs = new SmartCardEmulatorService(card);
                PIN = "0000";
                IssueThaliaRoot(cs,card);
                IssueSurfnetRoot(cs, card);
        }
        else
        {
            try {
                cs = getNewCardService();
            } catch (CardException e) {
                e.printStackTrace();
            }
        }

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("START"))
			{
				System.out.println("Test");
				irmaFrame.setVisible(false);
				LoginScreen.setVisible(true);
			}
		if("0123456789".contains(e.getActionCommand())&& e.getActionCommand().length()== 1)
		{
			if(pinCode.length()<4){
			pinCode.append(e.getActionCommand());
			x.append("x");
			((PinScreen) PinScreen).getTextfield().setText(x.toString());
			}
		}
		if(e.getActionCommand().equals("enter"))
		{
			System.out.println(pinCode);
			}
		if(e.getActionCommand().equals("C"))
		{
			if(pinCode.length() >0)
			{
				pinCode.deleteCharAt(pinCode.length()-1);
				x.deleteCharAt(x.length()-1);
				((PinScreen) PinScreen).getTextfield().setText(x.toString());
			}
		}
	}

    public CardService getPCSCCardService() throws InfoException, IllegalArgumentException {
        List<CardTerminal> terminalList;
        try {
            terminalList = TerminalFactory.getDefault().terminals().list();
        } catch (CardException e) {
            throw new InfoException("No card readers connected.");
        }
        if(!terminalList.isEmpty()) {
            int readerNr = 0;

            try {
                terminal = terminalList.get(readerNr);
                return new TerminalCardService(terminal);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid PCSC reader Nr: pcsc://" + readerNr);
            }
        } else {
            return null; // TODO error handling
        }
    }


    public CardService getNewCardService() throws CardException {
//        final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
        // For emulated card...
//        IRMACard card =  new IRMACard();
//        SmartCardEmulatorService emu = new SmartCardEmulatorService(card);
//        return emu;

        try {
            return getPCSCCardService();
        } catch (InfoException e) {
            e.printStackTrace();
            return null;
        }

        // For real pcsc smartcard



//        emu.addListener(new CardChangedListener() {
//            @Override
//            public void cardChanged(IRMACard card) {
//                IRMACardHelper.storeState(card, path);
//            }
//        });
    }
    public void issueThaliaCredentials(CardService cs, IRMACard card, JsonObject result,String PIN) {

        //Issue Membership attribute
        try {
            CredentialDescription cd = DescriptionStore.getInstance().
                    getCredentialDescriptionByName("Thalia", "membership");
            IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);
            // Setup the attributes that will be issued to the card
            Attributes attributes = new Attributes();
            String membership_type = result.get("membership_type").getAsString();
            if(membership_type.contains("Membership"))
            {
                attributes.add("isMember", "yes".getBytes());
            }
            else
            {
                attributes.add("isMember", "no".getBytes());
            }
            if(membership_type.contains("Honorary"))
            {
                attributes.add("isHonoraryMember", "yes".getBytes());
            }
            else
            {
                attributes.add("isHonoraryMember", "no".getBytes());
            }
            if(membership_type.contains("Benefactor"))
            {
                attributes.add("isBegunstiger", "yes".getBytes());
            }
            else
            {
                attributes.add("isBegunstiger", "no".getBytes());
            }
            // Setup a connection and send pin for emulated card service
            IdemixService is = new IdemixService(cs);
            IdemixCredentials ic = new IdemixCredentials(is);
            ic.connect();
            is.sendPin(PIN.getBytes());
            System.out.println("ISK± " + isk + "attributes± " + attributes + "cd± " + cd);
            ic.issue(cd, isk, attributes, null); // null indicates default expiry
//            final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
//            IRMACardHelper.storeState(card, path);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Issue over18 attribute
        try {
            CredentialDescription cd = DescriptionStore.getInstance().
                    getCredentialDescriptionByName("Thalia", "age");
            IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);
            // Setup the attributes that will be issued to the card
            Attributes attributes = new Attributes();
            String bday =  result.get("birthday").getAsString();
            System.out.println(bday);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime birthday = LocalDateTime.parse(bday, formatter);
            birthday = birthday.plusYears(18);
            if (birthday.isBefore(LocalDateTime.now()))
            {
                System.out.println("Je bent meerderjarig!");
                attributes.add("over18","yes".getBytes());
            }
            else
            {
                attributes.add("over18","no".getBytes());
            }
            // Setup a connection and send pin for emulated card service
            IdemixService is = new IdemixService(cs);
            IdemixCredentials ic = new IdemixCredentials(is);
            ic.connect();
            is.sendPin(PIN.getBytes());
            ic.issue(cd, isk, attributes, null); // null indicates default expiry
            final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
            IRMACardHelper.storeState(card, path);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void IssueThaliaRoot(CardService cs, IRMACard card) {

        try {
            CredentialDescription cd = DescriptionStore.getInstance().
                    getCredentialDescriptionByName("Thalia", "root");
            IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);


            // Setup the attributes that will be issued to the card
            Attributes attributes = new Attributes();
            attributes.add("userID", "wkuipers".getBytes());

            // Setup a connection and send pin for emulated card service
            IdemixService is = new IdemixService(cs);
            IdemixCredentials ic = new IdemixCredentials(is);
            ic.connect();

            is.sendPin("0000".getBytes());
            ic.issue(cd, isk, attributes, null); // null indicates default expiry


            final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
            IRMACardHelper.storeState(card, path);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void IssueSurfnetRoot(CardService cs, IRMACard card) {

        try {
            CredentialDescription cd = DescriptionStore.getInstance().
                    getCredentialDescriptionByName("Surfnet", "root");
            IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);


            // Setup the attributes that will be issued to the card
            Attributes attributes = new Attributes();
            attributes.add("userID", "s4317904@student.ru.nl".getBytes());
            attributes.add("securityHash", "DEADBEEF".getBytes());

            // Setup a connection and send pin for emulated card service
            IdemixService is = new IdemixService(cs);
            IdemixCredentials ic = new IdemixCredentials(is);
            ic.connect();

            is.sendPin("0000".getBytes());
            ic.issue(cd, isk, attributes, null); // null indicates default expiry


            // Setup a connection to a real card
//            CardService real = new TerminalCardService();   <--- doesn't exist?

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
