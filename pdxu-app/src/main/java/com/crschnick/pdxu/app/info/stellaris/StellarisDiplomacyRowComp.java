package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.gui.game.StellarisTagRenderer;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public abstract class StellarisDiplomacyRowComp extends DiplomacyRowComp<StellarisTag> {

    @Override
    protected Region map(SavegameData<?> data, StellarisTag tag) {
        return GameImage.imageNode(StellarisTagRenderer.createTagImage(GameFileContext.fromData(data), tag), CLASS_TAG_ICON);
    }

    @Override
    protected String mapTooltip(SavegameData<?> data, StellarisTag tag) {
        return tag.getName();
    }

    @Override
    protected final void init(SavegameContent content, SavegameData<?> data) {
        this.tags = getTags(content, data);
    }

    protected abstract List<StellarisTag> getTags(SavegameContent content, SavegameData<?> data);
}
