package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.eu5.Eu5Tag;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Eu5VassalMultiComp extends Eu5DiplomacyRowComp {

    @Override
    protected String getStyleClass() {
        return GuiStyle.CLASS_VASSAL;
    }

    @Override
    protected String getIconTooltip(SavegameData<?> data) {
        return AppI18n.get("vassals");
    }

    @Override
    protected Image getIcon() {
        return GameImage.EU5_ICON_VASSAL;
    }

    private static final Set<String> IGNORED_TYPES = Set.of("tributary", "colonial_nation", "conquistador", "trade_company");

    private static String getSubjectType(Node dependency) {
        var targets = dependency.getNodeForKeyIfExistent("named_targets");
        if (targets.isEmpty()) {
            return "";
        }

        for (var target : targets.get().getNodeArray()) {
            var flag = target.getNodeForKeyIfExistent("flag").map(Node::getString);
            if (flag.isPresent() && flag.get().equals("subject_type")) {
                return target.getNodeForKeysIfExistent("target", "object")
                        .map(Node::getString)
                        .orElse("");
            }
        }
        return "";
    }

    @Override
    protected List<Eu5Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var list = new ArrayList<Eu5Tag>();
        content.get().getNodeForKeys("diplomacy_manager").forEach((s, node) -> {
            if (s == null || !s.equals("dependency")) {
                return;
            }

            if (IGNORED_TYPES.contains(getSubjectType(node))) {
                return;
            }

            if (node.getNodeForKey("first").getLong() == data.eu5().getTag().getId()) {
                var tag = Eu5Tag.getTag(
                        data.eu5().getAllTags(), node.getNodeForKey("second").getLong());
                if (!list.contains(tag)) {
                    list.add(tag);
                }
            }
        });
        return list;
    }
}
