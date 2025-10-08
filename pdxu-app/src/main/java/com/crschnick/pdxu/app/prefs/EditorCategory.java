package com.crschnick.pdxu.app.prefs;

import atlantafx.base.controls.ProgressSliderSkin;
import atlantafx.base.theme.Styles;
import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ChoiceComp;
import com.crschnick.pdxu.app.comp.base.IntFieldComp;
import com.crschnick.pdxu.app.comp.base.TextFieldComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Arrays;

public class EditorCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "editor";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2c-comment-edit-outline");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("editor")
                .sub(new OptionsBuilder()
                        .pref(prefs.editorExternalProgram)
                        .addComp(new TextFieldComp(prefs.editorExternalProgram).maxWidth(600))
                        .pref(prefs.editorIndentation)
                        .addComp(ChoiceComp.ofTranslatable(prefs.editorIndentation, Arrays.asList(EditorIndentation.values()), false).maxWidth(600))
                        .pref(prefs.editorWarnOnNodeTypeChange)
                        .addToggle(prefs.editorWarnOnNodeTypeChange)
                        .pref(prefs.editorMaxTooltipLines)
                        .addComp(
                                Comp.of(() -> {
                                            var field = new IntFieldComp(prefs.editorMaxTooltipLines).disable(new ReadOnlyBooleanWrapper(true)).maxWidth(40);
                                            field.apply(struc -> struc.get().setOpacity(1.0));
                                            field.apply(struc -> struc.get().setAlignment(Pos.CENTER));
                                            field.padding(new Insets(4, 8, 4, 8));

                                            var s = new Slider(1, 60, prefs.editorMaxTooltipLines.getValue());
                                            s.getStyleClass().add(Styles.SMALL);
                                            s.valueProperty().addListener((ov, oldv, newv) -> {
                                                prefs.editorMaxTooltipLines.setValue(newv.intValue());
                                            });
                                            s.setSkin(new ProgressSliderSkin(s));
                                            HBox.setHgrow(s, Priority.ALWAYS);

                                            var hbox = new HBox(field.createRegion(), s);
                                            hbox.setAlignment(Pos.CENTER);
                                            hbox.setSpacing(8);
                                            return hbox;
                                        })
                                        .maxWidth(600))
                        .pref(prefs.editorEnableNodeTags)
                        .addToggle(prefs.editorEnableNodeTags)
                        .pref(prefs.editorEnableNodeJumps)
                        .addToggle(prefs.editorEnableNodeJumps)
                        .pref(prefs.editorPageSize)
                        .addComp(
                                Comp.of(() -> {
                                            var field = new IntFieldComp(prefs.editorPageSize).disable(new ReadOnlyBooleanWrapper(true)).maxWidth(40);
                                            field.apply(struc -> struc.get().setOpacity(1.0));
                                            field.apply(struc -> struc.get().setAlignment(Pos.CENTER));
                                            field.padding(new Insets(4, 8, 4, 8));

                                            var s = new Slider(1, 60, prefs.editorPageSize.getValue());
                                            s.getStyleClass().add(Styles.SMALL);
                                            s.valueProperty().addListener((ov, oldv, newv) -> {
                                                prefs.editorPageSize.setValue(newv.intValue());
                                            });
                                            s.setSkin(new ProgressSliderSkin(s));
                                            HBox.setHgrow(s, Priority.ALWAYS);

                                            var hbox = new HBox(field.createRegion(), s);
                                            hbox.setAlignment(Pos.CENTER);
                                            hbox.setSpacing(8);
                                            return hbox;
                                        })
                                        .maxWidth(600))
                );
        return builder.buildComp();
    }
}
