package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;

import javafx.scene.image.Image;

public class Eu4VassalsComp extends Eu4SubjectsComp {

    @Override
    protected boolean includes(String subjectType) {
        return !Eu4PersonalUnionsComp.isPersonalUnion(subjectType)
                && !subjectType.contains("colony")
                && !subjectType.contains("tributary")
                && !subjectType.equals("private_enterprise");
    }

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
        return GameImage.EU4_ICON_VASSAL;
    }
}
