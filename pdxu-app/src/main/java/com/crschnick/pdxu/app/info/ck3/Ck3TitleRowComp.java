package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.gui.game.Ck3CoatOfArmsCache;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Title;

import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public abstract class Ck3TitleRowComp extends DiplomacyRowComp<Ck3Title> {

    public static String getTitleName(SavegameData<?> data, Ck3Title title) {
        return title.getName().orElseGet(() -> GameLocalisation.getLocalisedValue(title.getKey(), data));
    }

    @Override
    protected Region mapTag(SavegameData<?> data, Ck3Title tag) {
        return GameImage.imageNode(Ck3CoatOfArmsCache.titleImage(tag, GameFileContext.fromData(data)), CLASS_TAG_ICON);
    }

    @Override
    protected String mapTagTooltip(SavegameData<?> data, Ck3Title tag) {
        return getTitleName(data, tag);
    }

    @Override
    protected final void init(SavegameContent content, SavegameData<?> data) {
        this.tags = getTitles(data);
    }

    protected abstract List<Ck3Title> getTitles(SavegameData<?> data);
}
