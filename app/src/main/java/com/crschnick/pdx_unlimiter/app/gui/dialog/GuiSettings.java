package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.core.settings.SettingsEntry;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
