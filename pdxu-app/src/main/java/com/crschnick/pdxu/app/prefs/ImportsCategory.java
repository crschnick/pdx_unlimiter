package com.crschnick.pdxu.app.prefs;

import atlantafx.base.controls.ProgressSliderSkin;
import atlantafx.base.theme.Styles;
import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.IntFieldComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ImportsCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "imports";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi-import");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("paradoxConverters")
                .sub(new OptionsBuilder()
                        .pref(prefs.playSoundOnBackgroundImport)
                        .addToggle(prefs.playSoundOnBackgroundImport)
                        .pref(prefs.enableTimedImports)
                        .addToggle(prefs.enableTimedImports)
                        .pref(prefs.timedImportsInterval)
                        .addComp(
                                Comp.of(() -> {
                                            var field = new IntFieldComp(prefs.timedImportsInterval).disable(new ReadOnlyBooleanWrapper(true)).maxWidth(40);
                                            field.apply(struc -> struc.get().setOpacity(1.0));
                                            field.apply(struc -> struc.get().setAlignment(Pos.CENTER));
                                            field.padding(new Insets(4, 8, 4, 8));

                                            var s = new Slider(1, 60, prefs.timedImportsInterval.getValue());
                                            s.getStyleClass().add(Styles.SMALL);
                                            s.valueProperty().addListener((ov, oldv, newv) -> {
                                                prefs.timedImportsInterval.setValue(newv.intValue());
                                            });
                                            s.setSkin(new ProgressSliderSkin(s));
                                            HBox.setHgrow(s, Priority.ALWAYS);

                                            var hbox = new HBox(field.createRegion(), s);
                                            hbox.setAlignment(Pos.CENTER);
                                            hbox.setSpacing(8);
                                            return hbox;
                                        })
                                        .maxWidth(600))
                        .pref(prefs.importOnNormalGameExit)
                        .addToggle(prefs.importOnNormalGameExit)
                        .pref(prefs.deleteOnImport)
                        .addToggle(prefs.deleteOnImport)
                );
        return builder.buildComp();
    }
}
