package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameNotes;
import com.jfoenix.controls.JFXCheckBox;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GuiSavegameNotes {

    public static void showSavegameNotesDialog(SavegameNotes notes) {
        GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.NONE);
            alert.getButtonTypes().add(ButtonType.OK);
            alert.setTitle("Savegame notes");

            VBox dialogPaneContent = new VBox();

            TextArea textArea = new TextArea();
            textArea.textProperty().bindBidirectional(notes.textProperty());
            textArea.autosize();
            dialogPaneContent.getChildren().add(textArea);

            JFXCheckBox cb = new JFXCheckBox();
            cb.selectedProperty().bindBidirectional(notes.remindMeProperty());
            Label l = new Label("Remind me when launching the savegame");
            dialogPaneContent.getChildren().add(new HBox(cb, l));

            alert.getDialogPane().setContent(dialogPaneContent);
            alert.getDialogPane().getStyleClass().add("savegame-notes");
        });
    }

    public static void showSavegameNotesReminderDialog(SavegameNotes notes) {
        if (!notes.remindMeProperty().get()) {
            return;
        }

        ButtonType ignoreLater = new ButtonType("Don't remind me again", ButtonBar.ButtonData.APPLY);
        var r = GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.NONE);
            alert.getButtonTypes().add(ButtonType.OK);
            alert.setTitle("Savegame notes");
            alert.setHeaderText(notes.textProperty().get());

            alert.getButtonTypes().add(ignoreLater);
        });
        r.ifPresent(t -> {
            if (t.equals(ignoreLater)) {
                notes.remindMeProperty().set(false);
            }
        });
    }
}
