package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeEnvironment;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.crschnick.pdxu.app.util.ColorHelper.fromGameColor;

public class Vic3CoatOfArmsCache extends GameCacheManager.Cache {

    private static final int IMG_SIZE = 64;

    static Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        var loaded = cache.colorsLoaded;
        if (loaded) {
            return cache.colors;
        }

        cache.colorsLoaded = true;
        Consumer<Path> loader = path -> {
            try {
                Node node = TextFormatParser.text().parse(path);
                node.getNodeForKeyIfExistent("colors").ifPresent(n -> {
                    n.forEach((k, v) -> {
                        try {
                            if (v.isArray()) {
                                cache.colors.put(k, fromGameColor(GameColor.fromRgbArray(v)));
                            } else if (v.isTagged()) {
                                cache.colors.put(k, fromGameColor(GameColor.fromColorNode(v)));
                            }
                        } catch (Exception ignored) {
                        }
                    });
                });
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).handle();
            }
        };
        CascadeDirectoryHelper.traverseDirectory(Path.of("common").resolve("named_colors"), ctx, loader);

        var jominiColors = GameInstallation.ALL
                .get(Game.VIC3)
                .getInstallDir()
                .resolve("jomini/common/named_colors/default_colors.txt");
        if (Files.exists(jominiColors)) {
            loader.accept(jominiColors);
        }

        return cache.colors;
    }

    private static synchronized Optional<javafx.scene.paint.Color> getCountryColor(GameFileContext ctx, String tag) {
        var cache = GameCacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        if (cache.countryColors == null) {
            var map = new HashMap<String, javafx.scene.paint.Color>();
            CascadeDirectoryHelper.traverseDirectory(Path.of("common").resolve("country_definitions"), ctx, path -> {
                try {
                    TextFormatParser.vic3().parse(path).forEach((k, v) -> {
                        var c = v.getNodeForKeyIfExistent("color");
                        if (c.isPresent()) {
                            map.put(k, fromGameColor(c.get().isTagged()
                                    ? GameColor.fromColorNode(c.get())
                                    : GameColor.fromRgbArray(c.get())));
                        }
                    });
                } catch (Exception ex) {
                    ErrorEventFactory.fromThrowable(ex).omit().handle();
                }
            });
            cache.countryColors = map;
        }
        return Optional.ofNullable(cache.countryColors.get(tag));
    }

    public static Node getCoatOfArmsNode(GameFileContext context) {
        var cache = GameCacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        if (cache.coatOfArmsNode != null) {
            return cache.coatOfArmsNode;
        }

        var all = Vic3TagRenderer.loadCoatOfArmsNode(new NodeEnvironment(Map.of()), context);
        cache.coatOfArmsNode = all;
        return cache.coatOfArmsNode;
    }

    public static Image tagFlag(SavegameInfo<Vic3Tag> info, Vic3Tag tag) {
        return tagFlag(info.getData(), tag);
    }

    public static Image tagFlag(SavegameData<Vic3Tag> data, Vic3Tag tag) {
        var cache = GameCacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        var cachedImg = cache.flags.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }

        var context = GameFileContext.fromData(data);
        var allNode = getCoatOfArmsNode(context);
        var isPlayer = tag.equals(data.getTag());
        if (!isPlayer && allNode.getNodeForKeyIfExistent(tag.getTag()).isEmpty()) {
            var fallback = getCountryColor(context, tag.getTag())
                    .<Image>map(color -> {
                        var img = new WritableImage((int) (IMG_SIZE * 1.5), IMG_SIZE);
                        for (int x = 0; x < img.getWidth(); x++) {
                            for (int y = 0; y < img.getHeight(); y++) {
                                img.getPixelWriter().setColor(x, y, color);
                            }
                        }
                        return img;
                    })
                    .orElse(ImageHelper.DEFAULT_IMAGE);
            cache.flags.put(tag, fallback);
            return fallback;
        }

        try {
            Supplier<CoatOfArms> coa = isPlayer
                    ? () -> data.vic3().getCoatOfArms()
                    : () -> CoatOfArms.fromNode(
                            allNode.getNodeForKeyIfExistent(tag.getTag()).orElseThrow(), s -> {
                                var found = allNode.getNodesForKey(s);
                                return !found.isEmpty() ? found.getLast() : null;
                            });
            var img = Vic3TagRenderer.renderImage(coa.get(), context, (int) (IMG_SIZE * 1.5), IMG_SIZE);
            var convertedImage = ImageHelper.toFXImage(img);
            cache.flags.put(tag, convertedImage);
            return convertedImage;
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
            return ImageHelper.DEFAULT_IMAGE;
        }
    }

    private boolean colorsLoaded;
    private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();
    private final Map<Vic3Tag, Image> flags = new ConcurrentHashMap<>();
    private Node coatOfArmsNode;
    private Map<String, javafx.scene.paint.Color> countryColors;

    public Vic3CoatOfArmsCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
