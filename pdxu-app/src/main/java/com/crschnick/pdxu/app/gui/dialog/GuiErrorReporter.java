package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.util.Hyperlinks;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuiErrorReporter {

    public static void showReportSent() {
        Alert a = GuiDialogHelper.createAlert();
        a.initModality(Modality.WINDOW_MODAL);
        a.setAlertType(Alert.AlertType.CONFIRMATION);
        a.setTitle("Report sent");
        a.setHeaderText("Your report has been successfully sent! Thank you");
        a.show();
    }

    public static boolean showException(Throwable e, boolean terminal) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        boolean r = showErrorMessage(e.getMessage(), stackTrace, true, terminal);
        if (r) {
            showReportSent();
        }
        return r;
    }

    public static boolean showSimpleErrorMessage(String msg) {
        return showErrorMessage(msg, null, false, false);
    }

    public static boolean showErrorMessage(String msg, String details, boolean reportable, boolean terminal) {
        AtomicBoolean shouldSend = new AtomicBoolean(false);
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                shouldSend.set(showErrorMessageInternal(msg, details, reportable, terminal));
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } else {
            shouldSend.set(showErrorMessageInternal(msg, details, reportable, terminal));
        }
        return shouldSend.get();
    }

    private static boolean showErrorMessageInternal(String msg, String details, boolean reportable, boolean terminal) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        // Create Alert without icon since it may not have loaded yet
        if (PdxuApp.getApp() != null && PdxuApp.getApp().getIcon() != null) {
            GuiDialogHelper.setIcon(alert);
        }

        alert.getButtonTypes().clear();
        if (reportable) {
            ButtonType autoReport = new ButtonType("Report automatically", ButtonBar.ButtonData.OK_DONE);
            ButtonType reportOnGithub = new ButtonType("Report on github", ButtonBar.ButtonData.APPLY);
            ButtonType reportOnDiscord = new ButtonType("Get help on Discord", ButtonBar.ButtonData.APPLY);
            alert.getButtonTypes().addAll(reportOnDiscord, reportOnGithub, autoReport);

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


                You can notify the developers of this error automatically by clicking the 'Report automatically' button. (This will send some diagnostics data.)
                Alternatively, you can also report it on GitHub to provide some information about the issue and get notified about the status of your reported issue.

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

        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE);
    }

    public static Optional<String> showIssueDialog() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        GuiDialogHelper.setIcon(alert);

        alert.getButtonTypes().clear();
        ButtonType report = new ButtonType("Send", ButtonBar.ButtonData.APPLY);
        alert.getButtonTypes().addAll(report);
        alert.setTitle("Issue reporter");
        alert.setHeaderText("""
                If you encountered an issue, please describe it here.

                By clicking 'Send', you send this report and additional log information to the developers.
                If you want to get notified of a fix or help the devs in case of any questions,
                please include some sort of contact information, like a reddit/discord/github username or an email
                                """);

        VBox dialogPaneContent = new VBox();
        TextArea textArea = new TextArea();
        textArea.autosize();
        dialogPaneContent.getChildren().addAll(textArea);
        alert.getDialogPane().setContent(dialogPaneContent);

        Optional<ButtonType> r = alert.showAndWait();
        r.ifPresent(b -> showReportSent());
        return r.isPresent() && r.get().getButtonData().equals(ButtonBar.ButtonData.APPLY) ?
                Optional.ofNullable(textArea.getText()) : Optional.empty();
    }
}
