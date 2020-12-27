package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuiErrorReporter {

    public static boolean showException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return showErrorMessage(e.getMessage(), stackTrace, true);
    }

    public static boolean showErrorMessage(String msg, String details, boolean reportable) {
        AtomicBoolean shouldSend = new AtomicBoolean(false);
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                shouldSend.set(showErrorMessageInternal(msg, details, reportable));
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } else {
            shouldSend.set(showErrorMessageInternal(msg, details, reportable));
        }
        return shouldSend.get();
    }

    private static boolean showErrorMessageInternal(String msg, String details, boolean reportable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        // Create Alert without icon since it may not have loaded yet
        if (PdxuApp.getApp() != null && PdxuApp.getApp().getIcon() != null) {
            DialogHelper.setIcon(alert);
        }
        alert.getButtonTypes().clear();

        ButtonType bar = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().add(bar);

        if (reportable) {
            ButtonType foo = new ButtonType("Report automatically", ButtonBar.ButtonData.OK_DONE);
            ButtonType report = new ButtonType("Report on github", ButtonBar.ButtonData.APPLY);
            alert.getButtonTypes().addAll(report, foo);

            Button reportButton = (Button) alert.getDialogPane().lookupButton(report);
            reportButton.addEventFilter(ActionEvent.ACTION, event -> {
                ThreadHelper.browse("https://github.com/crschnick/pdx_unlimiter/issues/new");
                event.consume();
            });
        }

        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error reporter");
        alert.setHeaderText((msg != null ? msg : "An error occured") + (reportable ?
                """

                        If you have a suspicion of the cause and want to help us fix the error, you can report it on github.

                        Alternatively you can notify the developers of this error automatically by clicking the 'Report automatically' button.
                        This will send some diagnostics data""" : ""));

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

        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE);
    }
}
