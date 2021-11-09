package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.Eu4TagRenderer;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.lang.GameLocalisation;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public abstract class Eu4DiplomacyRowComp extends DiplomacyRowComp<Eu4Tag> {

    @Override
    protected Region map(SavegameData<?> data, Eu4Tag tag) {
        return GameImage.imageNode(Eu4TagRenderer.smallShieldImage(info, tag), CLASS_TAG_ICON);
    }

    @Override
    protected String mapTooltip(SavegameData<?> data, Eu4Tag tag) {
        var ctx = GameFileContext.fromData(data);
        return GameLocalisation.getLocalisedValue(tag.getTag(), ctx);
    }

    @Override
    protected final void init(ArrayNode node, SavegameData<?> data) {
        this.tags = getTags(node, data);
    }

    protected abstract List<Eu4Tag> getTags(ArrayNode node, SavegameData<?> data);
}
