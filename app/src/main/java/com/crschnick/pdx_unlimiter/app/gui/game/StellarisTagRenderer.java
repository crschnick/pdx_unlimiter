package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.info.GameColor;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.crschnick.pdx_unlimiter.core.node.ColorNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.crschnick.pdx_unlimiter.app.util.ColorHelper.fromGameColor;
import static com.crschnick.pdx_unlimiter.app.util.ColorHelper.pickClosestColor;

public class StellarisTagRenderer {

    private static final int IMG_SIZE = 256;

    private static final int PATTERN_COLOR_1 = 0xFFFF0000;
    private static final int PATTERN_COLOR_2 = 0xFF00FF00;

    public static class StellarisTagImageCache extends CacheManager.Cache {

        private final Map<Path, BufferedImage> backgrounds = new ConcurrentHashMap<>();
        private final Map<Path, BufferedImage> icons = new ConcurrentHashMap<>();
        private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();

        public StellarisTagImageCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }
    }

    private static Map<String, javafx.scene.paint.Color> loadPredefinedStellarisColors(Node node) {
        Map<String, javafx.scene.paint.Color> map = new HashMap<>();
        node.getNodeForKeyIfExistent("colors").ifPresent(n -> {
            n.forEach((k, v) -> {
                ColorNode colorData = (ColorNode) v.getNodeForKey("flag");
                map.put(k, fromGameColor(GameColor.fromColorNode(colorData)));
            });
        });
        return map;
    }

    private static Map<String, javafx.scene.paint.Color> loadPredefinedColorsForSavegame(SavegameInfo<StellarisTag> info) {
        var file = CascadeDirectoryHelper.openFile(
                Path.of("flags").resolve("colors.txt"), info);
        if (file.isEmpty()) {
            return Map.of();
        }

        try {
            Node node = TextFormatParser.textFileParser().parse(file.get());
            return loadPredefinedStellarisColors(node);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }

    public static Image createTagImage(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        //TODO: Cache better
        var img = createBasicFlagImage(info, tag);

        ImageLoader.applyAlphaMask(img, GameImage.STELLARIS_FLAG_MASK);
        img.getGraphics().drawImage(ImageLoader.fromFXImage(GameImage.STELLARIS_FLAG_FRAME), 0, 0, IMG_SIZE, IMG_SIZE, null);

        return ImageLoader.toFXImage(img);
    }

    private static BufferedImage createBasicFlagImage(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        var cache = CacheManager.getInstance().get(StellarisTagImageCache.class);
        if (cache.colors.size() == 0) {
            cache.colors.putAll(loadPredefinedColorsForSavegame(info));
        }

        BufferedImage bg = background(info, tag);
        BufferedImage icon = icon(info, tag);

        BufferedImage result = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        var g = result.getGraphics();
        g.drawImage(bg, 0, 0, IMG_SIZE, IMG_SIZE, null);
        g.drawImage(icon, 38, 38, IMG_SIZE - 76, IMG_SIZE - 76, null);

        return result;
    }


    private static BufferedImage icon(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        var cache = CacheManager.getInstance().get(StellarisTagImageCache.class);

        var path = Path.of("flags", tag.getIconCategory()).resolve(tag.getIconFile());
        if (cache.icons.containsKey(path)) {
            return cache.icons.get(path);
        }

        var iconIn = CascadeDirectoryHelper.openFile(path, info);
        if (iconIn.isEmpty()) {
            return ImageLoader.DEFAULT_AWT_IMAGE;
        }

        var img = ImageLoader.loadAwtImage(iconIn.get(), null);
        cache.icons.put(path, img);
        return img;
    }

    private static BufferedImage background(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        var cache = CacheManager.getInstance().get(StellarisTagImageCache.class);

        int bgPrimary = ColorHelper.intFromColor(cache.colors.getOrDefault(
                tag.getBackgroundPrimaryColor(), javafx.scene.paint.Color.TRANSPARENT));
        int bgSecondary = ColorHelper.intFromColor(cache.colors.getOrDefault(
                tag.getBackgroundSecondaryColor(), javafx.scene.paint.Color.TRANSPARENT));
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            int colorIndex = pickClosestColor(rgb, PATTERN_COLOR_1, PATTERN_COLOR_2);
            return new int[]{bgPrimary, bgSecondary}[colorIndex];
        };

        var path = Path.of("flags", "backgrounds").resolve(tag.getBackgroundFile());
        if (cache.backgrounds.containsKey(path)) {
            return cache.backgrounds.get(path);
        }

        var in = CascadeDirectoryHelper.openFile(path, info);
        if (in.isEmpty()) {
            return ImageLoader.DEFAULT_AWT_IMAGE;
        }

        var img = ImageLoader.loadAwtImage(in.get(), customFilter);
        cache.backgrounds.put(path, img);
        return img;
    }
}
