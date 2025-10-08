package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.savegame.SavegameNotes;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GuiSavegameNotes {

    public static void showSavegameNotesDialog(SavegameNotes notes) {
        var modal = ModalOverlay.of("savegameNotes", Comp.of(() -> {
            VBox dialogPaneContent = new VBox();

            TextArea textArea = new TextArea();
            textArea.textProperty().bindBidirectional(notes.textProperty());
            textArea.autosize();
            dialogPaneContent.getChildren().add(textArea);

            CheckBox cb = new CheckBox();
            cb.selectedProperty().bindBidirectional(notes.remindMeProperty());
            Label l = new Label();
            l.textProperty().bind(AppI18n.observable("savegameNotesRemind"));
            HBox hbox = new HBox(cb, l);
            hbox.setSpacing(8);
            dialogPaneContent.getChildren().add(hbox);
            dialogPaneContent.setSpacing(10);
            return dialogPaneContent;
        }).prefWidth(700));
        modal.addButton(ModalButton.ok());
        modal.show();
    }

    public static void showSavegameNotesReminderDialog(SavegameNotes notes) {
        if (!notes.remindMeProperty().get()) {
            return;
        }

        var modal = ModalOverlay.of("savegameNotes", AppDialog.dialogText(notes.textProperty().get()));
        modal.addButton(new ModalButton("dontRemind", () -> {
            notes.remindMeProperty().set(false);
        }, true, false));
        modal.addButton(new ModalButton("launch", null, true, true));
        modal.showAndWait();
    }
}
