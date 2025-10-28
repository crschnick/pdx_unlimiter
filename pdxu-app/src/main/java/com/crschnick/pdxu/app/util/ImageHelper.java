package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.*;

import com.realityinteractive.imageio.tga.TGAImageReaderSpi;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import static com.crschnick.pdxu.app.util.ColorHelper.*;
import static java.awt.color.ColorSpace.CS_LINEAR_RGB;
import static java.awt.color.ColorSpace.CS_sRGB;

public class ImageHelper {

    public static final Image DEFAULT_IMAGE = new WritableImage(1, 1);
    public static final BufferedImage DEFAULT_AWT_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private static Field srgbField = null;

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new TGAImageReaderSpi());
        try {
            srgbField = ColorModel.class.getDeclaredField("is_sRGB");
            srgbField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            ErrorEventFactory.fromThrowable(e).term().handle();
        }
    }

    public static Image loadImage(Path p) {
        return loadImage(p, null);
    }

    public static Image cut(Image img, Rectangle2D r) {
        // Don't try to cut if image has not been found
        if (img == DEFAULT_IMAGE) {
            return DEFAULT_IMAGE;
        }

        PixelReader reader = img.getPixelReader();
        try {
            return new WritableImage(
                    reader, (int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
        } catch (ArrayIndexOutOfBoundsException e) {
            ErrorEventFactory.fromThrowable(e).expected().omit().handle();
            return DEFAULT_IMAGE;
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
            return DEFAULT_IMAGE;
        }
    }

    public static Image loadImage(Path p, Function<Integer, Integer> pixelSelector) {
        if (p == null) {
            return DEFAULT_IMAGE;
        }

        if (!Files.isRegularFile(p)) {
            TrackEvent.error("Image file " + p.toString() + " not found.");
            return DEFAULT_IMAGE;
        }

        if (!Files.isReadable(p)) {
            TrackEvent.error("Image file " + p.toString() + " not readable.");
            return DEFAULT_IMAGE;
        }

        BufferedImage image;
        try {
            image = ImageIO.read(p.toFile());
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable("Image file " + p.toString() + " not readable.", e)
                    .omit()
                    .handle();
            return DEFAULT_IMAGE;
        }

        if (image == null) {
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
        pw.setPixels(
                0,
                0,
                image.getWidth(),
                image.getHeight(),
                PixelFormat.getIntArgbInstance(),
                iArray,
                0,
                image.getWidth());
        return img;
    }

    public static BufferedImage loadAwtImage(Path input, Function<Integer, Integer> pixelSelector) {
        try {
            BufferedImage image = ImageIO.read(input.toFile());

            // Fix eu5 images not being declared srgb even though they should
            if (!(boolean) srgbField.get(image.getColorModel())) {
                image = new BufferedImage(ColorModel.getRGBdefault(), image.getRaster(), false, new Hashtable<>());;
            }

            if (pixelSelector != null) {
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        int rgb = image.getRGB(x, y);
                        image.setRGB(x, y, pixelSelector.apply(rgb));
                    }
                }
            }
            return image;
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable("Image file " + input.toString() + " not readable.", e)
                    .omit()
                    .expected()
                    .handle();
            return DEFAULT_AWT_IMAGE;
        }
    }

    public static WritableImage toFXImage(BufferedImage image) {
        WritableImage img = new WritableImage(image.getWidth(), image.getHeight());
        var iArray = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        PixelWriter pw = img.getPixelWriter();
        pw.setPixels(
                0,
                0,
                image.getWidth(),
                image.getHeight(),
                PixelFormat.getIntArgbInstance(),
                iArray,
                0,
                image.getWidth());
        return img;
    }

    public static BufferedImage fromFXImage(Image fxImage) {
        BufferedImage img =
                new BufferedImage((int) fxImage.getWidth(), (int) fxImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < fxImage.getWidth(); x++) {
            for (int y = 0; y < fxImage.getHeight(); y++) {
                int rgb = fxImage.getPixelReader().getArgb(x, y);
                img.setRGB(x, y, rgb);
            }
        }
        return img;
    }

    public static void writePng(Image image, Path out) throws IOException {
        if (Files.isDirectory(out)) {
            return;
        }

        if (Files.exists(out) && !Files.isWritable(out)) {
            if (!out.toFile().setWritable(true)) {
                return;
            }
        }

        BufferedImage swingImage =
                new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                swingImage.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }

        ImageIO.write(swingImage, "png", out.toFile());
    }

    public static void applyAlphaMask(BufferedImage awtImage, Image mask) {
        double xF = mask.getWidth() / awtImage.getWidth();
        double yF = mask.getHeight() / awtImage.getHeight();
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int maskArgb = mask.getPixelReader().getArgb((int) Math.floor(xF * x), (int) Math.floor(yF * y));

                int color = ((getAlpha(maskArgb)) << 24) + (getRed(argb) << 16) + (getGreen(argb) << 8) + getBlue(argb);
                awtImage.setRGB(x, y, color);
            }
        }
    }
}
