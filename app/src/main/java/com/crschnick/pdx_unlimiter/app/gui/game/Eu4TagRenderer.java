package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3CoatOfArms;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Eu4TagRenderer {

    private static final int SMALL_IMG_SIZE = 60;
    private static final int IMG_SIZE = 256;

    public static Image smallShieldImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        BufferedImage flagImage = ImageLoader.fromFXImage(eu4TagImage(info, tag));
        applyMask(flagImage, GameImage.EU4_SMALL_SHIELD_MASK);

        BufferedImage i = new BufferedImage(SMALL_IMG_SIZE, SMALL_IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        g.drawImage(flagImage,
                8,
                8,
                i.getWidth() - 16,
                i.getHeight() - 16,
                new java.awt.Color(0, 0, 0, 0),
                null);

        g.drawImage(ImageLoader.fromFXImage(GameImage.EU4_SMALL_SHIELD_FRAME),
                -2,
                -4,
                i.getWidth() + 4,
                i.getWidth() + 4,
                new java.awt.Color(0, 0, 0, 0),
                null);

        return ImageLoader.toFXImage(i);
    }

    public static Image shieldImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        BufferedImage flagImage = ImageLoader.fromFXImage(eu4TagImage(info, tag));
        applyMask(flagImage, GameImage.EU4_SHIELD_MASK);

        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        g.drawImage(flagImage,
                32,
                42,
                i.getWidth() - 64,
                i.getHeight() - 56,
                new java.awt.Color(0, 0, 0, 0),
                null);

        g.drawImage(ImageLoader.fromFXImage(GameImage.EU4_SHIELD_FRAME),
                -20,
                0,
                i.getWidth() + 40,
                i.getHeight() + 20,
                new java.awt.Color(0, 0, 0, 0),
                null);

        return ImageLoader.toFXImage(i);
    }

    private static void applyMask(BufferedImage awtImage, Image mask) {
        double xF = mask.getWidth() / awtImage.getWidth();
        double yF = mask.getHeight() / awtImage.getHeight();
        for (int x = 0; x < awtImage.getWidth(); x++) {
            for (int y = 0; y < awtImage.getHeight(); y++) {
                int argb = awtImage.getRGB(x, y);
                int maskArgb = mask.getPixelReader().getArgb(
                        (int) Math.floor(xF * x), (int) Math.floor(yF * y));
                int maskAlpha = maskArgb & 0xFF000000;

                int color = (argb & 0x00FFFFFF) + maskAlpha;
                awtImage.setRGB(x, y, color);
            }
        }
    }

    public static class Eu4TagImageCache extends CacheManager.Cache {
        Map<String, Image> tagImages = new HashMap<>();

        public Eu4TagImageCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }
    }

    private static Image eu4TagImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        return CacheManager.getInstance().get(Eu4TagImageCache.class).tagImages.computeIfAbsent(
                tag.getTag(), s -> eu4TagImage(GameImage.getEu4TagPath(s), info));
    }

    private static Image eu4TagImage(Path path, SavegameInfo<Eu4Tag> info) {
        var in = CascadeDirectoryHelper.openFile(
                path, info, GameInstallation.ALL.get(Game.EU4));
        return ImageLoader.loadImage(in.orElse(null), null);
    }
}
