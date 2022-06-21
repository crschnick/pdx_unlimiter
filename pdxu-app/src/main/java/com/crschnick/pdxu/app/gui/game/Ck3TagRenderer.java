package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.ck3.Ck3CoatOfArms;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.crschnick.pdxu.app.util.ColorHelper.*;

public class Ck3TagRenderer {

    private static final int REF_IMG_SIZE = 256;

    private static final int PATTERN_COLOR_1 = 0x00FF0000;
    private static final int PATTERN_COLOR_2 = 0x00FFFF00;
    private static final int PATTERN_COLOR_3 = 0x00FFFFFF;

    private static final int EMBLEM_COLOR_1 = 0x000080;
    private static final int EMBLEM_COLOR_2 = 0x00FF00;
    private static final int EMBLEM_COLOR_3 = 0xFF0080;

    private static Map<String, javafx.scene.paint.Color> loadPredefinedColors(GameFileContext ctx) {
        var file = CascadeDirectoryHelper.openFile(
                Path.of("common").resolve("named_colors").resolve("default_colors.txt"),
                ctx);
        if (file.isPresent()) {
            try {
                Node node = TextFormatParser.text().parse(file.get());
                Map<String, javafx.scene.paint.Color> map = new HashMap<>();
                node.getNodeForKeyIfExistent("colors").ifPresent(n -> {
                    n.forEach((k, v) -> {
                        map.put(k, fromGameColor(GameColor.fromColorNode(v)));
                    });
                });
                return map;
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }

        return Map.of();
    }

    private static void renderImage(Graphics g, java.awt.Image img, double x, double y, double w, double h) {
        g.drawImage(img,
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(w),
                (int) Math.round(h),
                new java.awt.Color(0, 0, 0, 0),
                null);
    }

    public static BufferedImage renderImage(Ck3CoatOfArms coa, GameFileContext ctx, int size, boolean cloth) {
        if (coa == null) {
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        for (var sub : coa.getSubs()) {
            var rawPatternImg = pattern(g, sub, ctx, size);
            for (var emblem : sub.getEmblems()) {
                emblem(i, rawPatternImg, sub, emblem, ctx, size);
            }
        }
        if (cloth) {
            applyMask(i, GameImage.CK3_COA_OVERLAY);
            brighten(i);
        }

        return i;
    }

    public static Image renderRealmImage(Ck3CoatOfArms coa, String governmentShape, GameFileContext ctx, int size, boolean cloth) {
        var realmImg = renderImage(coa, ctx, size, false);

        var masks = Map.of(
                "clan_government", GameImage.CK3_REALM_CLAN_MASK,
                "republic_government", GameImage.CK3_REALM_REPUBLIC_MASK,
                "theocracy_government", GameImage.CK3_REALM_THEOCRACY_MASK,
                "tribal_government", GameImage.CK3_REALM_TRIBAL_MASK);
        var useMask = masks.getOrDefault(governmentShape, GameImage.CK3_REALM_MASK);
        applyMask(realmImg, useMask);
        brighten(realmImg);

        double scaleFactor = (double) size / REF_IMG_SIZE;
        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        renderImage(g, realmImg, scaleFactor, 4 * scaleFactor,
                realmImg.getWidth() - scaleFactor,
                realmImg.getHeight() - (4 * scaleFactor));


        var frames = Map.of(
                "clan_government", GameImage.CK3_REALM_CLAN_FRAME,
                "republic_government", GameImage.CK3_REALM_REPUBLIC_FRAME,
                "theocracy_government", GameImage.CK3_REALM_THEOCRACY_FRAME,
                "tribal_government", GameImage.CK3_REALM_TRIBAL_FRAME);
        var useFrame = frames.getOrDefault(governmentShape, GameImage.CK3_REALM_FRAME);
        renderImage(g, ImageHelper.fromFXImage(useFrame),
                3 * scaleFactor,
                -8 * scaleFactor,
                realmImg.getWidth() - (6 * scaleFactor),
                realmImg.getHeight() + (20 * scaleFactor));

        return ImageHelper.toFXImage(i);
    }

    public static Image renderHouseImage(Ck3CoatOfArms coa, GameFileContext ctx, int size, boolean cloth) {
        var houseImg = renderImage(coa, ctx, size, cloth);
        applyMask(houseImg, GameImage.CK3_HOUSE_MASK);

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scaleFactor = (double) size / REF_IMG_SIZE;
        renderImage(g, houseImg,
                20 * scaleFactor,
                20 * scaleFactor,
                i.getWidth() - (40 * scaleFactor),
                i.getHeight() - (40 * scaleFactor));

        renderImage(g,
                ImageHelper.fromFXImage(GameImage.CK3_HOUSE_FRAME),
                -25 * scaleFactor,
                -15 * scaleFactor,
                houseImg.getWidth() + (33 * scaleFactor),
                houseImg.getHeight() + (30 * scaleFactor));

        return ImageHelper.toFXImage(i);
    }

    public static Image renderTitleImage(Ck3CoatOfArms coa, GameFileContext ctx, int size, boolean cloth) {
        var titleImg = renderImage(coa, ctx, size, cloth);
        applyMask(titleImg, GameImage.CK3_TITLE_MASK);

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scaleFactor = (double) size / REF_IMG_SIZE;
        renderImage(g, titleImg,
                13 * scaleFactor,
                13 * scaleFactor,
                i.getWidth() - (28 * scaleFactor),
                i.getHeight() - (28 * scaleFactor));

        renderImage(g,
                ImageHelper.fromFXImage(GameImage.CK3_TITLE_FRAME),
                -6 * scaleFactor,
                -4 * scaleFactor,
                titleImg.getWidth() + (11 * scaleFactor),
                titleImg.getHeight() + (11 * scaleFactor));

        return ImageHelper.toFXImage(i);
    }

    private static void brighten(BufferedImage awtImage) {
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int color = (getAlpha(argb) << 24) +
                        (Math.min((int) (1.8 * getRed(argb)), 255) << 16) +
                        (Math.min((int) (1.8 * getGreen(argb)), 255) << 8) +
                        (Math.min((int) (1.8 * getBlue(argb)), 255));
                awtImage.setRGB(x, y, color);
            }
        }
    }

