package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.core.ComponentManager;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.util.integration.Eu4SeHelper;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GuiSettings {

    public static void showSettings() {
        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.getButtonTypes().add(ButtonType.APPLY);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.setTitle(PdxuI18n.get("SETTINGS"));

        Settings s = Settings.getInstance();
        Set<Runnable> applyFuncs = new HashSet<>();
        VBox vbox = new VBox(
                GuiSettingsComponents.section("GAME_DIRS", applyFuncs, s.vic3, s.eu4, s.ck3, s.hoi4, s.stellaris, s.ck2, s.vic2),
                new Separator(),
                GuiSettingsComponents.section("GENERAL", applyFuncs, s.enableAutoUpdate, s.storageDirectory),
                new Separator(),
                GuiSettingsComponents.section("IMPORTS", applyFuncs,
                        s.importOnGameNormalExit,
                        s.enabledTimedImports,
                        s.timedImportsInterval,
                        s.playSoundOnBackgroundImport,
                        s.deleteOnImport),
                new Separator(),
                GuiSettingsComponents.section("INTERFACE", applyFuncs,
                        s.fontSize,
                        s.maxTooltipWidth,
                        s.confirmDeletion,
                        s.useGameLanguage,
                        s.language),
                new Separator(),
                GuiSettingsComponents.section("IRONY", applyFuncs, s.ironyDir, s.launchIrony),
                new Separator(),
                GuiSettingsComponents.section("CONVERTERS", applyFuncs, s.ck3toeu4Dir),
                new Separator(),
                GuiSettingsComponents.section("SKANDERBEG", applyFuncs, s.skanderbegApiKey));
        if (Eu4SeHelper.isSupported()) {
            vbox.getChildren().add(new Separator());
            vbox.getChildren().add(GuiSettingsComponents.section("EU4SE", applyFuncs, s.enableEu4SaveEditor));
        }
        vbox.setSpacing(10);
        var sp = new ScrollPane(vbox);
        sp.setFitToWidth(true);
        alert.getDialogPane().setContent(sp);
        sp.setPrefWidth(650);
        sp.setPrefHeight(600);
        vbox.getStyleClass().add("settings-content");
        sp.getStyleClass().add("settings-container");

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().equals(ButtonType.APPLY)) {
            ComponentManager.reloadSettings(() -> applyFuncs.forEach(ru -> ru.run()));
        }
    }
}
