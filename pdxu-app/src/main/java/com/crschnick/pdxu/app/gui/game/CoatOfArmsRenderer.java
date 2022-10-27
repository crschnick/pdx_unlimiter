package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.TaggedNode;
import com.crschnick.pdxu.io.node.ValueNode;
import com.crschnick.pdxu.model.CoatOfArms;
import com.crschnick.pdxu.model.GameColor;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
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

    protected abstract Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext context);

    public void brighten(BufferedImage awtImage) {
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

    private void applyCullingMask(BufferedImage emblemImage, BufferedImage patternImage, List<Integer> indices) {
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

    public void applyMask(BufferedImage awtImage, Image mask) {
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

    public BufferedImage pattern(Graphics g, CoatOfArms.Sub sub, GameFileContext ctx, int width, int height) {
        var colors = getPredefinedColors(ctx);
        if (sub.getPatternFile() != null) {
            int pColor1 = sub.getColors()[0] != null
                    ? ColorHelper.intFromColor(
                    colors.getOrDefault(sub.getColors()[0], javafx.scene.paint.Color.TRANSPARENT))
                    : 0;
            int pColor2 = sub.getColors()[1] != null
                    ? ColorHelper.intFromColor(
                    colors.getOrDefault(sub.getColors()[1], javafx.scene.paint.Color.TRANSPARENT))
                    : 0;
            int pColor3 = sub.getColors()[2] != null
                    ? ColorHelper.intFromColor(
                    colors.getOrDefault(sub.getColors()[2], javafx.scene.paint.Color.TRANSPARENT))
                    : 0;
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                int alpha = rgb & 0xFF000000;
                int color = rgb & 0x00FFFFFF;
                int colorIndex = pickClosestColor(color, PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                int usedColor = new int[]{
                        pColor1,
                        pColor2,
                        pColor3
                }[colorIndex] & 0x00FFFFFF;
                return alpha + usedColor;
            };
            var patternFile = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms", "patterns").resolve(sub.getPatternFile()), ctx);
            patternFile.map(p -> ImageHelper.loadAwtImage(p, patternFunction)).ifPresent(img -> {
                g.drawImage(
                        img,
                        (int) (sub.getX() * width),
                        (int) (sub.getY() * height),
                        (int) (sub.getScaleX() * width),
                        (int) (sub.getScaleY() * height),
                        null
                );
            });
            return patternFile.map(p -> ImageHelper.loadAwtImage(p, null)).orElse(null);
        } else {
            return null;
        }
    }

    public javafx.scene.paint.Color applyMaskPixel(javafx.scene.paint.Color colour, double maskValue) {
        return new javafx.scene.paint.Color(
                colour.getRed() * maskValue,
                colour.getGreen() * maskValue,
                colour.getBlue() * maskValue,
                maskValue
        );
    }

    public javafx.scene.paint.Color overlayColors(javafx.scene.paint.Color bottom, javafx.scene.paint.Color top) {
        double alpha = top.getOpacity() + bottom.getOpacity() * (1 - top.getOpacity());
        return new javafx.scene.paint.Color(
                (top.getRed() * top.getOpacity() + bottom.getRed() * bottom.getOpacity() * (1 - top.getOpacity())) / alpha,
                (top.getGreen() * top.getOpacity() + bottom.getGreen() * bottom.getOpacity() * (1 - top.getOpacity())) / alpha,
                (top.getBlue() * top.getOpacity() + bottom.getBlue() * bottom.getOpacity() * (1 - top.getOpacity())) / alpha,
                alpha
        );
    }

    private javafx.scene.paint.Color evaluateColor(String color, GameFileContext ctx) {
        var tagged = Arrays.stream(TaggedNode.COLORS)
                .filter(tagType -> color != null && color.startsWith(tagType.getId()))
                .findAny()
                .map(tagType -> new TaggedNode(tagType, Arrays.stream(color.substring(tagType.getId().length() + 2, color.length() - 1).split(" "))
                        .filter(s -> s.length() > 0)
                        .map(s -> new ValueNode(s, false)).toList()));
        if (tagged.isPresent()) {
            return ColorHelper.fromGameColor(GameColor.fromColorNode(tagged.get()));
        }

        var colors = getPredefinedColors(ctx);
        return color != null
                ? colors.getOrDefault(color, javafx.scene.paint.Color.TRANSPARENT)
                : javafx.scene.paint.Color.TRANSPARENT;
    }

    public void emblem(
            BufferedImage currentImage,
            BufferedImage rawPatternImage,
            CoatOfArms.Sub sub,
            CoatOfArms.Emblem emblem,
            GameFileContext ctx,
            int width, int height
    ) {
        Function<Integer, Integer> customFilter;
        boolean hasColor = emblem.getColors() != null;
        if (emblem.getColors() != null) {
            javafx.scene.paint.Color eColor1 = evaluateColor(emblem.getColors()[0], ctx);
            javafx.scene.paint.Color eColor2 = evaluateColor(emblem.getColors()[1], ctx);
            javafx.scene.paint.Color eColor3 = evaluateColor(emblem.getColors()[2], ctx);

            customFilter = (Integer rgb) -> {
                javafx.scene.paint.Color newColor = this.applyMaskPixel(eColor1, 1);
                newColor = this.overlayColors(newColor, this.applyMaskPixel(eColor2, getGreen(rgb) / 255.0));
                newColor = this.overlayColors(newColor, this.applyMaskPixel(eColor3, getRed(rgb) / 255.0));
                int darkShadingA = 255 - Math.min(getBlue(rgb) * 2, 255);
                newColor = this.overlayColors(newColor, javafx.scene.paint.Color.color(0, 0, 0, darkShadingA / 255.0));
                int lightShadingA = Math.round(Math.max(Math.min((getBlue(rgb) - 127) * 2, 255), 0));
                newColor = this.overlayColors(newColor, javafx.scene.paint.Color.color(1, 1, 1, lightShadingA / 255.0));
                int usedColor = intFromColor(newColor) & 0x00FFFFFF;

                int alpha = rgb & 0xFF000000;
                return alpha + usedColor;
            };
        } else {
            customFilter = (Integer rgb) -> {
                return rgb;
            };
        }

        var path = CascadeDirectoryHelper.openFile(
                Path.of("gfx", "coat_of_arms", (hasColor ? "colored" : "textured") + "_emblems")
                        .resolve(emblem.getFile()),
                ctx
        );
        path.map(p -> ImageHelper.loadAwtImage(p, customFilter)).ifPresent(img -> {
            boolean hasMask = emblem.getMask().stream().anyMatch(i -> i != 0);
            BufferedImage emblemToCullImage = null;
            if (hasMask) {
                emblemToCullImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D usedGraphics =
                    hasMask ? (Graphics2D) emblemToCullImage.getGraphics() : (Graphics2D) currentImage.getGraphics();

            emblem.getInstances().stream()
                    .sorted(Comparator.comparingDouble(CoatOfArms.Instance::getDepth))
                    .forEach(instance -> {
                        double angle = Math.toRadians(instance.getRotation());
                        double sin = Math.sin(angle);
                        double cos = Math.cos(angle);

                        var rotWidth = Math.abs(img.getWidth() * cos + img.getHeight() * sin);
                        var rotHeight = Math.abs(img.getWidth() * sin + img.getHeight() * cos);

                        var scaleX = ((double) width / rotWidth) * instance.getScaleX() * sub.getScaleX();
                        var scaleY = ((double) height / rotHeight) * instance.getScaleY() * sub.getScaleY();

                        var x = width * (sub.getX() + (sub.getScaleX() * instance.getX()));
                        var y = height * (sub.getY() + (sub.getScaleY() * instance.getY()));

                        AffineTransform trans = new AffineTransform();
                        trans.translate(x, y);
                        trans.scale(scaleX, scaleY);
                        trans.translate(-rotWidth / 2.0, -rotHeight / 2.0);

                        if (instance.getRotation() != 0) {
                            trans.translate(rotWidth / 2.0, rotHeight / 2.0);
                            trans.rotate(angle);
                            trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
                        }

                        usedGraphics.drawImage(img, new AffineTransformOp(trans, AffineTransformOp.TYPE_BICUBIC), 0, 0);
                    });

            if (hasMask) {
                applyCullingMask(emblemToCullImage, rawPatternImage, emblem.getMask());
                currentImage.getGraphics().drawImage(emblemToCullImage, 0, 0, new Color(0, 0, 0, 0), null);
            }
        });
    }

    void renderImage(Graphics g, java.awt.Image img, double x, double y, double w, double h) {
        g.drawImage(
                img,
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(w),
                (int) Math.round(h),
                new Color(0, 0, 0, 0),
                null
        );
    }
}
