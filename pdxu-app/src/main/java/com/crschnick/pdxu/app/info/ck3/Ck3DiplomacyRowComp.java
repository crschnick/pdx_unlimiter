package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public abstract class Ck3DiplomacyRowComp extends DiplomacyRowComp<Ck3Tag> {

    @Override
    protected Region map(SavegameData<?> data, Ck3Tag tag) {
        return GameImage.imageNode(
                Ck3TagRenderer.renderRealmImage(tag.getCoatOfArms(), tag.getGovernmentName(), GameFileContext.fromData(data), 64, false),
                CLASS_TAG_ICON
        );
    }

    @Override
    protected String mapTooltip(SavegameData<?> data, Ck3Tag tag) {
        return tag.getName();
    }

    @Override
    protected final void init(SavegameContent content, SavegameData<?> data) {
        this.tags = getTags(content, data);
    }

    protected abstract List<Ck3Tag> getTags(SavegameContent content, SavegameData<?> data);
}
