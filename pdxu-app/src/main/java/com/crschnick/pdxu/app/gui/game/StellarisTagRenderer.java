package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class StellarisTagRenderer {

    private static final int IMG_SIZE = 256;

    private static final int PATTERN_COLOR_1 = 0xFFFF0000;
    private static final int PATTERN_COLOR_2 = 0xFF00FF00;

    private static Map<String, javafx.scene.paint.Color> loadPredefinedStellarisColors(Node node, String type) {
        Map<String, javafx.scene.paint.Color> map = new HashMap<>();
        node.getNodeForKeyIfExistent("colors").ifPresent(n -> {
            n.forEach((k, v) -> {
                if (k == null || !v.isArray()) {
                    return;
                }

                v.getNodeForKeyIfExistent(type).filter(Node::isTagged).ifPresent(colorData -> {
                    map.put(k, ColorHelper.fromGameColor(GameColor.fromColorNode(colorData)));
                });
            });
        });
        return map;
    }

    private static synchronized void loadPredefinedColorsForSavegame(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(StellarisTagImageCache.class);
        if (cache.colors.size() > 0) {
            return;
        }

        var file = CascadeDirectoryHelper.openFile(Path.of("flags").resolve("colors.txt"), ctx);
        if (file.isEmpty()) {
            return;
        }

        try {
            Node node = TextFormatParser.text().parse(file.get());
            cache.mapColors.putAll(loadPredefinedStellarisColors(node, "map"));
            cache.colors.putAll(loadPredefinedStellarisColors(node, "flag"));
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
        }
    }

    public static Optional<javafx.scene.paint.Color> getMapColor(GameFileContext ctx, String colorName) {
        loadPredefinedColorsForSavegame(ctx);
        var cache = GameCacheManager.getInstance().get(StellarisTagImageCache.class);
        return Optional.ofNullable(colorName).map(cache.mapColors::get);
    }

    public static Image createTagImage(GameFileContext ctx, StellarisTag tag) {
        // TODO: Cache better
        var img = createBasicFlagImage(ctx, tag);

        ImageHelper.applyAlphaMask(img, GameImage.STELLARIS_FLAG_MASK);
        img.getGraphics()
                .drawImage(ImageHelper.fromFXImage(GameImage.STELLARIS_FLAG_FRAME), 0, 0, IMG_SIZE, IMG_SIZE, null);

        return ImageHelper.toFXImage(img);
    }

    private static BufferedImage createBasicFlagImage(GameFileContext ctx, StellarisTag tag) {
        loadPredefinedColorsForSavegame(ctx);

        BufferedImage bg = background(ctx, tag);
        BufferedImage icon = icon(ctx, tag);

        BufferedImage result = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        var g = result.getGraphics();
        g.drawImage(bg, 0, 0, IMG_SIZE, IMG_SIZE, null);
        g.drawImage(icon, 40, 40, IMG_SIZE - 80, IMG_SIZE - 80, null);

        return result;
    }

    private static BufferedImage icon(GameFileContext ctx, StellarisTag tag) {
        var cache = GameCacheManager.getInstance().get(StellarisTagImageCache.class);

        var path = Path.of("flags", tag.getIconCategory()).resolve(tag.getIconFile());
        if (cache.icons.containsKey(path)) {
            return cache.icons.get(path);
        }

        var iconIn = CascadeDirectoryHelper.openFile(path, ctx);
        if (iconIn.isEmpty()) {
            return ImageHelper.DEFAULT_AWT_IMAGE;
        }

        var img = ImageHelper.loadAwtImage(iconIn.get(), null);
        cache.icons.put(path, img);
        return img;
    }

    private static BufferedImage background(GameFileContext ctx, StellarisTag tag) {
        var cache = GameCacheManager.getInstance().get(StellarisTagImageCache.class);

        int bgPrimary = ColorHelper.intFromColor(
                cache.colors.getOrDefault(tag.getBackgroundPrimaryColor(), javafx.scene.paint.Color.TRANSPARENT));
        int bgSecondary = ColorHelper.intFromColor(
                cache.colors.getOrDefault(tag.getBackgroundSecondaryColor(), javafx.scene.paint.Color.TRANSPARENT));
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            int colorIndex = ColorHelper.pickClosestColor(rgb, PATTERN_COLOR_1, PATTERN_COLOR_2);
            return new int[] {bgPrimary, bgSecondary}[colorIndex];
        };

        var path = Path.of("flags", "backgrounds").resolve(tag.getBackgroundFile());
        if (cache.backgrounds.containsKey(path)) {
            return cache.backgrounds.get(path);
        }

        var in = CascadeDirectoryHelper.openFile(path, ctx);
        if (in.isEmpty()) {
            return ImageHelper.DEFAULT_AWT_IMAGE;
        }

        var img = ImageHelper.loadAwtImage(in.get(), customFilter);
        cache.backgrounds.put(path, img);
        return img;
    }

    public static class StellarisTagImageCache extends GameCacheManager.Cache {

        private final Map<Path, BufferedImage> backgrounds = new ConcurrentHashMap<>();
        private final Map<Path, BufferedImage> icons = new ConcurrentHashMap<>();
        private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();
        private final Map<String, javafx.scene.paint.Color> mapColors = new ConcurrentHashMap<>();

        public StellarisTagImageCache() {
            super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }
    }
}
