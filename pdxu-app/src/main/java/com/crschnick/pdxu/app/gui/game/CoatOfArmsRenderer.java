package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.TaggedNode;
import com.crschnick.pdxu.io.node.ValueNode;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.coa.Emblem;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
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
        protected Map<String, Color> getPredefinedColors(GameFileContext context) {
            return Ck3CoatOfArmsCache.getPredefinedColors(context);
        }

        @Override
        protected Color getMissingReplacementColor() {
            return Color.TRANSPARENT;
        }

        @Override
        protected Path getPatternsDir() {
            return Path.of("gfx", "coat_of_arms", "patterns");
        }

        @Override
        protected Path getEmblemDir(boolean textured) {
            return Path.of("gfx", "coat_of_arms", (!textured ? "colored" : "textured") + "_emblems");
        }
    }

    public static final Ck3Renderer CK3 = new Ck3Renderer();

    public static class Vic3Renderer extends CoatOfArmsRenderer {

        @Override
        protected Map<String, Color> getPredefinedColors(GameFileContext context) {
            return Vic3CoatOfArmsCache.getPredefinedColors(context);
        }

        @Override
        protected Color getMissingReplacementColor() {
            return Color.color(1, 0, 1);
        }

        @Override
        protected Path getPatternsDir() {
            return Path.of("gfx", "coat_of_arms", "patterns");
        }

        @Override
        protected Path getEmblemDir(boolean textured) {
            return Path.of("gfx", "coat_of_arms", (!textured ? "colored" : "textured") + "_emblems");
        }
    }

    public static class Eu5Renderer extends CoatOfArmsRenderer {

        @Override
        protected Map<String, Color> getPredefinedColors(GameFileContext context) {
            return Eu5CoatOfArmsCache.getPredefinedColors(context);
        }

        @Override
        protected Color getMissingReplacementColor() {
            return Color.color(1, 0, 1);
        }

        @Override
        protected Path getPatternsDir() {
            return Path.of("main_menu", "gfx", "coat_of_arms", "patterns");
        }

        @Override
        protected Path getEmblemDir(boolean textured) {
            return Path.of("main_menu", "gfx", "coat_of_arms", (!textured ? "colored" : "textured") + "_emblems");
        }
    }

    public static final Vic3Renderer VIC3 = new Vic3Renderer();

    public static final Eu5Renderer EU5 = new Eu5Renderer();

    static final int REF_IMG_SIZE = 256;
    private static final int PATTERN_COLOR_1 = 0x00FF0000;
    private static final int PATTERN_COLOR_2 = 0x00FFFF00;
    private static final int PATTERN_COLOR_3 = 0x00FFFFFF;

    protected abstract Map<String, Color> getPredefinedColors(GameFileContext context);

    protected abstract Color getMissingReplacementColor();

    protected abstract Path getPatternsDir();

    protected abstract Path getEmblemDir(boolean textured);

    public BufferedImage renderImage(CoatOfArms coa, GameFileContext ctx, int width, int height) {
        if (coa == null) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setBackground(ColorHelper.toAwtColor(getMissingReplacementColor()));
        g.clearRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        for (var sub : coa.getSubs()) {
            var rawPatternImg = pattern(g, sub, ctx, width, height);
            for (var emblem : sub.getEmblems()) {
                emblem(i, rawPatternImg, sub, emblem, ctx, width, height);
            }
        }

        return i;
    }

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

    private void applySubClip(Graphics graphics, CoatOfArms.Sub sub, int width, int height) {
        var x = (int) Math.floor(sub.getX() * width);
        var y = (int) Math.floor(sub.getY() * height);

        var w = (int) Math.ceil(sub.getScaleX() * width);
        var h = (int) Math.ceil(sub.getScaleY() * height);

        graphics.setClip(x, y, w, h);
    }

    private void applyCullingMask(
            CoatOfArms.Sub sub, BufferedImage emblemImage, BufferedImage patternImage, List<Integer> indices) {
        if (patternImage == null) {
            return;
        }

        double xF = (double) patternImage.getWidth() / emblemImage.getWidth();
        double yF = (double) patternImage.getHeight() / emblemImage.getHeight();
        var maskRgb = (indices.contains(1) ? 0x00FF0000 : 0)
                | (indices.contains(2) ? 0x0000FF00 : 0)
                | (indices.contains(3) ? 0x000000FF : 0);
        for (int x = 0; x < emblemImage.getWidth(); x++) {
            for (int y = 0; y < emblemImage.getHeight(); y++) {
                var cx = (int) Math.floor((xF * x - patternImage.getWidth() * sub.getX()) / sub.getScaleX());
                var cy = (int) Math.floor((yF * y - patternImage.getHeight() * sub.getY()) / sub.getScaleY());
                if (cx < 0 || cy < 0 || cx >= patternImage.getWidth() || cy >= patternImage.getHeight()) {
                    continue;
                }

                int patternArgb = patternImage.getRGB(cx, cy);
                var mr = Math.clamp(
                        ColorHelper.getRed(patternArgb) / 255.0
                                - ColorHelper.getGreen(patternArgb) / 255.0
                                - ColorHelper.getBlue(patternArgb) / 255.0,
                        0.0,
                        1.0);
                var mg = Math.clamp(
                        ColorHelper.getGreen(patternArgb) / 255.0 - ColorHelper.getBlue(patternArgb) / 255.0, 0.0, 1.0);
                var mb = ColorHelper.getBlue(patternArgb) / 255.0;
                int emblemArgb = emblemImage.getRGB(x, y);

                var t = Math.clamp(
                        mr * ColorHelper.getRed(maskRgb) / 255.0
                                + mg * ColorHelper.getGreen(maskRgb) / 255.0
                                + mb * ColorHelper.getBlue(maskRgb) / 255.0,
                        0.0,
                        1.0);
                var alpha = (int) Math.round(ColorHelper.getAlpha(emblemArgb) * t);
                var r = (emblemArgb & 0x00FFFFFF) | (alpha << 24);
                emblemImage.setRGB(x, y, r);
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
        if (sub.getPatternFile() != null) {
            Color pColor1 = evaluateColorDefinition(sub.getColors()[0], ctx);
            Color pColor2 = evaluateColorDefinition(sub.getColors()[1], ctx);
            Color pColor3 = evaluateColorDefinition(sub.getColors()[2], ctx);
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                Color newColor = this.withAlpha(pColor1, getRed(rgb) / 255.0);
                newColor = this.overlayColors(newColor, this.withAlpha(pColor2, getGreen(rgb) / 255.0));
                newColor = this.overlayColors(newColor, this.withAlpha(pColor3, getBlue(rgb) / 255.0));
                int usedColor = intFromColor(newColor) & 0x00FFFFFF;

                int alpha = rgb & 0xFF000000;
                return alpha + usedColor;
            };
            var patternFile = CascadeDirectoryHelper.openFile(getPatternsDir().resolve(sub.getPatternFile()), ctx);
            var image = patternFile
                    .map(p -> ImageHelper.loadAwtImage(p, patternFunction))
                    .orElse(ImageHelper.DEFAULT_AWT_IMAGE);
            g.drawImage(
                    image,
                    (int) (sub.getX() * width),
                    (int) (sub.getY() * height),
                    (int) (sub.getScaleX() * width),
                    (int) (sub.getScaleY() * height),
                    null);
            return patternFile.map(p -> ImageHelper.loadAwtImage(p, null)).orElse(null);
        } else {
            return null;
        }
    }

    public Color withAlpha(Color colour, double maskValue) {
        return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), maskValue);
    }

    public Color overlayColors(Color bottom, Color top) {
        double alpha = top.getOpacity() + bottom.getOpacity() * (1 - top.getOpacity());
        return new Color(
                (top.getRed() * top.getOpacity() + bottom.getRed() * bottom.getOpacity() * (1 - top.getOpacity()))
                        / alpha,
                (top.getGreen() * top.getOpacity() + bottom.getGreen() * bottom.getOpacity() * (1 - top.getOpacity()))
                        / alpha,
                (top.getBlue() * top.getOpacity() + bottom.getBlue() * bottom.getOpacity() * (1 - top.getOpacity()))
                        / alpha,
                alpha);
    }

    private Color evaluateColorDefinition(String color, GameFileContext ctx) {
        if (color == null) {
            return getMissingReplacementColor();
        }

        // For inline colors like rgb { ... }
        var taggedType = Arrays.stream(TaggedNode.COLORS)
                .sorted(Comparator.comparingInt(value -> -value.getId().length()))
                .filter(tagType -> color != null && color.startsWith(tagType.getId()))
                .findFirst();
        if (taggedType.isPresent()) {
            var values = Arrays.stream(color.substring(taggedType.get().getId().length() + 2, color.length() - 1)
                            .split(" "))
                    .filter(s -> !s.isEmpty())
                    .map(s -> new ValueNode(s, false))
                    .toList();
            var node = new TaggedNode(taggedType.get(), values);
            return ColorHelper.fromGameColor(GameColor.fromColorNode(node));
        }

        var colors = getPredefinedColors(ctx);
        return colors.getOrDefault(color, getMissingReplacementColor());
    }

    public void emblem(
            BufferedImage currentImage,
            BufferedImage rawPatternImage,
            CoatOfArms.Sub sub,
            Emblem emblem,
            GameFileContext ctx,
            int width,
            int height) {
        Function<Integer, Integer> customFilter;
        boolean hasColor = emblem.getColors() != null;
        if (emblem.getColors() != null) {
            Color eColor1 = evaluateColorDefinition(emblem.getColors()[0], ctx);
            Color eColor2 = evaluateColorDefinition(emblem.getColors()[1], ctx);
            Color eColor3 = evaluateColorDefinition(emblem.getColors()[2], ctx);

            customFilter = (Integer rgb) -> {
                Color newColor = this.withAlpha(eColor1, 1);
                newColor = this.overlayColors(newColor, this.withAlpha(eColor2, getGreen(rgb) / 255.0));
                newColor = this.overlayColors(newColor, this.withAlpha(eColor3, getRed(rgb) / 255.0));
                int darkShadingA = 255 - Math.min(getBlue(rgb) * 2, 255);
                newColor = this.overlayColors(newColor, Color.color(0, 0, 0, darkShadingA / 255.0));
                int lightShadingA = Math.round(Math.max(Math.min((getBlue(rgb) - 127) * 2, 255), 0));
                newColor = this.overlayColors(newColor, Color.color(1, 1, 1, lightShadingA / 255.0));
                int usedColor = intFromColor(newColor) & 0x00FFFFFF;

                int alpha = rgb & 0xFF000000;
                return alpha + usedColor;
            };
        } else {
            customFilter = (Integer rgb) -> {
                return rgb;
            };
        }

        if (emblem.getFile() == null) {
            return;
        }

        var path = CascadeDirectoryHelper.openFile(getEmblemDir(!hasColor).resolve(emblem.getFile()), ctx);
        if (path.isEmpty()) {
            return;
        }

        var img = ImageHelper.loadAwtImage(path.get(), customFilter);
        boolean hasMask = emblem.getMask().stream().anyMatch(i -> i != 0);
        BufferedImage emblemToCullImage = null;
        if (hasMask) {
            emblemToCullImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D usedGraphics =
                hasMask ? (Graphics2D) emblemToCullImage.getGraphics() : (Graphics2D) currentImage.getGraphics();

        emblem.getInstances().stream()
                .sorted(Comparator.comparingDouble(Emblem.Instance::getDepth))
                .forEach(instance -> {
                    var scaleX = ((double) width / img.getWidth()) * instance.getScaleX() * sub.getScaleX();
                    var scaleY = ((double) height / img.getHeight()) * instance.getScaleY() * sub.getScaleY();

                    var x = width * (sub.getX() + (sub.getScaleX() * instance.getX()));
                    var y = height * (sub.getY() + (sub.getScaleY() * instance.getY()));

                    AffineTransform trans = new AffineTransform();
                    // when doing multiple transformations, they happen in reverse order, so this translate happens last
                    // and moves the emblem to its final position
                    trans.translate(x, y);

                    // restore original aspect ratio
                    trans.scale(((double) img.getWidth()) / ((double) img.getHeight()), 1.0);

                    // Resize
                    trans.scale(Math.abs(scaleX), Math.abs(scaleY));

                    if (instance.getRotation() != 0) {
                        double angle = Math.toRadians(instance.getRotation());
                        trans.rotate(angle);
                    }

                    // flip images with negative scale
                    if (instance.getScaleX() < 0) {
                        trans.scale(-1.0, 1.0);
                    }
                    if (instance.getScaleY() < 0) {
                        trans.scale(1.0, -1.0);
                    }

                    // scale image to make it square, because the game operates on [-1,1] and [0,1] coordinates
                    trans.scale(((double) img.getHeight()) / ((double) img.getWidth()), 1.0);

                    // move image so that 0,0 is in the center
                    trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);

                    try {
                        var op = new AffineTransformOp(trans, AffineTransformOp.TYPE_BICUBIC);
                        if (!hasMask) {
                            applySubClip(usedGraphics, sub, width, height);
                        }
                        usedGraphics.drawImage(img, op, 0, 0);
                    } catch (ImagingOpException ignored) {
                    }
                });

        if (hasMask) {
            applyCullingMask(sub, emblemToCullImage, rawPatternImage, emblem.getMask());
            applySubClip(currentImage.getGraphics(), sub, width, height);
            currentImage.getGraphics().drawImage(emblemToCullImage, 0, 0, new java.awt.Color(0, 0, 0, 0), null);
        }

        currentImage.getGraphics().setClip(0, 0, width, height);
    }

    void renderImage(Graphics g, java.awt.Image img, double x, double y, double w, double h) {
        g.drawImage(
                img,
                (int) Math.round(x),
                (int) Math.round(y),
                (int) Math.round(w),
                (int) Math.round(h),
                new java.awt.Color(0, 0, 0, 0),
                null);
    }
}
