package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Eu4ImageLoader {

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
    }

    private static Map<String, Image> IMAGES = new HashMap<>();

    public static Image loadImage(Path p) throws IOException {
        if (IMAGES.containsKey(p.toString())) {
            return IMAGES.get(p.toString());
        }

        File file = p.toFile();
        BufferedImage image = ImageIO.read(file);
        WritableImage w = new WritableImage(image.getWidth(), image.getHeight());
        w = SwingFXUtils.toFXImage(image, w);
        IMAGES.put(p.toString(), w);
        return w;
    }

    public static ImageView loadDisasterImage(String name) throws IOException {
        Path p = Eu4Installation.getPath().resolve("gfx/interface/disasters/" + name + ".dds");
        Image i = loadImage(p);
        ImageView v = new ImageView(i);
        v.setViewport(new Rectangle2D(44, 0, 44, 44));
        return v;
    }

    public static ImageView loadFlagImage(String name) throws IOException {
        Path p = Eu4Installation.getPath().resolve("gfx/flags/" + name + ".tga");
        Image i = loadImage(p);
        ImageView v = new ImageView(i);
        //v.setViewport(new Rectangle2D(0, 0, 44, 44));
        v.setFitWidth(35);
        v.setFitHeight(35);
        return v;
    }
}
