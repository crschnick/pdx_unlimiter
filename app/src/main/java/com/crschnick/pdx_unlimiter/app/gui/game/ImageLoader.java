package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.realityinteractive.imageio.tga.TGAImageReaderSpi;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.*;
import net.nikr.dds.DDSImageReaderSpi;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class ImageLoader {

    public static final Image DEFAULT_IMAGE = new WritableImage(1, 1);

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new DDSImageReaderSpi());
        registry.registerServiceProvider(new TGAImageReaderSpi());
    }

    public static Image loadImage(Path p) {
        return loadImage(p, null);
    }

    public static Image cut(Image img, Rectangle2D r) {
        PixelReader reader = img.getPixelReader();
        return new WritableImage(reader, (int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
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
        var iArray = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        if (pixelSelector != null) {
            for (int i = 0; i < iArray.length; i++) {
                iArray[i] = pixelSelector.apply(iArray[i]);
            }
        }
        PixelWriter pw = img.getPixelWriter();
        pw.setPixels(0, 0, image.getWidth(), image.getHeight(), PixelFormat.getIntArgbInstance(), iArray, 0, image.getWidth());
        return img;
    }

    static BufferedImage loadAwtImage(Path input, Function<Integer, Integer> pixelSelector) {
        try {
            BufferedImage image = ImageIO.read(input.toFile());
            if (pixelSelector != null) {
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        int rgb = image.getRGB(x, y);
                        image.setRGB(x, y, pixelSelector.apply(rgb));
                    }
                }
            }
            return image;
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return null;
        }
    }

    public static WritableImage toFXImage(BufferedImage image) {
        WritableImage img = new WritableImage(image.getWidth(), image.getHeight());
        var iArray = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        PixelWriter pw = img.getPixelWriter();
        pw.setPixels(0, 0, image.getWidth(), image.getHeight(), PixelFormat.getIntArgbInstance(), iArray, 0, image.getWidth());
        return img;
    }

    static BufferedImage fromFXImage(Image fxImage) {
        BufferedImage img = new BufferedImage(
                (int) fxImage.getWidth(), (int) fxImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < fxImage.getWidth(); x++) {
            for (int y = 0; y < fxImage.getHeight(); y++) {
                int rgb = fxImage.getPixelReader().getArgb(x, y);
                img.setRGB(x, y, rgb);
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
