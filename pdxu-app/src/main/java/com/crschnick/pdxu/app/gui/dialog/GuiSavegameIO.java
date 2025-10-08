package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.core.AppCache;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppSideWindow;
import com.crschnick.pdxu.app.gui.GuiStyle;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class GuiSavegameIO {

    public static Optional<Path> showExportDialog() {
        Alert alert = AppSideWindow.createEmptyAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle(AppI18n.get("exportStorage"));
        alert.setHeaderText(AppI18n.get("exportStorageinfo"));

        HBox dialogPaneContent = new HBox();
        dialogPaneContent.setAlignment(Pos.CENTER);
        Label label = new Label("Export location: ");
        label.setAlignment(Pos.BOTTOM_CENTER);

        TextField textArea = new TextField();
        textArea.setEditable(false);
        Path prev = AppCache.getNonNull("storageExportPath", Path.class, () -> null);
        if (prev != null && Files.exists(prev)) {
            textArea.setText(prev.toString());
        }

        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
        b.setOnMouseClicked((m) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            if (prev != null && Files.exists(prev)) {
                directoryChooser.setInitialDirectory(prev.toFile());
            }

            directoryChooser.setTitle("Select export location");
            File file = directoryChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null) {
                textArea.setText(file.toString());
                AppCache.update("storageExportPath", file.toPath());
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
