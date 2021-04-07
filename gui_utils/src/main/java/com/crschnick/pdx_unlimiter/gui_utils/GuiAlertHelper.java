package com.crschnick.pdx_unlimiter.gui_utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class GuiAlertHelper {

    public static Optional<ButtonType> showBlockingAlert(GuiStyle style, Consumer<Alert> c) {
        AtomicReference<Optional<ButtonType>> result = new AtomicReference<>();
        if (!Platform.isFxApplicationThread()) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                Alert a = GuiAlertHelper.createAlert(style);
                c.accept(a);
                result.set(a.showAndWait());
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } else {
            Alert a = GuiAlertHelper.createAlert(style);
            c.accept(a);
            result.set(a.showAndWait());
        }
        return result.get();
    }

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

    public static Alert createAlert(GuiStyle style) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        style.applyToAlert(alert);
        return alert;
    }

    public static void showText(GuiStyle style, String title, String header, String text) {
        Alert alert = createAlert(style);
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
}
