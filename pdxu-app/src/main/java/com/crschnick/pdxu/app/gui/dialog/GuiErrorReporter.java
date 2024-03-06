package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.util.Hyperlinks;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;

public class GuiErrorReporter {

    public static void showException(Throwable e, boolean terminal) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        showErrorMessage(e.getMessage(), stackTrace, true, terminal);
    }

    public static void showSimpleErrorMessage(String msg) {
        showErrorMessage(msg, null, false, false);
    }

    public static void showErrorMessage(String msg, String details, boolean reportable, boolean terminal) {
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                showErrorMessageInternal(msg, details, reportable, terminal);
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } else {
            showErrorMessageInternal(msg, details, reportable, terminal);
        }
    }

    private static void showErrorMessageInternal(String msg, String details, boolean reportable, boolean terminal) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        // Create Alert without icon since it may not have loaded yet
        if (PdxuApp.getApp() != null && PdxuApp.getApp().getIcon() != null) {
            GuiDialogHelper.setIcon(alert);
        }

        alert.getButtonTypes().clear();
        if (reportable) {
            ButtonType reportOnGithub = new ButtonType("Report on github", ButtonBar.ButtonData.APPLY);
            ButtonType reportOnDiscord = new ButtonType("Get help on Discord", ButtonBar.ButtonData.APPLY);
            alert.getButtonTypes().addAll(reportOnDiscord, reportOnGithub);

            alert.getDialogPane().lookupButton(reportOnGithub).addEventFilter(ActionEvent.ACTION, event -> {
                Hyperlinks.open(Hyperlinks.NEW_ISSUE);
                event.consume();
            });
            alert.getDialogPane().lookupButton(reportOnDiscord).addEventFilter(ActionEvent.ACTION, event -> {
                Hyperlinks.open(Hyperlinks.DISCORD);
                event.consume();
            });
        }
        ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().add(ok);

        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Pdx-Unlimiter");
        alert.setHeaderText((msg != null ? msg.substring(0, Math.min(msg.length(), 1000)) : "An error occured") + (reportable ? """

                You can report the issue on GitHub or Discord to provide some information about the issue and get notified about the status of your reported issue.

                """ + (!terminal ? "Note that this error is not terminal and you can continue using the Pdx-Unlimiter.\n" +
                "However, if something is no longer working correctly, you should try to restart the Pdx-Unlimiter." :
                "This error means that the Pdx-Unlimiter can not operate anymore without solving the underlying problem.\n" +
                        "This is done to protect the state of your savegame storage.") : ""));

        VBox dialogPaneContent = new VBox();

        if (details != null) {
            javafx.scene.control.Label label = new javafx.scene.control.Label("Details:");

            javafx.scene.control.TextArea textArea = new TextArea();
            textArea.setText(details);
            textArea.editableProperty().setValue(false);
            textArea.autosize();
            dialogPaneContent.getChildren().addAll(label, textArea);
        }

        alert.getDialogPane().setContent(dialogPaneContent);

        alert.getDialogPane().setMaxWidth(800);

        alert.showAndWait();
    }
}
