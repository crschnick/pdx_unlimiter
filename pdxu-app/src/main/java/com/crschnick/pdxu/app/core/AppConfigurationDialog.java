package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.comp.base.ScrollComp;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import com.crschnick.pdxu.app.prefs.AppearanceCategory;
import com.crschnick.pdxu.app.util.Hyperlinks;

import javafx.scene.layout.Region;

public class AppConfigurationDialog {

    public static void showIfNeeded() {
        if (!AppProperties.get().isInitialLaunch()) {
            return;
        }

        var options = new OptionsBuilder()
                .sub(AppearanceCategory.languageChoice())
                .sub(AppearanceCategory.themeChoice())
                .buildComp();
        options.styleClass("initial-setup");
        options.styleClass("prefs-container");

        var scroll = new ScrollComp(options);
        scroll.apply(struc -> {
            struc.get().prefHeightProperty().bind(((Region) struc.get().getContent()).heightProperty());
        });
        scroll.minWidth(650);
        scroll.prefWidth(650);

        var modal = ModalOverlay.of("initialSetup", scroll);
        modal.addButton(new ModalButton(
                "docs",
                () -> {
                    Hyperlinks.open(Hyperlinks.DOCS);
                },
                false,
                false));
        modal.addButton(ModalButton.ok());
        AppDialog.show(modal);
    }
}