    private static void applyCullingMask(BufferedImage emblemImage, BufferedImage patternImage, List<Integer> indices) {
        double xF = (double) patternImage.getWidth() / emblemImage.getWidth();
        double yF = (double) patternImage.getHeight() / emblemImage.getHeight();
        for (int x = 0; x < emblemImage.getWidth(); x++) {
            for (int y = 0; y < emblemImage.getHeight(); y++) {
                int maskArgb = patternImage.getRGB(
                        (int) Math.floor(xF * x), (int) Math.floor(yF * y));
                int maskRgb = 0x00FFFFFF & maskArgb;
                int maskIndex = 1 + ColorHelper.pickClosestColor(maskRgb,
                        PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                if (!indices.contains(maskIndex)) {
                    emblemImage.setRGB(x, y, 0);
                }
            }
        }
    }

    private static void applyMask(BufferedImage awtImage, Image mask) {
        double xF = mask.getWidth() / awtImage.getWidth();
        double yF = mask.getHeight() / awtImage.getHeight();
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int maskArgb = mask.getPixelReader().getArgb(
                        (int) Math.floor(xF * x), (int) Math.floor(yF * y));

                int color = (((int) ((getAlpha(maskArgb) / 255.0) * getAlpha(argb))) << 24) +
                        (((int) ((getRed(maskArgb) / 255.0) * getRed(argb))) << 16) +
                        (((int) ((getGreen(maskArgb) / 255.0) * getGreen(argb))) << 8) +
                        (((int) ((getBlue(maskArgb) / 255.0) * getBlue(argb))));
                awtImage.setRGB(x, y, color);
            }
        }
    }

    private static BufferedImage pattern(Graphics g, Ck3CoatOfArms.Sub sub, GameFileContext ctx, int size) {
        ensureImagesLoaded();
        var colors = loadPredefinedColors(ctx);
        if (sub.getPatternFile() != null) {
            int pColor1 = sub.getColors().size() > 0 ? ColorHelper.intFromColor(colors
                    .getOrDefault(sub.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            int pColor2 = sub.getColors().size() > 1 ? ColorHelper.intFromColor(colors
                    .getOrDefault(sub.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            int pColor3 = sub.getColors().size() > 2 ? ColorHelper.intFromColor(colors
                    .getOrDefault(sub.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                int alpha = rgb & 0xFF000000;
                int color = rgb & 0x00FFFFFF;
                int colorIndex = pickClosestColor(color, PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                int usedColor = new int[]{pColor1, pColor2, pColor3}[colorIndex] & 0x00FFFFFF;
                return alpha + usedColor;
            };
            var patternFile = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms", "patterns").resolve(sub.getPatternFile()),
                    ctx);
            patternFile.map(p -> ImageHelper.loadAwtImage(p, patternFunction)).ifPresent(img -> {
                g.drawImage(img, (int) (sub.getX() * size), (int) (sub.getY() * size),
                        (int) (sub.getScaleX() * size), (int) (sub.getScaleY() * size), null);
            });
            return patternFile.map(p -> ImageHelper.loadAwtImage(p, null)).orElse(null);
        } else {
            return null;
        }
    }

    private static void emblem(BufferedImage currentImage,
                               BufferedImage rawPatternImage,
                               Ck3CoatOfArms.Sub sub,
                               Ck3CoatOfArms.Emblem emblem,
                               GameFileContext ctx,
                               int size) {
        ensureImagesLoaded();
        var colors = loadPredefinedColors(ctx);
        int eColor1 = emblem.getColors().size() > 0 ? ColorHelper.intFromColor(colors
                .getOrDefault(emblem.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        int eColor2 = emblem.getColors().size() > 1 ? ColorHelper.intFromColor(colors
                .getOrDefault(emblem.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        int eColor3 = emblem.getColors().size() > 2 ? ColorHelper.intFromColor(colors
                .getOrDefault(emblem.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            int alpha = rgb & 0xFF000000;
            int color = rgb & 0x00FFFFFF;
            int colorIndex = pickClosestColor(color, EMBLEM_COLOR_1, EMBLEM_COLOR_2, EMBLEM_COLOR_3);
            int usedColor = new int[]{eColor1, eColor2, eColor3}[colorIndex] & 0x00FFFFFF;
            return alpha + usedColor;
        };

        boolean hasColor = emblem.getColors().size() > 0;
        var path = CascadeDirectoryHelper.openFile(
                Path.of("gfx", "coat_of_arms",
                        (hasColor ? "colored" : "textured") + "_emblems").resolve(emblem.getFile()),
                ctx);
        path.map(p -> ImageHelper.loadAwtImage(p, customFilter)).ifPresent(img -> {

            boolean hasMask = emblem.getMask().stream().anyMatch(i -> i != 0);
            BufferedImage emblemToCullImage = null;
            if (hasMask) {
                emblemToCullImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D usedGraphics = hasMask ? (Graphics2D) emblemToCullImage.getGraphics() :
                    (Graphics2D) currentImage.getGraphics();

            emblem.getInstances().stream().sorted(Comparator.comparingDouble(Ck3CoatOfArms.Instance::getDepth)).forEach(instance -> {
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
                    trans.rotate(Math.signum(scaleX) * Math.signum(scaleY) * Math.toRadians(instance.getRotation()));
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

    private static void ensureImagesLoaded() {
        // Ugly hack to ensure that all needed images are loaded!
        if (GameImage.CK3_HOUSE_FRAME == ImageHelper.DEFAULT_IMAGE) {
            GameImage.loadCk3Images();
        }
    }
}
