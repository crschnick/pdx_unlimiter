package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class ImageLoader {

    private static final Image DEFAULT_IMAGE = new WritableImage(1, 1);

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new com.realityinteractive.imageio.tga.TGAImageReaderSpi());
    }

    public static Image loadImage(Path p) {
        return loadImage(p, null);
    }

    static Image loadImage(Path p, Function<Integer, Integer> pixelSelector) {
        if (p == null) {
            return DEFAULT_IMAGE;
        }

        if (!Files.isRegularFile(p)) {
            LoggerFactory.getLogger(ImageLoader.class).error("Image file " + p.toString() + " not found.");
            return DEFAULT_IMAGE;
        }

        BufferedImage image;
        try {
            image = ImageIO.read(p.toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return DEFAULT_IMAGE;
        }

        WritableImage img = new WritableImage(image.getWidth(), image.getHeight());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                img.getPixelWriter().setArgb(x, y, pixelSelector != null ? pixelSelector.apply(rgb) : rgb);
            }
        }
        return img;
    }

    static BufferedImage loadAwtImage(Path input, Function<Integer, Integer> pixelSelector) {
        try {
            BufferedImage image = ImageIO.read(input.toFile());
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int rgb = image.getRGB(x, y);
                    image.setRGB(x, y, pixelSelector != null ? pixelSelector.apply(rgb) : rgb);
                }
            }
            return image;
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return null;
        }
    }

    static Image toFXImage(BufferedImage awtImage) {
        WritableImage img = new WritableImage(awtImage.getWidth(), awtImage.getHeight());
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int rgb = awtImage.getRGB(x, y);
                img.getPixelWriter().setArgb(x, y, rgb);
            }
        }
        return img;
    }

    public static void writePng(Image image, Path out) throws IOException {
        BufferedImage swingImage = new BufferedImage(
                (int) image.getWidth(),
                (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                swingImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }

        ImageIO.write(swingImage, "png", out.toFile());
    }
}
