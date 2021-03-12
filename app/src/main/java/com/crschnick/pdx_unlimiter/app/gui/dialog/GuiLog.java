package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.LogManager;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Modality;

import java.io.IOException;
import java.nio.file.Files;

public class GuiLog {

    public static void showLogDialog() {
        Alert alert = GuiDialogHelper.createAlert();
        var refresh = new ButtonType("Refresh");
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

}
