package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.core.CacheManager;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.CoatOfArms;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                Path.of("common").resolve("named_colors").resolve("coa_colors.txt"),
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

    public static Image tagFlag(SavegameInfo<Vic3Tag> info, Vic3Tag tag) {
        var cache = CacheManager.getInstance().get(Vic3CoatOfArmsCache.class);
        var cachedImg = cache.flags.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }
        CoatOfArms coa = tag.getCoatOfArms();
        var img = Ck3TagRenderer.renderRealmImage(
                coa, null, GameFileContext.fromData(info.getData()), IMG_SIZE, true);
        cache.flags.put(tag, img);
        return img;
    }

    private boolean colorsLoaded;
    private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();
    private final Map<Vic3Tag, Image> flags = new ConcurrentHashMap<>();

    public Vic3CoatOfArmsCache() {
        super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
