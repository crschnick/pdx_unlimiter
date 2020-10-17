package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import io.sentry.Sentry;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class DialogHelper {

    public static boolean showException(Exception e) {
        ButtonType foo = new ButtonType("Send error", ButtonBar.ButtonData.OK_DONE);
        ButtonType bar = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.ERROR, "", foo, bar);
        alert.setTitle("Error alert");
        alert.setHeaderText("An exception occured");

        VBox dialogPaneContent = new VBox();

        Label label = new Label("Stack Trace:");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);
        textArea.editableProperty().setValue(false);
        textArea.setMinWidth(1000);
        textArea.setMinHeight(800);

        dialogPaneContent.getChildren().addAll(label, textArea);

        alert.getDialogPane().setContent(dialogPaneContent);

        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get().getButtonData().isDefaultButton();
    }

    public static void showSettings() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.getButtonTypes().add(ButtonType.OK);
        alert.setTitle("Settings");
        alert.getDialogPane().setMinWidth(500);

        HBox dialogPaneContent = new HBox();

        Label label = new Label("EU4 location: ");

        TextField textArea = new TextField();
        textArea.setMinWidth(500);
        Button b = new Button("\uD83D\uDCBE");
        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select EU4 installation directory");
            File file = fileChooser.showDialog(((Node)m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });
        textArea.textProperty().addListener((change, o, n) -> {
            Eu4Installation i = new Eu4Installation(Paths.get(textArea.getText()));
            if (!i.isValid()) {
                textArea.setStyle("-fx-border-color: #aa7777; -fx-border-width: 3px;");
            } else {
                textArea.setStyle("-fx-border-color: #77aa77; -fx-border-width: 3px;");
            }
        });
        textArea.setText(Installation.EU4.isPresent() ? Installation.EU4.get().getPath().toString() : "");

        dialogPaneContent.getChildren().addAll(label, textArea, b);
        alert.getDialogPane().setContent(dialogPaneContent);
        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().getButtonData().isDefaultButton()) {
            Installation.EU4 = Optional.of(new Eu4Installation(Paths.get(textArea.getText())));
            try {
                Installation.initInstallations();
                Installation.saveConfig();
            } catch (Exception e) {
                ErrorHandler.handleException(e, false);
            }
        }
    }

    public static Optional<Path> showImportArchiveDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Import all savegames from archive");
        alert.setContentText("Do you want to import all savegames from a pdxu savegame archive? This may take a while.");

        HBox dialogPaneContent = new HBox();
        Label label = new Label("Archive: ");
        label.setAlignment(Pos.BOTTOM_CENTER);
        TextField textArea = new TextField();
        textArea.setMinWidth(500);
        Button b = new Button("\uD83D\uDCBE");
        b.setOnMouseClicked((m) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select archive to import");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip archive", "*.zip"));
            File file = fileChooser.showOpenDialog(((Node)m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        dialogPaneContent.getChildren().addAll(label, textArea, b);
        alert.getDialogPane().setContent(dialogPaneContent);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton() ? Optional.of(Paths.get(textArea.getText())) : Optional.empty();
    }

    public static Optional<Path> showExportDialog(boolean archive) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (archive) {
            alert.setHeaderText("Export all savegames to archive");
            alert.setContentText("Do you want to export all savegames to a pdxu savegame archive? This may take a while.");
        } else {

        }

        HBox dialogPaneContent = new HBox();
        Label label = new Label("Export to file: ");
        label.setAlignment(Pos.BOTTOM_CENTER);
        TextField textArea = new TextField();
        textArea.setMinWidth(500);
        Button b = new Button("\uD83D\uDCBE");
        b.setOnMouseClicked((m) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select export location");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip archive", "*.zip"));
            File file = fileChooser.showSaveDialog(((Node)m.getTarget()).getScene().getWindow());
            if (file != null) {
                textArea.setText(file.toString());
            }
        });

        dialogPaneContent.getChildren().addAll(label, textArea, b);
        alert.getDialogPane().setContent(dialogPaneContent);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton() ? Optional.of(Paths.get(textArea.getText())) : Optional.empty();
    }

    public static boolean showImportSavegamesDialog() {
        if (!Installation.EU4.isPresent()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Valid EU4 installation needed");
            alert.showAndWait();
            return false;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Import all savegames");
            alert.setContentText("Do you want to import all savegames from " + Installation.EU4.get().getSaveDirectory().toString() + "? This may take a while.");
            Optional<ButtonType> result = alert.showAndWait();
            return result.get().getButtonData().isDefaultButton();
        }
    }

    public static boolean showSavegameDeleteDialog() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Do you want to delete the selected savegame?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }

    public static boolean showUpdateAllSavegamesDialog() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm update");
        alert.setHeaderText("Do you want to update all savegames? This may take a while.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }

    public static boolean showCampaignDeleteDialog() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Do you want to delete the selected campaign? This will delete all savegames of it.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }
}
