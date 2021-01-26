package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuiErrorReporter {

    public static boolean showException(Throwable e, boolean terminal) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        boolean r = showErrorMessage(e.getMessage(), stackTrace, true, terminal);
        if (r) {
            DialogHelper.showReportSent();
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
                        This will send some diagnostics data.
                        """ + (!terminal ? "\n Note that this error is not terminal and you can continue using the Pdx-Unlimiter." : "") : ""));

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

    public static Optional<String> showIssueDialog() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        DialogHelper.setIcon(alert);

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
        r.ifPresent(b -> DialogHelper.showReportSent());
        return r.isPresent() && r.get().getButtonData().equals(ButtonBar.ButtonData.APPLY) ?
                Optional.ofNullable(textArea.getText()) : Optional.empty();
    }

    public static boolean showRakalyTokenDialog() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        DialogHelper.setIcon(alert);

        alert.getButtonTypes().clear();

        ButtonType ok = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().add(ok);

        ButtonType send = new ButtonType("Send savegame", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().add(send);

        alert.setTitle("Rakaly reporter");
        alert.setHeaderText("""
                During parsing of the ironman savegame, an unknown token has been found.

                To help the developers of Rakaly, the Ironman converter for Paradox games,
                to improve the quality of converted savegames, you can automatically send them
                the savegame file to analyze.
                            """);

        Optional<ButtonType> r = alert.showAndWait();
        boolean sent = r.isPresent() && r.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE);
        if (sent) {
            DialogHelper.showReportSent();
        }
        return sent;
    }
}
