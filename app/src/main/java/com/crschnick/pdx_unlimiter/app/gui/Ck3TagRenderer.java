package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.function.Function;

import static com.crschnick.pdx_unlimiter.app.util.ColorHelper.*;

public class Ck3TagRenderer {

    private static final int IMG_SIZE = 256;

    private static final int PATTERN_COLOR_1 = 0x00FF0000;
    private static final int PATTERN_COLOR_2 = 0x00FFFF00;
    private static final int PATTERN_COLOR_3 = 0x00FFFFFF;

    private static final int EMBLEM_COLOR_1 = 0x000080;
    private static final int EMBLEM_COLOR_2 = 0x00FF00;
    private static final int EMBLEM_COLOR_3 = 0xFF0080;

    public static Image realmImage(SavegameInfo<Ck3Tag> info, Ck3Tag.CoatOfArms coa) {
        BufferedImage coaImg = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics coaG = coaImg.getGraphics();


        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        pattern(coaG, coa, info);
        for (var emblem : coa.getEmblems()) {
            emblem(coaG, emblem, info);
        }

        g.drawImage(coaImg,
                0,
                0,
                i.getWidth(),
                i.getHeight(),
                new java.awt.Color(0, 0, 0, 0),
                null);


        applyMask(i, GameImage.CK3_REALM_MASK);
        brighten(i);


        g.drawImage(ImageLoader.fromFXImage(GameImage.CK3_REALM_FRAME),
                3,
                -12,
                i.getWidth() - 6,
                i.getHeight() + 24,
                new java.awt.Color(0, 0, 0, 0),
                null);

        return ImageLoader.toFXImage(i);
    }

    public static Image houseImage(SavegameInfo<Ck3Tag> info, Ck3Tag.CoatOfArms coa) {
        BufferedImage coaImg = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics coaG = coaImg.getGraphics();


        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        pattern(coaG, coa, info);
        for (var emblem : coa.getEmblems()) {
            emblem(coaG, emblem, info);
        }

        applyMask(coaImg, GameImage.CK3_HOUSE_MASK);

        g.drawImage(coaImg,
                20,
                20,
                i.getWidth() -40,
                i.getHeight() -40,
                new java.awt.Color(0, 0, 0, 0),
                null);


        applyMask(i, GameImage.CK3_COA_OVERLAY);
        brighten(i);

        g.drawImage(ImageLoader.fromFXImage(GameImage.CK3_HOUSE_FRAME),
                -25,
                -15,
                i.getWidth() + 33,
                i.getHeight() + 30,
                new java.awt.Color(0, 0, 0, 0),
                null);

        return ImageLoader.toFXImage(i);
    }

    public static Image titleImage(SavegameInfo<Ck3Tag> info, Ck3Tag.CoatOfArms coa) {
        BufferedImage coaImg = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics coaG = coaImg.getGraphics();


        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        pattern(coaG, coa, info);
        for (var emblem : coa.getEmblems()) {
            emblem(coaG, emblem, info);
        }

        applyMask(coaImg, GameImage.CK3_TITLE_MASK);

        g.drawImage(coaImg,
                20,
                20,
                i.getWidth() -40,
                i.getHeight() -40,
                new java.awt.Color(0, 0, 0, 0),
                null);

        g.drawImage(ImageLoader.fromFXImage(GameImage.CK3_TITLE_FRAME),
                -9,
                -6,
                i.getWidth() + 17,
                i.getHeight() + 17,
                new java.awt.Color(0, 0, 0, 0),
                null);


        return ImageLoader.toFXImage(i);
    }

    private static void brighten(BufferedImage awtImage) {
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int color = (getAlpha(argb) << 24) +
                        (Math.min((int) (1.6 * getRed(argb)), 255) << 16) +
                        (Math.min((int) (1.6 * getGreen(argb)), 255) << 8)+
                        (Math.min((int) (1.6 * getBlue(argb)), 255));
                awtImage.setRGB(x, y, color);
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
                        ( (  (int) ((getGreen(maskArgb) / 255.0) * getGreen(argb))) << 8) +
                        (((int) ((getBlue(maskArgb) / 255.0) * getBlue(argb))));
                awtImage.setRGB(x, y, color);
            }
        }
    }

    private static int pickClosestColor(int input, int... colors) {
        int minDist = Integer.MAX_VALUE;
        int cMin = -1;
        int counter = 0;
        for (int c : colors) {
            if (Math.abs(input - c) < minDist) {
                minDist = Math.abs(input - c);
                cMin = counter;
            }
            counter++;
        }
        return cMin;
    }

    private static void pattern(Graphics g, Ck3Tag.CoatOfArms coa, SavegameInfo<Ck3Tag> info) {
        if (coa.getPatternFile() != null) {
            int pColor1 = coa.getColors().size() > 0 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(coa.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            int pColor2 = coa.getColors().size() > 1 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(coa.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            int pColor3 = coa.getColors().size() > 2 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(coa.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                int alpha = rgb & 0xFF000000;
                int color = rgb & 0x00FFFFFF;
                int colorIndex = pickClosestColor(color, PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                int usedColor = new int[] {pColor1, pColor2, pColor3}[colorIndex] & 0x00FFFFFF;
                return alpha + usedColor;
            };
            var patternFile = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms", "patterns").resolve(coa.getPatternFile()),
                    info,
                    GameInstallation.CK3);
            patternFile.map(p -> ImageLoader.loadAwtImage(p, patternFunction)).ifPresent(img -> {
                g.drawImage(img, 0, 0, IMG_SIZE, IMG_SIZE, null);
            });
        }
    }

    private static void emblem(Graphics g, Ck3Tag.CoatOfArms.Emblem emblem, SavegameInfo<Ck3Tag> info) {
        int eColor1 = emblem.getColors().size() > 0 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                .getOrDefault(emblem.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        int eColor2 = emblem.getColors().size() > 1 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                .getOrDefault(emblem.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        int eColor3 = emblem.getColors().size() > 2 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                .getOrDefault(emblem.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            int alpha = rgb & 0xFF000000;
            int color = rgb & 0x00FFFFFF;
            int colorIndex = pickClosestColor(color, EMBLEM_COLOR_1, EMBLEM_COLOR_2, EMBLEM_COLOR_3);
            int usedColor = new int[] {eColor1, eColor2, eColor3}[colorIndex] & 0x00FFFFFF;
            return alpha + usedColor;
        };

        boolean hasColor = emblem.getColors().size() > 0;
        var path = CascadeDirectoryHelper.openFile(
                Path.of("gfx", "coat_of_arms",
                        (hasColor ? "colored" : "textured") + "_emblems").resolve(emblem.getFile()),
                info,
                GameInstallation.CK3);
        path.map(p -> ImageLoader.loadAwtImage(p, customFilter)).ifPresent(img -> {
            for (var instance : emblem.getInstances()) {
                int width = (int) (instance.getScaleX() * IMG_SIZE);
                int height = (int) (instance.getScaleY() * IMG_SIZE);
                int startX = (int) ((instance.getX() * IMG_SIZE) - (width / 2.0));
                int startY = (int) ((instance.getY() * IMG_SIZE) - (height / 2.0));
                g.drawImage(img,
                        startX,
                        startY,
                        width,
                        height,
                        new java.awt.Color(0, 0, 0, 0),
                        null);
            }
        });
    }
}
