package org.irmacard.irma_kiosk;

import java.awt.*;
import java.awt.event.ActionEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import net.sf.scuba.smartcards.TerminalCardService;
import org.irmacard.credentials.Attributes;
import org.irmacard.credentials.CredentialsException;
import org.irmacard.credentials.idemix.IdemixCredentials;
import org.irmacard.credentials.idemix.IdemixSecretKey;
import org.irmacard.credentials.idemix.descriptions.IdemixVerificationDescription;
import org.irmacard.credentials.idemix.info.IdemixKeyStore;
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

public class IRMAKiosk implements ActionListener, Runnable {

    private JFrame irmaFrame;
    private ProgressPanel progressPanel;
    private PINPanel pinPanel;

    private HttpTransport transport;
    private JsonObjectParser jsonObjectParser;
    private String apikey = "";
    private final Boolean debug = false; // true for json, false for hardware
    private CardService cs;
    private IRMACard card;
    private JsonObject result;
    private String PIN;
    private CardTerminal terminal;
    private volatile boolean progress = false;

    public IRMAKiosk() {

    }

    public void run()
    {
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        irmaFrame = new IrmaFrame();
        //graphicsDevice.setFullScreenWindow(irmaFrame);

        StartPanel startPanel = new StartPanel(this);
        irmaFrame.add(startPanel);
        irmaFrame.invalidate();
        irmaFrame.setVisible(true);
        waitOnProgress();
        irmaFrame.remove(startPanel);
        pinPanel = new PINPanel(this);
        irmaFrame.add(pinPanel);
        irmaFrame.invalidate();
        irmaFrame.setVisible(true);
        waitOnProgress();
        PIN = pinPanel.getPassword();
        irmaFrame.remove(pinPanel);
        progressPanel = new ProgressPanel(this);
        irmaFrame.add(progressPanel);
        irmaFrame.invalidate();
        irmaFrame.setVisible(true);


        transport = new NetHttpTransport.Builder().build();
        URI core = new File(System.getProperty("user.dir")).toURI().resolve("irma_configuration/");
        Path apikeyPath = Paths.get(System.getProperty("user.dir") + "/apikey");
        try {
            apikey = Files.readAllLines(apikeyPath).get(0);
        } catch (IOException e) {
            progressPanel.addLine("Apikey file could not be read.");
            waitOnProgress();
            return;
        }

        DescriptionStore.setCoreLocation(core);
        IdemixKeyStore.setCoreLocation(core);

        //Debug setup
        if (debug) {
            card = new IRMACard();
            cs = new SmartCardEmulatorService(card);
            PIN = "0000";
            try
            {
                issueThaliaRoot("wkuipers", cs, card);
                IssueSurfnetRoot(cs, card);
            } catch (CredentialsException e) {
                progressPanel.addLine("Issuing failed. Perhaps the PIN code was wrong.");
                waitOnProgress();
                return;
            } catch (InfoException e) {
                progressPanel.addLine("Issuing failed. Contact the Identificaatcie");
                waitOnProgress();
                return;
            } catch (CardServiceException e) {
                progressPanel.addLine("Issuing failed. Contact the Identificaatcie");
                waitOnProgress();
                return;
            }


        }
        else {
            try {
                cs = getNewCardService();
            } catch (CardException e) {
                e.printStackTrace();
            }
        }

        if(cs == null)
        {
            progressPanel.addLine("Cardreader problem: Did you insert your card?");
            waitOnProgress();
            return;
        }
        try {
            progressPanel.addLine("Verifying Thalia root...");
            try {
                result = verifyThaliaRoot(cs);
            } catch (InfoException e) {
                progressPanel.addLine("Verifying failed with InfoException. Contact the identificaatcie");
                waitOnProgress();
                return;
            } catch (IOException e) {
                progressPanel.addLine("Verifying failed with IOException Contact the identificaatcie");
                waitOnProgress();
                return;
            }
        } catch (CredentialsException e) {
            progressPanel.addLine("Verification of Thalia Root failed. Perhaps the PIN code was wrong.");
            waitOnProgress();
            return;
        }

        if(result == null)
        {
            progressPanel.addLine("Failed to verify by thalia root. Verifying by surfnet root.");
            try {
                progressPanel.addLine("Verifying by Surfnet root...");
                try {
                    result = verifySurfnetRoot(cs);
                } catch (InfoException e) {
                    progressPanel.addLine("Verifying failed with InfoException. Contact the identificaatcie");
                    waitOnProgress();
                    return;
                } catch (IOException e) {
                    progressPanel.addLine("Verifying failed with IOException Contact the identificaatcie");;
                    waitOnProgress();
                    return;
                }
            } catch (CredentialsException e) {
                progressPanel.addLine("Verification of Surfnet Root failed. Perhaps the PIN code was wrong.");
                waitOnProgress();
                return;
            }
            if(result == null)
            {
                progressPanel.addLine("Failed to verify by surfnet root. Ask the identificaatcie to fix your root credentials.");
                waitOnProgress();
                return;
            }
        }
        progressPanel.addLine("Verification succeeded!");
        try {
            progressPanel.addLine("Issuing Thalia credentials...");
            issueThaliaRoot(result.get("username").getAsString(), cs, card);
            issueThaliaCredentials(cs, card, result);
            IssueThalia(cs, card, result);
        } catch (CredentialsException e) {
            progressPanel.addLine("Issuing failed. Perhaps the PIN code was wrong.");
            waitOnProgress();
            return;
        } catch (InfoException e) {
            progressPanel.addLine("Issuing failed. Contact the Identificaatcie");
            waitOnProgress();
            return;
        } catch (CardServiceException e) {
            progressPanel.addLine("Issuing failed. Contact the Identificaatcie");
            waitOnProgress();
            return;
        }

        progressPanel.addLine("Issues succesful!");
        progressPanel.addLine("Done!");
        waitOnProgress();
    }

