package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3CoatOfArms;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3House;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Title;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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


    public static class CoatOfArmsCache extends CacheManager.Cache {

        private final Map<Ck3Tag, Image> realms = new HashMap<>();
        private final Map<Ck3Title, Image> titles = new HashMap<>();
        private final Map<Ck3House, Image> houses = new HashMap<>();
        private final Map<String, javafx.scene.paint.Color> colors = new HashMap<>();

        public CoatOfArmsCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }
    }

    public static Image realmImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        var cache = CacheManager.getInstance().get(CoatOfArmsCache.class);
        var cachedImg = cache.realms.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }


        Ck3CoatOfArms coa = tag.getCoatOfArms();
        BufferedImage coaImg = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D coaG = (Graphics2D) coaImg.getGraphics();


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

        var masks = Map.of(
                "clan_government", GameImage.CK3_REALM_CLAN_MASK,
                "republic_government", GameImage.CK3_REALM_REPUBLIC_MASK,
                "theocracy_government", GameImage.CK3_REALM_THEOCRACY_MASK,
                "tribal_government", GameImage.CK3_REALM_THEOCRACY_MASK);
        var useMask = masks.getOrDefault(tag.getGovernmentName(), GameImage.CK3_REALM_MASK);
        applyMask(i, useMask);
        brighten(i);


        var frames = Map.of(
                "clan_government", GameImage.CK3_REALM_CLAN_FRAME,
                "republic_government", GameImage.CK3_REALM_REPUBLIC_FRAME,
                "theocracy_government", GameImage.CK3_REALM_THEOCRACY_FRAME,
                "tribal_government", GameImage.CK3_REALM_THEOCRACY_FRAME);
        var useFrame = frames.getOrDefault(tag.getGovernmentName(), GameImage.CK3_REALM_FRAME);
        g.drawImage(ImageLoader.fromFXImage(useFrame),
                3,
                -12,
                i.getWidth() - 6,
                i.getHeight() + 24,
                new java.awt.Color(0, 0, 0, 0),
                null);

        var img = ImageLoader.toFXImage(i);
        cache.realms.put(tag, img);
        return img;
    }

    public static Image houseImage(SavegameInfo<Ck3Tag> info, Ck3House house) {
        var cache = CacheManager.getInstance().get(CoatOfArmsCache.class);
        var cachedImg = cache.houses.get(house);
        if (cachedImg != null) {
            return cachedImg;
        }

        Ck3CoatOfArms coa = house.getCoatOfArms();
        BufferedImage coaImg = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D coaG = (Graphics2D) coaImg.getGraphics();


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
                i.getWidth() - 40,
                i.getHeight() - 40,
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

        var img = ImageLoader.toFXImage(i);
        cache.houses.put(house, img);
        return img;
    }

    public static Image titleImage(SavegameInfo<Ck3Tag> info, Ck3Title title) {
        var cache = CacheManager.getInstance().get(CoatOfArmsCache.class);
        var cachedImg = cache.titles.get(title);
        if (cachedImg != null) {
            return cachedImg;
        }

        Ck3CoatOfArms coa = title.getCoatOfArms();
        BufferedImage coaImg = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D coaG = (Graphics2D) coaImg.getGraphics();


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
                i.getWidth() - 40,
                i.getHeight() - 40,
                new java.awt.Color(0, 0, 0, 0),
                null);

        g.drawImage(ImageLoader.fromFXImage(GameImage.CK3_TITLE_FRAME),
                -9,
                -6,
                i.getWidth() + 17,
                i.getHeight() + 17,
                new java.awt.Color(0, 0, 0, 0),
                null);


        var img = ImageLoader.toFXImage(i);
        cache.titles.put(title, img);
        return img;
    }

    private static void brighten(BufferedImage awtImage) {
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int color = (getAlpha(argb) << 24) +
                        (Math.min((int) (1.6 * getRed(argb)), 255) << 16) +
                        (Math.min((int) (1.6 * getGreen(argb)), 255) << 8) +
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
                        (((int) ((getGreen(maskArgb) / 255.0) * getGreen(argb))) << 8) +
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

    private static void pattern(Graphics g, Ck3CoatOfArms coa, SavegameInfo<Ck3Tag> info) {
        var cache = CacheManager.getInstance().get(CoatOfArmsCache.class);
        if (cache.colors.size() == 0) {
            cache.colors.putAll(ColorHelper.loadCk3(info));
        }

        if (coa.getPatternFile() != null) {
            int pColor1 = coa.getColors().size() > 0 ? ColorHelper.intFromColor(cache.colors
                    .getOrDefault(coa.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            int pColor2 = coa.getColors().size() > 1 ? ColorHelper.intFromColor(cache.colors
                    .getOrDefault(coa.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            int pColor3 = coa.getColors().size() > 2 ? ColorHelper.intFromColor(cache.colors
                    .getOrDefault(coa.getColors().get(2), javafx.scene.paint.Color.TRANSPARENT)) : 0;
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                int alpha = rgb & 0xFF000000;
                int color = rgb & 0x00FFFFFF;
                int colorIndex = pickClosestColor(color, PATTERN_COLOR_1, PATTERN_COLOR_2, PATTERN_COLOR_3);
                int usedColor = new int[]{pColor1, pColor2, pColor3}[colorIndex] & 0x00FFFFFF;
                return alpha + usedColor;
            };
            var patternFile = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms", "patterns").resolve(coa.getPatternFile()),
                    info,
                    GameInstallation.ALL.get(Game.CK3));
            patternFile.map(p -> ImageLoader.loadAwtImage(p, patternFunction)).ifPresent(img -> {
                g.drawImage(img, 0, 0, IMG_SIZE, IMG_SIZE, null);
            });
        }
    }

    private static void emblem(Graphics2D g, Ck3CoatOfArms.Emblem emblem, SavegameInfo<Ck3Tag> info) {
        var cache = CacheManager.getInstance().get(CoatOfArmsCache.class);
        if (cache.colors.size() == 0) {
            cache.colors.putAll(ColorHelper.loadCk3(info));
        }

        int eColor1 = emblem.getColors().size() > 0 ? ColorHelper.intFromColor(cache.colors
                .getOrDefault(emblem.getColors().get(0), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        int eColor2 = emblem.getColors().size() > 1 ? ColorHelper.intFromColor(cache.colors
                .getOrDefault(emblem.getColors().get(1), javafx.scene.paint.Color.TRANSPARENT)) : 0;
        int eColor3 = emblem.getColors().size() > 2 ? ColorHelper.intFromColor(cache.colors
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
                info,
                GameInstallation.ALL.get(Game.CK3));
        path.map(p -> ImageLoader.loadAwtImage(p, customFilter)).ifPresent(img -> {
            for (var instance : emblem.getInstances()) {
                var scaleX = ((double) IMG_SIZE / img.getWidth()) * instance.getScaleX();
                var scaleY = ((double) IMG_SIZE / img.getHeight()) * instance.getScaleY();

                AffineTransform trans = new AffineTransform();

                trans.translate(IMG_SIZE * instance.getX(), IMG_SIZE * instance.getY());
                trans.scale(scaleX, scaleY);
                trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);

                if (instance.getRotation() != 0) {
                    trans.translate(img.getWidth() / 2.0, img.getHeight() / 2.0);
                    trans.rotate(Math.signum(scaleX) * Math.signum(scaleY) * Math.toRadians(instance.getRotation()));
                    trans.translate(-img.getWidth() / 2.0, -img.getHeight() / 2.0);
                }

                g.drawImage(img, trans, null);
            }
        });
    }
}
