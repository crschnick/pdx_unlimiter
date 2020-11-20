package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.LogManager;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.format.NamespaceCreator;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DialogHelper {

    public static void showLogDialog() {
        var refresh = new ButtonType("Refresh");
        Alert alert = createAlert();
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.getButtonTypes().add(refresh);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle("Log");


        TextArea textArea = new TextArea();
        textArea.editableProperty().setValue(false);

        Button val = (Button) alert.getDialogPane().lookupButton(refresh);
        val.addEventFilter(
                ActionEvent.ACTION,
                e -> {
                    if (LogManager.getInstance().getLogFile().isPresent()) {
                        try {
                            textArea.setText(Files.readString(LogManager.getInstance().getLogFile().get()));
                            e.consume();
                        } catch (IOException ex) {
                            ErrorHandler.handleException(ex);
                        }
                    }
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

    public static void createNamespaceDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select non-ironman save");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EU4 save game", "*.eu4"));
        File named = fileChooser.showOpenDialog(PdxuApp.getApp().getScene().getWindow());
        if (named == null) {
            return;
        }

        fileChooser = new FileChooser();
        fileChooser.setTitle("Select ironman save");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EU4 save game", "*.eu4"));
        File unnamed = fileChooser.showOpenDialog(PdxuApp.getApp().getScene().getWindow());
        if (unnamed == null) {
            return;
        }

        try {
            Eu4Savegame un = Eu4Savegame.fromFile(unnamed.toPath());
            Eu4Savegame n = Eu4Savegame.fromFile(named.toPath());
            System.out.println(NamespaceCreator.createNamespace(
                    Eu4IntermediateSavegame.fromSavegame(un), Eu4IntermediateSavegame.fromSavegame(n)));
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    public static Alert createAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        setIcon(alert);
        GuiStyle.addStylesheets(alert.getDialogPane().getScene());
        return alert;
    }

    public static Alert createEmptyAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        setIcon(alert);
        GuiStyle.addStylesheets(alert.getDialogPane().getScene());
        GuiStyle.makeEmptyAlert(alert.getDialogPane().getScene());
        return alert;
    }

    private static void setIcon(Alert a) {
        ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(PdxuApp.getApp().getIcon());
    }

    public static void showText(String title, String header, String file) {
        String text = null;
        try {
            text = new String(DialogHelper.class.getResourceAsStream(file).readAllBytes());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }


        Alert alert = createAlert();
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

    public static boolean showException(Exception e) {
        ButtonType foo = new ButtonType("Send error", ButtonBar.ButtonData.OK_DONE);
        ButtonType bar = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = createAlert();
        alert.getButtonTypes().addAll(foo, bar);
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An exception occured. If you want to notify the developers of this error, click the 'send error' button. ");

        VBox dialogPaneContent = new VBox();

        Label label = new Label("Stack Trace:");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);
        textArea.editableProperty().setValue(false);

        dialogPaneContent.getChildren().addAll(label, textArea);

        alert.getDialogPane().setContent(dialogPaneContent);

        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get().getButtonData().isDefaultButton();
    }

    public static boolean showInitialSettings() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        setIcon(alert);
        alert.setTitle("Specify EU4 install location");
        alert.setHeaderText("A valid EU4 installation is required to run the savegame manager. However, no EU4 installation has been found.");
        alert.getDialogPane().setMinWidth(500);

        Settings s = Settings.getInstance().copy();
        alert.getDialogPane().setContent(installLocationNode(s));

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().getButtonData().isDefaultButton()) {
            Settings.updateSettings(s);
        }

        return GameInstallation.EU4 != null;
    }

    private static Node installLocationNode(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("EU4 location: ");

        TextField textArea = new TextField();
        textArea.setEditable(false);
        textArea.setMinWidth(500);
        Button b = new Button("\uD83D\uDCBE");
        if (s.getEu4().isPresent()) {
            b.setDisable(true);
        }
        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select EU4 installation directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        textArea.textProperty().addListener((change, o, n) -> {
            s.setEu4(Optional.ofNullable(n.equals("") ? null : Path.of(n)));
        });
        textArea.setText(s.getEu4().map(Path::toString).orElse(""));

        dialogPaneContent.getChildren().addAll(label, textArea, b);
        return dialogPaneContent;
    }

    public static void showSettings() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.getButtonTypes().add(ButtonType.OK);
        alert.setTitle("Settings");
        alert.getDialogPane().setMinWidth(500);

        Settings s = Settings.getInstance().copy();
        alert.getDialogPane().setContent(installLocationNode(s));

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().getButtonData().isDefaultButton()) {
            Settings.updateSettings(s);
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
            File file = fileChooser.showOpenDialog(((Node) m.getTarget()).getScene().getWindow());
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
            File file = fileChooser.showSaveDialog(((Node) m.getTarget()).getScene().getWindow());
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
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Import all savegames");
        alert.setContentText("Do you want to import all savegames from " + GameInstallation.EU4.getSaveDirectory().toString() + "? This may take a while.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();
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
