package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ChoiceComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;

public class SystemCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "system";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2d-desktop-classic");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("system")
                .sub(new OptionsBuilder()
                        .pref(prefs.startupBehaviour)
                        .addComp(ChoiceComp.ofTranslatable(
                                        prefs.startupBehaviour,
                                        PrefsChoiceValue.getSupported(StartupBehaviour.class),
                                        false)
                                .maxWidth(300.0))
                        .pref(prefs.closeBehaviour)
                        .addComp(ChoiceComp.ofTranslatable(
                                        prefs.closeBehaviour,
                                        PrefsChoiceValue.getSupported(CloseBehaviour.class),
                                        false)
                                .maxWidth(300.0)));
        return builder.buildComp();
    }
}
