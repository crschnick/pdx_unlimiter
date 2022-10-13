package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.CoatOfArms;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.crschnick.pdxu.app.util.ColorHelper.*;

public abstract class CoatOfArmsRenderer {

    public static class Ck3Renderer extends CoatOfArmsRenderer {

        @Override
        protected Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext context) {
            return Ck3CoatOfArmsCache.getPredefinedColors(context);
        }
    }

    public static final Ck3Renderer CK3 = new Ck3Renderer();

    public static class Vic3Renderer extends CoatOfArmsRenderer {

        @Override
        protected Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext context) {
            return Vic3CoatOfArmsCache.getPredefinedColors(context);
        }
    }

    public static final Vic3Renderer VIC3 = new Vic3Renderer();


    static final int REF_IMG_SIZE = 256;
    private static final int PATTERN_COLOR_1 = 0x00FF0000;
    private static final int PATTERN_COLOR_2 = 0x00FFFF00;
    private static final int PATTERN_COLOR_3 = 0x00FFFFFF;
    private static final int EMBLEM_COLOR_1 = 0x000080;
    private static final int EMBLEM_COLOR_2 = 0x00FF00;
    private static final int EMBLEM_COLOR_3 = 0xFF0080;

    protected abstract Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext context);

    public  void brighten(BufferedImage awtImage) {
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int color = (getAlpha(argb) << 24)
                        + (Math.min((int) (1.8 * getRed(argb)), 255) << 16)
                        + (Math.min((int) (1.8 * getGreen(argb)), 255) << 8)
                        + (Math.min((int) (1.8 * getBlue(argb)), 255));
                awtImage.setRGB(x, y, color);
            }
        }
    }

    private  void applyCullingMask(BufferedImage emblemImage, BufferedImage patternImage, List<Integer> indices) {
        double xF = (double) patternImage.getWidth() / emblemImage.getWidth();
        double yF = (double) patternImage.getHeight() / emblemImage.getHeight();
        for (int x = 0; x < emblemImage.getWidth(); x++) {
            for (int y = 0; y < emblemImage.getHeight(); y++) {
                int maskArgb = patternImage.getRGB((int) Math.floor(xF * x), (int) Math.floor(yF * y));
                int maskRgb = 0x00FFFFFF & maskArgb;
                int maskIndex =
                        1 + ColorHelper.pickClosestColor(maskRgb, PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                if (!indices.contains(maskIndex)) {
                    emblemImage.setRGB(x, y, 0);
                }
            }
        }
    }

    public  void applyMask(BufferedImage awtImage, Image mask) {
        double xF = mask.getWidth() / awtImage.getWidth();
        double yF = mask.getHeight() / awtImage.getHeight();
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int maskArgb = mask.getPixelReader().getArgb((int) Math.floor(xF * x), (int) Math.floor(yF * y));

                int color = (((int) ((getAlpha(maskArgb) / 255.0) * getAlpha(argb))) << 24)
                        + (((int) ((getRed(maskArgb) / 255.0) * getRed(argb))) << 16)
                        + (((int) ((getGreen(maskArgb) / 255.0) * getGreen(argb))) << 8)
                        + (((int) ((getBlue(maskArgb) / 255.0) * getBlue(argb))));
                awtImage.setRGB(x, y, color);
            }
        }
    }

    public  BufferedImage pattern(Graphics g, CoatOfArms.Sub sub, GameFileContext ctx, int size) {
        ensureImagesLoaded();
        var colors = getPredefinedColors(ctx);
        if (sub.getPatternFile() != null) {
            int pColor1 = sub.getColors().size() > 0
                    ? ColorHelper.intFromColor(
                            colors.getOrDefault(sub.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT))
                    : 0;
            int pColor2 = sub.getColors().size() > 1
                    ? ColorHelper.intFromColor(
                            colors.getOrDefault(sub.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT))
                    : 0;
            int pColor3 = sub.getColors().size() > 2
                    ? ColorHelper.intFromColor(
                            colors.getOrDefault(sub.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT))
                    : 0;
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                int alpha = rgb & 0xFF000000;
                int color = rgb & 0x00FFFFFF;
                int colorIndex = pickClosestColor(color, PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                int usedColor = new int[] {pColor1, pColor2, pColor3}[colorIndex] & 0x00FFFFFF;
                return alpha + usedColor;
            };
            var patternFile = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms", "patterns").resolve(sub.getPatternFile()), ctx);
            patternFile.map(p -> ImageHelper.loadAwtImage(p, patternFunction)).ifPresent(img -> {
                g.drawImage(
                        img,
                        (int) (sub.getX() * size),
                        (int) (sub.getY() * size),
                        (int) (sub.getScaleX() * size),
                        (int) (sub.getScaleY() * size),
                        null);
            });
            return patternFile.map(p -> ImageHelper.loadAwtImage(p, null)).orElse(null);
        } else {
            return null;
        }
    }

    public  void emblem(
            BufferedImage currentImage,
            BufferedImage rawPatternImage,
            CoatOfArms.Sub sub,
            CoatOfArms.Emblem emblem,
            GameFileContext ctx,
            int size
    ) {
        ensureImagesLoaded();
        var colors = getPredefinedColors(ctx);
        int eColor1 = emblem.getColors().size() > 0
                ? ColorHelper.intFromColor(
                        colors.getOrDefault(emblem.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT))
                : 0;
        int eColor2 = emblem.getColors().size() > 1
                ? ColorHelper.intFromColor(
                        colors.getOrDefault(emblem.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT))
                : 0;
        int eColor3 = emblem.getColors().size() > 2
                ? ColorHelper.intFromColor(
                        colors.getOrDefault(emblem.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT))
                : 0;

        boolean hasColor = emblem.getColors().size() > 0;
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            if (!hasColor) {
                return rgb;
            }

            int alpha = rgb & 0xFF000000;
            int color = rgb & 0x00FFFFFF;
            int colorIndex = pickClosestColor(color, EMBLEM_COLOR_1, EMBLEM_COLOR_2, EMBLEM_COLOR_3);
            int usedColor = new int[] {eColor1, eColor2, eColor3}[colorIndex] & 0x00FFFFFF;
            return alpha + usedColor;
        };

        var path = CascadeDirectoryHelper.openFile(
                Path.of("gfx", "coat_of_arms", (hasColor ? "colored" : "textured") + "_emblems")
                        .resolve(emblem.getFile()),
                ctx);
        path.map(p -> ImageHelper.loadAwtImage(p, customFilter)).ifPresent(img -> {
            boolean hasMask = emblem.getMask().stream().anyMatch(i -> i != 0);
            BufferedImage emblemToCullImage = null;
            if (hasMask) {
                emblemToCullImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D usedGraphics =
                    hasMask ? (Graphics2D) emblemToCullImage.getGraphics() : (Graphics2D) currentImage.getGraphics();

            emblem.getInstances().stream()
                    .sorted(Comparator.comparingDouble(CoatOfArms.Instance::getDepth))
                    .forEach(instance -> {
                        var scaleX = ((double) size / img.getWidth()) * instance.getScaleX() * sub.getScaleX();
                        var scaleY = ((double) size / img.getHeight()) * instance.getScaleY() * sub.getScaleY();

                        var x = size * (sub.getX() + (sub.getScaleX() * instance.getX()));
                        var y = size * (sub.getY() + (sub.getScaleY() * instance.getY()));

                        AffineTransform trans = new AffineTransform();

                        trans.translate(x, y);
                        trans.scale(scaleX, scaleY);
                        trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);

                        if (instance.getRotation() != 0) {
                            trans.translate(img.getWidth() / 2.0, img.getHeight() / 2.0);
                            trans.rotate(
                                    Math.signum(scaleX) * Math.signum(scaleY) * Math.toRadians(instance.getRotation()));
                            trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
                        }

                        usedGraphics.drawImage(img, trans, null);
                    });

            if (hasMask) {
                applyCullingMask(emblemToCullImage, rawPatternImage, emblem.getMask());
                currentImage.getGraphics().drawImage(emblemToCullImage, 0, 0, new Color(0, 0, 0, 0), null);
            }
        });
    }

    private  void ensureImagesLoaded() {
        // Ugly hack to ensure that all needed images are loaded!
        if (GameImage.CK3_HOUSE_FRAME == ImageHelper.DEFAULT_IMAGE) {
            GameImage.loadCk3Images();
        }
    }

    void renderImage(Graphics g, java.awt.Image img, double x, double y, double w, double h) {
        g.drawImage(
                img,
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(w),
                (int) Math.round(h),
                new Color(0, 0, 0, 0),
                null);
    }
}
