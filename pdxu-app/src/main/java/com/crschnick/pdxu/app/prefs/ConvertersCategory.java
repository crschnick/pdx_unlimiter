package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.PathChoiceComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;

public class ConvertersCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "paradoxConverters";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2f-file-arrow-left-right-outline");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("paradoxConverters")
                .sub(new OptionsBuilder()
                        .pref(prefs.ck3toeu4Directory)
                        .addComp(new PathChoiceComp(prefs.ck3toeu4Directory, "ck3toeu4Directory", true).maxWidth(600))
                        .pref(prefs.eu4tovic3Directory)
                        .addComp(new PathChoiceComp(prefs.eu4tovic3Directory, "eu4tovic3Directory", true).maxWidth(600))
                        .pref(prefs.vic3tohoi4Directory)
                        .addComp(new PathChoiceComp(prefs.vic3tohoi4Directory, "vic3tohoi4Directory", true).maxWidth(600))
                );
        return builder.buildComp();
    }
}