    public void waitOnProgress()
    {
        while(!progress)
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        progress = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("START")) {
            progress = true;
        }
        else if (e.getActionCommand().equals("Enter"))
        {
            progress = pinPanel.passwordReady();
        }
        else if (e.getActionCommand().equals("Done"))
        {
            progress = true;
        }
    }

    public CardService getPCSCCardService() throws InfoException, IllegalArgumentException {
        List<CardTerminal> terminalList;
        try {
            terminalList = TerminalFactory.getDefault().terminals().list();
        } catch (CardException e) {
            throw new InfoException("No card readers connected.");
        }
        if (!terminalList.isEmpty()) {
            int readerNr = 0;

            try {
                terminal = terminalList.get(readerNr);
				if (!terminal.isCardPresent()) {
					progressPanel.add("Waiting for insertion of card");
					while (!terminal.waitForCardPresent(1000)) {
						progressPanel.add(".");
					}
					progressPanel.addLine("");
				}
                return new TerminalCardService(terminal);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid PCSC reader Nr: pcsc://" + readerNr);
            } catch (CardException e){
				throw new InfoException("Card Exception" + e.getMessage());
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

    public void issueThaliaCredentials(CardService cs, IRMACard card, JsonObject result) throws CredentialsException, InfoException, CardServiceException {

        //Issue Membership attribute
        CredentialDescription cd = DescriptionStore.getInstance().
                getCredentialDescriptionByName("Thalia", "membership");
        IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);
        // Setup the attributes that will be issued to the card
        Attributes attributes = new Attributes();
        String membership_type = result.get("membership_type").getAsString();
        if (membership_type.contains("Membership")) {
            attributes.add("isMember", "yes".getBytes());
        } else {
            attributes.add("isMember", "no".getBytes());
        }
        if (membership_type.contains("Honorary")) {
            attributes.add("isHonoraryMember", "yes".getBytes());
        } else {
            attributes.add("isHonoraryMember", "no".getBytes());
        }
        if (membership_type.contains("Benefactor")) {
            attributes.add("isBegunstiger", "yes".getBytes());
        } else {
            attributes.add("isBegunstiger", "no".getBytes());
        }
        // Setup a connection and send pin for emulated card service
        IdemixService is = new IdemixService(cs);
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();
        is.sendPin(PIN.getBytes());
        ic.issue(cd, isk, attributes, null); // null indicates default expiry
//            final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
//            IRMACardHelper.storeState(card, path);

        //Issue over18 attribute
        cd = DescriptionStore.getInstance().
                getCredentialDescriptionByName("Thalia", "age");
        isk = IdemixKeyStore.getInstance().getSecretKey(cd);
        // Setup the attributes that will be issued to the card
        attributes = new Attributes();
        String bday = result.get("birthday").getAsString();
        progressPanel.addLine(bday);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime birthday = LocalDateTime.parse(bday, formatter);
        birthday = birthday.plusYears(18);
        if (birthday.isBefore(LocalDateTime.now())) {
            attributes.add("over18", "yes".getBytes());
        } else {
            attributes.add("over18", "no".getBytes());
        }
        // Setup a connection and send pin for emulated card service
        is = new IdemixService(cs);
        ic = new IdemixCredentials(is);
        ic.connect();
        is.sendPin(PIN.getBytes());
        ic.issue(cd, isk, attributes, null); // null indicates default expiry
        final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
        IRMACardHelper.storeState(card, path);

    }

    public void issueThaliaRoot(String username, CardService cs, IRMACard card) throws CredentialsException, InfoException, CardServiceException {

        CredentialDescription cd = DescriptionStore.getInstance().
                getCredentialDescriptionByName("Thalia", "root");
        IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);


        // Setup the attributes that will be issued to the card
        Attributes attributes = new Attributes();
        attributes.add("userID", username.getBytes());

        // Setup a connection and send pin for emulated card service
        IdemixService is = new IdemixService(cs);
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();

        is.sendPin(PIN.getBytes());
        ic.issue(cd, isk, attributes, null); // null indicates default expiry


        final Path path = Paths.get(System.getProperty("user.dir"), "card.json");
        IRMACardHelper.storeState(card, path);


    }

    public void IssueSurfnetRoot(CardService cs, IRMACard card) throws CredentialsException, InfoException, CardServiceException {

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

        is.sendPin(PIN.getBytes());
        ic.issue(cd, isk, attributes, null); // null indicates default expiry


        // Setup a connection to a real card
//            CardService real = new TerminalCardService();   <--- doesn't exist?

    }

    public void IssueThalia(CardService cs, IRMACard card, JsonObject result) throws CredentialsException, InfoException, CardServiceException {

        CredentialDescription cd = DescriptionStore.getInstance().
                getCredentialDescriptionByName("Thalia", "thalia");
        System.out.println(cd.getIssuerDescription().getName());
        IdemixSecretKey isk = IdemixKeyStore.getInstance().getSecretKey(cd);

        String userID = result.get("username").getAsString();
        String bday = result.get("birthday").getAsString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime birthday = LocalDateTime.parse(bday, formatter);
        birthday = birthday.plusYears(18);
        String over18 = "no";
        if (birthday.isBefore(LocalDateTime.now())) {
            over18 = "yes";
        }
        String memberType = result.get("membership_type").getAsString();

        // Setup the attributes that will be issued to the card
        Attributes attributes = new Attributes();
        attributes.add("userID", userID.getBytes());
        attributes.add("over18", over18.getBytes());
        attributes.add("memberType", memberType.getBytes());

        // Setup a connection and send pin for emulated card service
        IdemixService is = new IdemixService(cs);
        IdemixCredentials ic = new IdemixCredentials(is);
        ic.connect();

        is.sendPin(PIN.getBytes());
        ic.issue(cd, isk, attributes, null); // null indicates default expiry


        // Setup a connection to a real card
//            CardService real = new TerminalCardService();   <--- doesn't exist?

    }


    public JsonObject verifySurfnetRoot(CardService cs) throws CredentialsException, InfoException, IOException {

        IdemixVerificationDescription vd = new IdemixVerificationDescription(
                "Surfnet", "rootAll");
        Attributes attr = new IdemixCredentials(cs).verify(vd);
        if (attr == null)
        {
            throw new CredentialsException();
        }
        String SurfnetRoot = new String(attr.get("userID"));
        String mode = "student_number";
        String value = SurfnetRoot.substring(0,8);

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("https://thalia.nu/api/irma_api.php?apikey=%s&%s=%s",apikey,mode,value);
        HttpResponse response = transport.createRequestFactory().buildGetRequest(new GenericUrl(sb.toString())).execute();
        JsonParser jp = new JsonParser();

        JsonObject jo = jp.parse(response.parseAsString()).getAsJsonObject();
        if(jo.get("status").getAsString().equals("ok"))
        {
            return jo;
        }


        return null;
    }

    public JsonObject verifyThaliaRoot(CardService cs) throws CredentialsException, InfoException, IOException {

        IdemixVerificationDescription vd = new IdemixVerificationDescription(
                "Thalia", "rootAll");
        System.out.println("STEP 1");
        Attributes attr = null;
        try {
            attr = new IdemixCredentials(cs).verify(vd);
        } catch (CredentialsException e) {
           return null;
        }

        if (attr == null) {
            return null;
        }
        String ThaliaUser = new String(attr.get("userID"));
        String mode = "thalia_username";

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("https://thalia.nu/api/irma_api.php?apikey=%s&%s=%s",apikey,mode,ThaliaUser);
        HttpResponse response = transport.createRequestFactory().buildGetRequest(new GenericUrl(sb.toString())).execute();
        JsonParser jp = new JsonParser();

        JsonObject jo = jp.parse(response.parseAsString()).getAsJsonObject();
        if(jo.get("status").getAsString().equals("ok")) {
            return jo;
        }


        return null;
    }
}
