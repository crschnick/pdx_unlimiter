package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.gui_utils.GuiAlertHelper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class GuiSavegameIO {

    public static Optional<Path> showExportDialog() {
        TextField textArea = new TextField();
        return GuiAlertHelper.showBlockingAlert(PdxuStyle.get(), alert -> {
            alert.setAlertType(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Export storage");
            alert.setHeaderText("""
                Do you want to export all stored savegames to a directory?
                        
                This allows you to import the savegames into a Pdx-Unlimiter running on a different computer.""");

            HBox dialogPaneContent = new HBox();
            Label label = new Label("Export location: ");
            label.setAlignment(Pos.BOTTOM_CENTER);
            textArea.setEditable(false);
            Button b = new Button();
            b.setGraphic(new FontIcon());
            b.getStyleClass().add(PdxuStyle.CLASS_BROWSE);
            b.setOnMouseClicked((m) -> {
                DirectoryChooser fileChooser = new DirectoryChooser();
                fileChooser.setTitle("Select export location");
                File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
                if (file != null) {
                    textArea.setText(file.toString());
                }
            });

            dialogPaneContent.getChildren().addAll(label, textArea, b);
            HBox.setHgrow(textArea, Priority.ALWAYS);
            alert.getDialogPane().setContent(dialogPaneContent);
        }).filter(b -> b.getButtonData().isDefaultButton() && textArea.getText().length() > 0)
                .map(b -> Path.of(textArea.getText()));
    }
}
