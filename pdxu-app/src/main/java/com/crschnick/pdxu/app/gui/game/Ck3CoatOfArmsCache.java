package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.ck3.Ck3House;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import com.crschnick.pdxu.model.ck3.Ck3Title;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.crschnick.pdxu.app.util.ColorHelper.fromGameColor;

public class Ck3CoatOfArmsCache extends GameCacheManager.Cache {

    private static final int REALM_DEFAULT_IMG_SIZE = 64;
    private static final int HOUSE_DEFAULT_IMG_SIZE = 128;
    private static final int TITLE_DEFAULT_IMG_SIZE = 64;

    static Map<String, javafx.scene.paint.Color> getPredefinedColors(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Ck3CoatOfArmsCache.class);
        var loaded = cache.colorsLoaded;
        if (loaded) {
            return cache.colors;
        }

        var file = CascadeDirectoryHelper.openFile(
                Path.of("common").resolve("named_colors").resolve("default_colors.txt"),
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
                ErrorEventFactory.fromThrowable(ex).handle();
            }
        }
        cache.colorsLoaded = true;
        return Map.of();
    }

    public static Image realmImage(SavegameData<Ck3Tag> data, Ck3Tag tag) {
        if (data == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        var cache = GameCacheManager.getInstance().get(Ck3CoatOfArmsCache.class);
        var cachedImg = cache.realms.get(tag);
        if (cachedImg != null) {
            return cachedImg;
        }
        CoatOfArms coa = tag.getCoatOfArms();
        var img = Ck3TagRenderer.renderRealmImage(
                coa, tag.getGovernmentName(), GameFileContext.fromData(data), REALM_DEFAULT_IMG_SIZE, true);
        cache.realms.put(tag, img);
        return img;
    }

    public static Image houseImage(Ck3House house, GameFileContext ctx) {
        if (house == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        var cache = GameCacheManager.getInstance().get(Ck3CoatOfArmsCache.class);
        var cachedImg = cache.houses.get(house);
        if (cachedImg != null) {
            return cachedImg;
        }
        var img = Ck3TagRenderer.renderHouseImage(house.getCoatOfArms(), ctx, HOUSE_DEFAULT_IMG_SIZE, true);
        cache.houses.put(house, img);
        return img;
    }

    public static Image titleImage(Ck3Title title, GameFileContext ctx) {
        if (title == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        var cache = GameCacheManager.getInstance().get(Ck3CoatOfArmsCache.class);
        var cachedImg = cache.titles.get(title);
        if (cachedImg != null) {
            return cachedImg;
        }

        CoatOfArms coa = title.getCoatOfArms();
        var img = Ck3TagRenderer.renderTitleImage(coa, ctx, TITLE_DEFAULT_IMG_SIZE, true);
        cache.titles.put(title, img);
        return img;
    }

    private boolean colorsLoaded;
    private final Map<String, javafx.scene.paint.Color> colors = new ConcurrentHashMap<>();
    private final Map<Ck3Tag, Image> realms = new ConcurrentHashMap<>();
    private final Map<Ck3Title, Image> titles = new ConcurrentHashMap<>();
    private final Map<Ck3House, Image> houses = new ConcurrentHashMap<>();

    public Ck3CoatOfArmsCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
