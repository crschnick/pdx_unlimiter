package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.eu5.Eu5Tag;

import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.crschnick.pdxu.app.util.ColorHelper.fromGameColor;

public class Eu5CoatOfArmsCache extends GameCacheManager.Cache {

    private static final int IMG_SIZE = 64;

    private static synchronized Map<String, String> getFlagDefinitionMap(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Eu5CoatOfArmsCache.class);
        var loaded = !cache.flagDefinitionMap.isEmpty();
        if (loaded) {
            return cache.flagDefinitionMap;
        }

        Consumer<Path> loader = path -> {
            try {
                Node node = TextFormatParser.text().parse(path);
                node.forEach((k, v) -> {
                    if (!v.isArray()) {
                        return;
                    }

                    var defs = v.getNodesForKey("flag_definition");
                    if (defs.size() > 0) {
                        var def = defs.getFirst();
                        cache.flagDefinitionMap.put(k, def.getNodeForKey("coa").getString());
                    }
                });
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).handle();
            }
        };
        CascadeDirectoryHelper.traverseDirectory(Path.of("main_menu", "common", "flag_definitions"), ctx, loader);

        return cache.flagDefinitionMap;
    }

    static synchronized Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Eu5CoatOfArmsCache.class);
        var loaded = cache.colorsLoaded;
        if (loaded) {
            return cache.colors;
        }

        cache.colorsLoaded = true;
        Consumer<Path> loader = path -> {
            try {
                Node node = TextFormatParser.text().parse(path);
                node.getNodeForKeyIfExistent("colors").filter(Node::isArray).ifPresent(n -> {
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
        CascadeDirectoryHelper.traverseDirectory(Path.of("main_menu", "common", "named_colors"), ctx, loader);

        var jominiColors = GameInstallation.ALL
                .get(Game.EU5)
                .getInstallDir()
                .resolve("jomini/loading_screen/common/named_colors/default_colors.txt");
        if (Files.exists(jominiColors)) {
            loader.accept(jominiColors);
        }

        return cache.colors;
    }

    public static synchronized Node getCoatOfArmsNode(GameFileContext context) {
        var cache = GameCacheManager.getInstance().get(Eu5CoatOfArmsCache.class);
        if (cache.coatOfArmsNode != null) {
            return cache.coatOfArmsNode;
        }

        var all = Eu5TagRenderer.getCoatOfArmsNode(context);
        cache.coatOfArmsNode = all;
        return cache.coatOfArmsNode;
    }

    public static synchronized Image tagFlag(SavegameData<Eu5Tag> data, Eu5Tag tag) {
        var cache = GameCacheManager.getInstance().get(Eu5CoatOfArmsCache.class);
        var cachedImg = cache.flags.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }

        var context = GameFileContext.fromData(data);
        var defintionMap = getFlagDefinitionMap(context);
        var all = getCoatOfArmsNode(context);
        try {
            Supplier<CoatOfArms> coa = () -> {
                if (tag == null) {
                    return CoatOfArms.empty();
                }

                if (tag == data.getTag()) {
                    var mainCoa = data.eu5().getTagCoa();
                    return mainCoa != null ? mainCoa : CoatOfArms.empty();
                }

                var key = defintionMap.getOrDefault(tag.getFlagTag(), tag.getFlagTag());
                var found = all.getNodeForKeyIfExistent(key);
                if (found.isEmpty()) {
                    return CoatOfArms.empty();
                }

                return CoatOfArms.fromNode(found.get(), s -> {
                    var parentFound = all.getNodesForKey(s);
                    return !parentFound.isEmpty() ? parentFound.getLast() : null;
                });
            };
            var img = Eu5TagRenderer.renderImage(coa.get(), context, (int) (IMG_SIZE * 1.5), IMG_SIZE);
            var convertedImage = ImageHelper.toFXImage(img);
            cache.flags.put(tag, convertedImage);
            return convertedImage;
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
            return ImageHelper.DEFAULT_IMAGE;
        }
    }

    private boolean colorsLoaded;
    private final Map<String, javafx.scene.paint.Color> colors = new HashMap<>();
    private final Map<Eu5Tag, Image> flags = new HashMap<>();
    private final Map<String, String> flagDefinitionMap = new HashMap<>();
    private Node coatOfArmsNode;

    public Eu5CoatOfArmsCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
