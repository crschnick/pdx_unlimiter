package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.platform.LabelGraphic;

public abstract class AppPrefsCategory {

    public boolean show() {
        return true;
    }

    public abstract String getId();

    protected abstract LabelGraphic getIcon();

    public abstract Comp<?> create();
}
