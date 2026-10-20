package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;

import javafx.scene.image.Image;

public class Eu4PersonalUnionsComp extends Eu4SubjectsComp {

    static boolean isPersonalUnion(String subjectType) {
        return subjectType.contains("personal_union");
    }

    @Override
    protected boolean includes(String subjectType) {
        return isPersonalUnion(subjectType);
    }

    @Override
    protected String getStyleClass() {
        return GuiStyle.CLASS_UNION;
    }

    @Override
    protected String getIconTooltip(SavegameData<?> data) {
        return AppI18n.get("personalUnions");
    }

    @Override
    protected Image getIcon() {
        return GameImage.EU4_ICON_UNION_SENIOR;
    }
}
