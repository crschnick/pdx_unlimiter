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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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
        return w;
    }

    public static Node createImageNode(Path p, String styleClass) {
        return createImageNode(p, styleClass, null, null);
    }

    public static Node createImageNode(Path p, String styleClass, Rectangle2D viewport) {
        return createImageNode(p, styleClass, viewport, null);
    }

    public static Node createImageNode(Path p, String styleClass, Rectangle2D viewport, Predicate<Integer> pixelSelector) {
        Image w = loadImage(p, pixelSelector);
        ImageView v = new ImageView(w);
        Pane pane = new Pane(v);
        if (viewport != null) v.setViewport(viewport);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        pane.getStyleClass().add(styleClass);
        return pane;
    }
}
