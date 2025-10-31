package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.Eu5CoatOfArmsCache;
import com.crschnick.pdxu.app.gui.game.Eu5TagRenderer;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.eu5.Eu5Tag;
import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public abstract class Eu5DiplomacyRowComp extends DiplomacyRowComp<Eu5Tag> {

    @Override
    protected Region mapTag(SavegameData<?> data, Eu5Tag tag) {
        return GameImage.imageNode(
                Eu5CoatOfArmsCache.tagFlag(data.eu5(), tag),
                CLASS_TAG_ICON);
    }

    @Override
    protected String mapTagTooltip(SavegameData<?> data, Eu5Tag tag) {
        return GameLocalisation.getLocalisedValue(tag.getNameTag(), data);
    }

    @Override
    protected final void init(SavegameContent content, SavegameData<?> data) {
        this.tags = getTags(content, data);
    }

    protected abstract List<Eu5Tag> getTags(SavegameContent content, SavegameData<?> data);
}
