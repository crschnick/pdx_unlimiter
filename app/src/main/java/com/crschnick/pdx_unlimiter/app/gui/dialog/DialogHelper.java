package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.LogManager;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class DialogHelper {

    public static Optional<ButtonType> waitForResult(Alert alert) {
        final Optional<ButtonType>[] result = new Optional[]{Optional.empty()};
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                result[0] = alert.showAndWait();
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } else {
            result[0] = alert.showAndWait();
        }
        return result[0];
    }

    public static void showLogDialog() {
        var refresh = new ButtonType("Refresh");
        Alert alert = createAlert();
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.getButtonTypes().add(refresh);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle("Show log");


        TextArea textArea = new TextArea(LogManager.getInstance().getLogFile().isPresent() ?
                "" : "Log file output is currently disabled!");
        textArea.editableProperty().setValue(false);

        Button val = (Button) alert.getDialogPane().lookupButton(refresh);
        val.addEventFilter(
                ActionEvent.ACTION,
                e -> {
                    if (LogManager.getInstance().getLogFile().isPresent()) {
                        try {
                            textArea.setText(Files.readString(LogManager.getInstance().getLogFile().get()));
                        } catch (IOException ex) {
                            ErrorHandler.handleException(ex);
                        }
                    }
                    e.consume();
                }
        );
        val.fireEvent(new ActionEvent());

        ScrollPane p = new ScrollPane(textArea);
        p.setFitToWidth(true);
        p.setFitToHeight(true);
        p.setMinWidth(700);
        p.setMinHeight(500);
        alert.getDialogPane().setContent(p);

        alert.showAndWait();
    }

    public static Alert createAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        // In case settings are not loaded yet
        if (Settings.getInstance() != null) {
            alert.getDialogPane().styleProperty().setValue(
                    "-fx-font-size: " + (Settings.getInstance().fontSize.getValue() - 2) + "pt;");
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
        if (PdxuApp.getApp() != null) {
            ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(PdxuApp.getApp().getIcon());
        }
    }

    public static void showText(String title, String header, String file) {
        String text = null;
        try {
            text = new String(GuiLayout.class.getResourceAsStream(file).readAllBytes());
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

    public static boolean showSavegameDeleteDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Do you want to delete the selected savegame?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }

    public static boolean showCampaignDeleteDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Do you want to delete the selected campaign? This will delete all savegames of it.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }
}
