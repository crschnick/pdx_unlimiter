package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.PathChoiceComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;

public class IronyCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "ironyModManager";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2a-auto-mode");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("ironyModManager")
                .sub(new OptionsBuilder()
                        .pref(prefs.ironyDirectory)
                        .addComp(new PathChoiceComp(prefs.ironyDirectory, "ironyDirectory", true).maxWidth(600))
                        .pref(prefs.launchIrony)
                        .addToggle(prefs.launchIrony)
                );
        return builder.buildComp();
    }
}
