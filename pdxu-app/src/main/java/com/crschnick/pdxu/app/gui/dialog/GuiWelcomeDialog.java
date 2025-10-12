package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.comp.base.MarkdownComp;
import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.AppResources;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public class GuiWelcomeDialog {

    public static void showAndWaitIfNeeded() {
        if (AppProperties.get().isDevelopmentEnvironment()
                || AppProperties.get().isAotTrainMode()
                || !AppProperties.get().isInitialLaunch()) {
            return;
        }

        var md = new AtomicReference<String>();
        AppResources.with(AppResources.MAIN_MODULE, "misc/welcome.md", file -> {
            md.set(Files.readString(file));
        });
        var modal =
                ModalOverlay.of("welcome", new MarkdownComp(md.get(), UnaryOperator.identity(), true).prefWidth(750));
        modal.addButton(ModalButton.ok());
        modal.showAndWait();
    }
}
