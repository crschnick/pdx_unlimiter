package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.GuiStyle;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GuiDialogHelper {

    public static Optional<ButtonType> showBlockingAlert(Consumer<Alert> c) {
        AtomicReference<Optional<ButtonType>> result = new AtomicReference<>();
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                Alert a = GuiDialogHelper.createAlert();
                c.accept(a);
                result.set(a.showAndWait());
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } else {
            Alert a = GuiDialogHelper.createAlert();
            c.accept(a);
            result.set(a.showAndWait());
        }
        return result.get();
    }

    public static Alert createAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        // In case settings are not loaded yet
        if (Settings.getInstance() != null) {
            alert.getDialogPane().styleProperty().setValue(
                    "-fx-font-size: " + (Settings.getInstance().fontSize.getValue() - 3) + "pt;");
        }
        setIcon(alert);
        GuiStyle.addStylesheets(alert.getDialogPane().getScene());
        return alert;
    }

    public static Alert createEmptyAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        setIcon(alert);
        GuiStyle.addStylesheets(alert.getDialogPane().getScene());
        alert.getDialogPane().styleProperty().setValue(
                "-fx-font-size: " + (Settings.getInstance().fontSize.getValue() - 2) + "pt;");
        GuiStyle.makeEmptyAlert(alert.getDialogPane().getScene());
        return alert;
    }

    public static void setIcon(Alert a) {
        if (PdxuApp.getApp() != null && PdxuApp.getApp().getIcon() != null) {
            ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(PdxuApp.getApp().getIcon());
        }
    }

    public static void showText(String title, String header, String file) {
        String text;
        try {
            text = Files.readString(PdxuInstallation.getInstance().getResourceDir().resolve(file));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }


        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);

        TextArea textArea = new TextArea();
        textArea.setText(text);
        textArea.editableProperty().setValue(false);

        ScrollPane p = new ScrollPane(textArea);
        p.setFitToWidth(true);
        p.setFitToHeight(true);
        p.setMinWidth(700);
        p.setMinHeight(500);
        alert.getDialogPane().setContent(p);

        alert.showAndWait();
    }

    public static boolean showMeltDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Melt savegame");
        alert.setHeaderText("""
                Do you want to convert the selected savegame into a non-ironman savegame using the Rakaly melter?
                """);
        alert.setContentText("""
                The original savegame will not get modified.""");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().getButtonData().isDefaultButton();

    }
}
