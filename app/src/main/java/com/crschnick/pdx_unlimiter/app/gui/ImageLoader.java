package com.crschnick.pdx_unlimiter.app.gui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public class ImageLoader {

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
    }

    public static Optional<Image> loadImageOptional(Path p) {
        try {
            return Optional.of(loadImage(p));
        } catch (Exception e) {
            LoggerFactory.getLogger(ImageLoader.class).warn("Image file " + p.toString() + " not found.");
            return Optional.empty();
        }
    }

    public static Image loadImage(Path p) throws IOException {
        return loadImage(p, null);
    }

    public static Optional<Image> loadImageOptional(InputStream in, Function<Integer, Integer> pixelSelector) {
        try {
            return Optional.of(loadImage(in, pixelSelector));
        } catch (Exception e) {
            LoggerFactory.getLogger(ImageLoader.class).warn("", e);
            return Optional.empty();
        }
    }

    static Image loadImage(Path p, Function<Integer, Integer> pixelSelector) throws IOException {
        if (!Files.isRegularFile(p)) {
            throw new IOException("Image file " + p.toString() + " not found.");
        }

        return loadImage(Files.newInputStream(p), pixelSelector);
    }

    static Image loadImage(InputStream in, Function<Integer, Integer> pixelSelector) throws IOException {
        BufferedImage image = ImageIO.read(in);
        in.close();

        WritableImage img = new WritableImage(image.getWidth(), image.getHeight());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                img.getPixelWriter().setArgb(x, y, pixelSelector != null ? pixelSelector.apply(rgb) : rgb);
            }
        }


        return img;
    }
}
