package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class ImageLoader {

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
    }

    public static Image loadImage(Path p) {
        return loadImage(p, null);
    }

    static Image loadImage(Path p, Predicate<Integer> pixelSelector) {
        if (!Files.isRegularFile(p)) {
            return null;
        }

        try {
            return loadImage(Files.newInputStream(p), pixelSelector);
        } catch (IOException e) {
            ErrorHandler.handleException(e, "Can't read image " + p.toString());
            return null;
        }
    }

    static Image loadImage(InputStream in, Predicate<Integer> pixelSelector) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(in);
            in.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
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
        return w;
    }
}
