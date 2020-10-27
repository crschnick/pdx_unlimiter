package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
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
import java.util.function.Predicate;

public class Eu4ImageLoader {

    private static Map<String, Image> IMAGES = new HashMap<>();

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
    }

    public static Image loadImage(Path p) {
        return loadImage(p, null);
    }

    public static Image loadImage(Path p, Predicate<Integer> pixelSelector) {
        if (IMAGES.containsKey(p.toString())) {
            return IMAGES.get(p.toString());
        }

        File file = p.toFile();
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            ErrorHandler.handleException(new IOException("Can't read image " + p.toString(), e));
        }

        if (pixelSelector != null) {
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int rgb = image.getRGB(x, y);
                    if (!pixelSelector.test(rgb)) {
                        image.setRGB(x, y, 0x01FFFFFF);
                    }
                }
            }
        }

        WritableImage w = new WritableImage(image.getWidth(), image.getHeight());
        w = SwingFXUtils.toFXImage(image, w);
        IMAGES.put(p.toString(), w);
        return w;
    }

    public static ImageView loadInterfaceImage(String name) {
        return loadInterfaceImage(name, null);
    }

    public static ImageView loadInterfaceImage(String name, Rectangle2D viewport) {
        Path p = GameInstallation.EU4.getPath().resolve("gfx/interface/" + name);
        Image i = loadImage(p);
        ImageView v = new ImageView(i);
        if (viewport != null) v.setViewport(viewport);
        return v;
    }

    public static ImageView loadDisasterImage(String name) {
        Path p = GameInstallation.EU4.getPath().resolve("gfx/interface/disasters/" + name + ".dds");
        Image i = loadImage(p);
        ImageView v = new ImageView(i);
        v.setViewport(new Rectangle2D(44, 0, 44, 44));
        return v;
    }

    public static ImageView loadFlagImage(String name, int size) {
        Path p = GameInstallation.EU4.getPath().resolve("gfx/flags/" + name + ".tga");
        Image i = loadImage(p);
        ImageView v = new ImageView(i);
        //v.setViewport(new Rectangle2D(0, 0, 44, 44));
        v.setFitWidth(size);
        v.setFitHeight(size);
        return v;
    }
}
