package com.crschnick.pdx_unlimiter.updater;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UpdaterGui extends JFrame {

    private Image image;

    private float progress;

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

    public void setProgress(float progress) {
        this.progress = progress;
        repaint();
    }

    private void renderSplashFrame(Graphics g) {
        g.drawImage(image, 0, 0, this);
        g.setColor(Color.WHITE);
        g.fillRect(50, 150, (int) (520 * progress), 40);
    }

    public void paint(Graphics g) {
        renderSplashFrame(g);
    }
}
