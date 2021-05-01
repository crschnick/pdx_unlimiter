package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.info.GameColor;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class Ck3Backgrounds {

    public static class BackgroundColorCache extends CacheManager.Cache {

        private final Map<String, GameColor> colors = new HashMap<>();

        public BackgroundColorCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }

        public void addColors(Node node) {
            node.forEach((k, v) -> {
                if (v.isArray() && v.hasKey("color")) {
                    if (v.getNodeForKey("color").isColor()) {
                        colors.put(k, GameColor.fromColorNode(v.getNodeForKey("color")));
                    } else {
                        colors.put(k, GameColor.fromRgbArray(v.getNodeForKey("color")));
                    }
                }

                if (v.isArray()) {
                    addColors(v);
                }
            });
        }
    }

    public static Color getBackgroundColor(SavegameInfo<Ck3Tag> info) {
        if (!info.hasOnePlayerTag()) {
            return ColorHelper.withAlpha(Color.BEIGE, 0.33);
        }

        var cache = CacheManager.getInstance().get(BackgroundColorCache.class);
        if (cache.colors.size() == 0) {
            var file = GameInstallation.ALL.get(Game.CK3).getInstallDir().resolve("game")
                    .resolve("common").resolve("landed_titles").resolve("00_landed_titles.txt");

            try {
                ArrayNode node = TextFormatParser.textFileParser().parse(file);
                cache.addColors(node);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }

        var key = info.getTag().getPrimaryTitle().getKey();
        var color = cache.colors.containsKey(key) ? ColorHelper.fromGameColor(cache.colors.get(key)) : Color.TRANSPARENT;
        return ColorHelper.withAlpha(color, 0.33);
    }
}
