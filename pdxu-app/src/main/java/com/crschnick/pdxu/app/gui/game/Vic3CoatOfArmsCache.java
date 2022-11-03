package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.core.CacheManager;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.CoatOfArms;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.crschnick.pdxu.app.util.ColorHelper.fromGameColor;

public class Vic3CoatOfArmsCache extends CacheManager.Cache {

    private static final int IMG_SIZE = 64;

    static Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext ctx) {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
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
                            cache.colors.put(k, fromGameColor(GameColor.fromColorNode(v)));
                        } catch (Exception ignored) {
                        }
                    });
                });
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        };
        CascadeDirectoryHelper.traverseDirectory(Path.of("common").resolve("named_colors"), ctx, loader);

        var jominiColors = GameInstallation.ALL.get(Game.VIC3)
                .getInstallDir().resolve("jomini/common/named_colors/default_colors.txt");
        if (Files.exists(jominiColors)) {
            loader.accept(jominiColors);
        }

        return cache.colors;
    }

    public static Node getCoatOfArmsNode(GameFileContext context) {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        if (cache.coatOfArmsNode != null) {
            return cache.coatOfArmsNode;
        }

        var dir = Path.of("common")
                .resolve("coat_of_arms")
                .resolve("coat_of_arms");
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(dir, context, files::add);

        var all = new LinkedArrayNode(files.stream().map(path -> {
            ArrayNode content = null;
            try {
                content = TextFormatParser.vic3().parse(path);
            } catch (Exception e) {
                return Optional.<ArrayNode>empty();
            }

            NodeEvaluator.evaluateArrayNode(content);
            return Optional.of(content);
        }).flatMap(Optional::stream).toList());
        cache.coatOfArmsNode = all;

        return cache.coatOfArmsNode;
    }

    public static Image tagFlag(SavegameInfo<Vic3Tag> info, Vic3Tag tag) {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        var cachedImg = cache.flags.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }

        var context = GameFileContext.fromData(info.getData());
        var all = getCoatOfArmsNode(context);
        try {
            Supplier<CoatOfArms> coa = tag == info.getData().getTag() ?
                    () -> info.getData().vic3().getCoatOfArms() :
                    () -> CoatOfArms.fromNode(all.getNodeForKeyIfExistent(tag.getTag()).orElseThrow(), s -> {
                        var found = all.getNodesForKey(s);
                        return found.size() > 0 ? found.get(found.size() - 1) : null;
                    });
            var img = Vic3TagRenderer.renderImage(
                    coa.get(), context, (int) (IMG_SIZE * 1.5), IMG_SIZE);
            var convertedImage = ImageHelper.toFXImage(img);
            cache.flags.put(tag, convertedImage);
            return convertedImage;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return ImageHelper.DEFAULT_IMAGE;
        }
    }

    private boolean colorsLoaded;
    private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();
    private final Map<Vic3Tag, Image> flags = new ConcurrentHashMap<>();
    private Node coatOfArmsNode;

    public Vic3CoatOfArmsCache() {
        super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
