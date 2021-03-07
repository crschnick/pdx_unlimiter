package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GuiSettings {

    public static void showSettings() {
        Alert alert = DialogHelper.createAlert();
        alert.getButtonTypes().add(ButtonType.APPLY);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.setTitle("Settings");
        alert.getDialogPane().setMinWidth(600);

        Settings s = Settings.getInstance();
        Set<Runnable> applyFuncs = new HashSet<>();
        VBox vbox = new VBox(
                GuiSettingsComponents.section("GAME_DIRS", applyFuncs, s.eu4, s.ck3, s.hoi4, s.stellaris),
                new Separator());
        vbox.setSpacing(10);
        alert.getDialogPane().setContent(vbox);

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().equals(ButtonType.APPLY)) {
            ComponentManager.reloadSettings(() -> applyFuncs.forEach(ru -> ru.run()));
        }
    }
}
