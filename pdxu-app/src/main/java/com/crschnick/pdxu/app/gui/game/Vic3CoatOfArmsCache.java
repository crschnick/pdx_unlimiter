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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.crschnick.pdxu.app.util.ColorHelper.fromGameColor;

public class Vic3CoatOfArmsCache extends CacheManager.Cache {

    private static final int IMG_SIZE = 64;

    static Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext ctx) {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        var loaded = cache.colorsLoaded;
        if (loaded) {
            return cache.colors;
        }

        var file = CascadeDirectoryHelper.openFile(
                Path.of("common").resolve("named_colors").resolve("00_coa_colors.txt"),
                ctx
        );
        if (file.isPresent()) {
            try {
                Node node = TextFormatParser.text().parse(file.get());
                node.getNodeForKeyIfExistent("colors").ifPresent(n -> {
                    n.forEach((k, v) -> {
                        try {
                            cache.colors.put(k, fromGameColor(GameColor.fromColorNode(v)));
                        } catch (Exception ignored) {
                        }
                    });
                });
                cache.colorsLoaded = true;
                return cache.colors;
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }
        cache.colorsLoaded = true;
        return Map.of();
    }

    public static Node getCoatOfArmsNode() {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        if (cache.coatOfArmsNode != null) {
            return cache.coatOfArmsNode;
        }

        var directory = GameInstallation.ALL.get(Game.VIC3)
                .getInstallDir()
                .resolve("game")
                .resolve("common")
                .resolve("coat_of_arms")
                .resolve("coat_of_arms");
        try (Stream<Path> list = Files.list(directory)) {
            var all = new LinkedArrayNode(list.map(path -> {
                ArrayNode content = null;
                try {
                    content = TextFormatParser.vic3().parse(path);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Optional.<ArrayNode>empty();
                }

                // Skip templates
                if (content.size() == 1) {
                    return Optional.<ArrayNode>empty();
                }

                NodeEvaluator.evaluateArrayNode(content);
                return Optional.of(content);
            }).flatMap(Optional::stream).toList());
            cache.coatOfArmsNode = all;
        } catch (IOException e) {
            cache.coatOfArmsNode = ArrayNode.array(List.of());
        }

        return cache.coatOfArmsNode;
    }

    public static Image tagFlag(SavegameInfo<Vic3Tag> info, Vic3Tag tag) {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        var cachedImg = cache.flags.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }

        CoatOfArms coa = CoatOfArms.fromNode(getCoatOfArmsNode().getNodeForKey(tag.getTag()), s -> getCoatOfArmsNode().getNodeForKey(s));
        var img = Vic3TagRenderer.renderImage(
                coa, GameFileContext.fromData(info.getData()), (int) (IMG_SIZE * 1.5), IMG_SIZE);
        var convertedImage = ImageHelper.toFXImage(img);
        cache.flags.put(tag, convertedImage);
        return convertedImage;
    }

    private boolean colorsLoaded;
    private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();
    private final Map<Vic3Tag, Image> flags = new ConcurrentHashMap<>();
    private Node coatOfArmsNode;

    public Vic3CoatOfArmsCache() {
        super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
