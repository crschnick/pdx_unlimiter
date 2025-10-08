package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.comp.base.ButtonComp;
import com.crschnick.pdxu.app.comp.base.VerticalComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppRestart;
import com.crschnick.pdxu.app.platform.PlatformThread;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;

import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AppPrefsSidebarComp extends SimpleComp {

    @Override
    protected Region createSimple() {
        var effectiveCategories = AppPrefs.get().getCategories().stream()
                .filter(appPrefsCategory -> appPrefsCategory.show())
                .toList();
        var buttons = effectiveCategories.stream()
                .<Comp<?>>map(appPrefsCategory -> {
                    return new ButtonComp(
                                    AppI18n.observable(appPrefsCategory.getId()),
                                    new ReadOnlyObjectWrapper<>(appPrefsCategory.getIcon()),
                                    () -> {
                                        AppPrefs.get().getSelectedCategory().setValue(appPrefsCategory);
                                    })
                            .apply(struc -> {
                                struc.get().setGraphicTextGap(9);
                                struc.get().setTextAlignment(TextAlignment.LEFT);
                                struc.get().setAlignment(Pos.CENTER_LEFT);
                                AppPrefs.get().getSelectedCategory().subscribe(val -> {
                                    struc.get()
                                            .pseudoClassStateChanged(
                                                    PseudoClass.getPseudoClass("selected"),
                                                    appPrefsCategory.equals(val));
                                });
                            })
                            .maxWidth(2000);
                })
                .collect(Collectors.toCollection(ArrayList::new));

        var restartButton = new ButtonComp(AppI18n.observable("restartApp"), new FontIcon("mdi2r-restart"), () -> {
            AppRestart.restart();
        });
        restartButton.maxWidth(2000);
        restartButton.visible(AppPrefs.get().getRequiresRestart());
        restartButton.padding(new Insets(6, 10, 6, 6));
        buttons.add(Comp.vspacer());
        buttons.add(restartButton);

        var vbox = new VerticalComp(buttons).styleClass("sidebar");
        vbox.apply(struc -> {
            AppPrefs.get().getSelectedCategory().subscribe(val -> {
                PlatformThread.runLaterIfNeeded(() -> {
                    var index = val != null ? effectiveCategories.indexOf(val) : 0;
                    if (index >= struc.get().getChildren().size()) {
                        return;
                    }

                    ((Button) struc.get().getChildren().get(index)).fire();
                });
            });
        });
        return vbox.createRegion();
    }
}
