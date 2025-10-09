package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.MarkdownComp;
import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.AppCache;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.AppResources;
import com.crschnick.pdxu.app.core.window.AppDialog;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public class GuiWelcomeDialog {

    public static void showAndWaitIfNeeded() {
        if (AppProperties.get().isInitialLaunch()) {
            return;
        }


        var md = new AtomicReference<String>();
        AppResources.with(AppResources.MAIN_MODULE, "misc/welcome.md", file -> {
            md.set(Files.readString(file));
        });
        var modal = ModalOverlay.of("welcome", new MarkdownComp(md.get(), UnaryOperator.identity(), true).prefWidth(750));
        modal.addButton(ModalButton.ok());
        modal.showAndWait();
    }
}
