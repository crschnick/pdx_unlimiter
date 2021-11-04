package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.core.CacheManager;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class Ck3Backgrounds {

    public static Color getBackgroundColor(SavegameInfo<Ck3Tag> info) {
        if (!info.getData().hasOnePlayerTag()) {
            return ColorHelper.withAlpha(Color.BEIGE, 0.33);
        }

        var cache = CacheManager.getInstance().get(BackgroundColorCache.class);
        if (cache.colors.size() == 0) {
            var file = GameInstallation.ALL.get(Game.CK3).getInstallDir().resolve("game")
                    .resolve("common").resolve("landed_titles").resolve("00_landed_titles.txt");

            try {
                ArrayNode node = TextFormatParser.text().parse(file);
                cache.addColors(node);
            } catch (Exception e) {
                ErrorHandler.handleException(e, "Couldn't parse title data", file);
            }
        }

        var key = info.getData().getTag().getPrimaryTitle().getKey();
        var color = cache.colors.containsKey(key) ? ColorHelper.fromGameColor(cache.colors.get(key)) : Color.TRANSPARENT;
        return ColorHelper.withAlpha(color, 0.33);
    }

    public static class BackgroundColorCache extends CacheManager.Cache {

        private final Map<String, GameColor> colors = new HashMap<>();

        public BackgroundColorCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }

        public void addColors(Node node) {
            node.forEach((k, v) -> {
                if (v.isArray() && v.hasKey("color")) {
                    if (v.getNodeForKey("color").isTagged()) {
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
}
