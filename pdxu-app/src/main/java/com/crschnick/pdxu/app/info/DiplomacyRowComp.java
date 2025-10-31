package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.gui.game.TagRows;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_DIPLOMACY_ROW;
import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_IMAGE_ICON;

public abstract class DiplomacyRowComp<T> extends SavegameInfoComp {

    protected List<T> tags;

    protected abstract Region mapTag(SavegameData<?> data, T tag);

    protected abstract String mapTagTooltip(SavegameData<?> data, T tag);

    protected abstract String getStyleClass();

    protected abstract String getIconTooltip(SavegameData<?> data);

    protected abstract Image getIcon();

    @Override
    public Region create(SavegameData<?> data) {
        if (tags.size() == 0) {
            return null;
        }

        var imgView = GameImage.imageNode(getIcon(), CLASS_IMAGE_ICON);
        var row =
                TagRows.createTagRow(imgView, getIconTooltip(data), tags, (T t) -> mapTagTooltip(data, t), (T t) -> mapTag(data, t));
        row.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        row.getStyleClass().add(getStyleClass());
        return row;
    }
}
