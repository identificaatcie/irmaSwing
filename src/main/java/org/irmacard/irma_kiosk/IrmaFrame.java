package org.irmacard.irma_kiosk;

import javax.swing.*;
import java.awt.*;

/**
 * Created by wietse on 16/11/15.
 */
public class IrmaFrame extends JFrame {

    public static Font irmaFont = new Font("Serif",Font.PLAIN,25);
    public static Font irmaFontLarge = new Font("Serif",Font.PLAIN,50);

    public IrmaFrame()
    {
        this.setSize(new Dimension(1280,800));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
