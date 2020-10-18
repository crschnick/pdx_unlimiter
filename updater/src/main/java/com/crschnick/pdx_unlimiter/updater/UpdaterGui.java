package com.crschnick.pdx_unlimiter.updater;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UpdaterGui extends JFrame {

    private Image image;

    public UpdaterGui() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        try {
            Image icon = ImageIO.read(Updater.class.getResource("icon.png"));
            setIconImage(icon);
            image = ImageIO.read(Updater.class.getResource("splash.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSize(image.getWidth(this), image.getHeight(this));
        setLocationRelativeTo(null);
    }

    private void renderSplashFrame(Graphics g, int frame) {
        final String[] comps = {"foo", "bar", "baz"};
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.BLACK);
        g.drawString("Loading "+comps[(frame/5)%3]+"...", 120, 150);
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
        renderSplashFrame(g, 10);
    }
}
