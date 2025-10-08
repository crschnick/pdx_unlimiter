package com.crschnick.pdxu.app.platform;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.LabelComp;
import com.crschnick.pdxu.app.comp.base.OptionsComp;
import com.crschnick.pdxu.app.comp.base.ToggleSwitchComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.prefs.AppPrefs;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;

import atlantafx.base.controls.Spacer;

import java.util.ArrayList;
import java.util.List;

public class OptionsBuilder {

    private final List<OptionsComp.Entry> entries = new ArrayList<>();
    private final List<Property<?>> props = new ArrayList<>();

    private ObservableValue<String> name;
    private ObservableValue<String> description;
    private Comp<?> comp;
    private Comp<?> lastCompHeadReference;
    private ObservableValue<String> lastNameReference;

    private void finishCurrent() {
        if (comp == null) {
            return;
        }

        var entry = new OptionsComp.Entry(null, description, name, comp);
        description = null;
        name = null;
        lastNameReference = null;
        comp = null;
        lastCompHeadReference = null;
        entries.add(entry);
    }

    public OptionsBuilder sub(OptionsBuilder builder) {
        props.addAll(builder.props);
        var c = builder.lastCompHeadReference;
        var n = builder.lastNameReference;
        pushComp(builder.buildComp());
        if (c != null) {
            lastCompHeadReference = c;
        }
        if (n != null) {
            lastNameReference = n;
        }
        return this;
    }

    public OptionsBuilder addTitle(String titleKey) {
        finishCurrent();
        entries.add(new OptionsComp.Entry(
                titleKey, null, null, new LabelComp(AppI18n.observable(titleKey)).styleClass("title-header")));
        return this;
    }

    public OptionsBuilder pref(Object property) {
        var mapping = AppPrefs.get().getMapping(property);
        pref(mapping.getKey(), mapping.isRequiresRestart());
        return this;
    }

    public OptionsBuilder pref(String key, boolean requiresRestart) {
        name(key);
        if (requiresRestart) {
            description(AppI18n.observable(key + "Description").map(s -> s + "\n\n" + AppI18n.get("requiresRestart")));
        } else {
            description(AppI18n.observable(key + "Description"));
        }
        return this;
    }

    public OptionsBuilder hide(ObservableValue<Boolean> b) {
        lastCompHeadReference.hide(b);
        return this;
    }

    private void pushComp(Comp<?> comp) {
        finishCurrent();
        this.comp = comp;
        this.lastCompHeadReference = comp;
    }

    public OptionsBuilder addToggle(Property<Boolean> prop) {
        var comp = new ToggleSwitchComp(prop, null, null);
        pushComp(comp);
        props.add(prop);
        return this;
    }

    public OptionsBuilder spacer(double size) {
        return addComp(Comp.of(() -> new Spacer(size, Orientation.VERTICAL)));
    }

    public OptionsBuilder fixedName(String s) {
        finishCurrent();
        name = new ReadOnlyStringWrapper(s);
        lastNameReference = name;
        return this;
    }

    public OptionsBuilder fixedDescription(String s) {
        finishCurrent();
        description = new ReadOnlyStringWrapper(s);
        return this;
    }

    public OptionsBuilder nameAndDescription(String key) {
        return name(key).description(key + "Description");
    }

    public OptionsBuilder name(String nameKey) {
        finishCurrent();
        name = AppI18n.observable(nameKey);
        lastNameReference = name;
        return this;
    }

    public OptionsBuilder description(String descriptionKey) {
        finishCurrent();
        description = AppI18n.observable(descriptionKey);
        return this;
    }

    public OptionsBuilder description(ObservableValue<String> description) {
        finishCurrent();
        this.description = description;
        return this;
    }

    public OptionsBuilder addComp(Comp<?> comp) {
        pushComp(comp);
        return this;
    }

    public OptionsBuilder addComp(Comp<?> comp, Property<?> prop) {
        pushComp(comp);
        props.add(prop);
        return this;
    }

    public OptionsComp buildComp() {
        finishCurrent();
        return new OptionsComp(entries);
    }
}
