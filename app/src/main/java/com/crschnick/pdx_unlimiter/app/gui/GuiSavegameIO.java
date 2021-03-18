package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiDialogHelper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class GuiSavegameIO {

    public static Optional<Path> showExportDialog() {
        Alert alert = GuiDialogHelper.createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export storage");
        alert.setHeaderText("""
                Do you want to export all stored savegames to a directory?
                        
                This allows you to import the savegames into a Pdx-Unlimiter running on a different computer.""");

        HBox dialogPaneContent = new HBox();
        Label label = new Label("Export location: ");
        label.setAlignment(Pos.BOTTOM_CENTER);
        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
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
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton() && textArea.getText().length() > 0 ?
                Optional.of(Paths.get(textArea.getText())) : Optional.empty();
    }
}
