package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.LogManager;
import com.crschnick.pdx_unlimiter.core.format.NamespaceCreator;
import com.crschnick.pdx_unlimiter.core.savegame.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialogHelper {

    public static void showLogDialog() {
        var refresh = new ButtonType("Refresh");
        Alert alert = createAlert();
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

    public static void createNamespaceDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select non-binary save file");
        File named = fileChooser.showOpenDialog(PdxuApp.getApp().getScene().getWindow());
        if (named == null) {
            return;
        }

        fileChooser = new FileChooser();
        fileChooser.setTitle("Select binary save file");
        File unnamed = fileChooser.showOpenDialog(PdxuApp.getApp().getScene().getWindow());
        if (unnamed == null) {
            return;
        }

        RawSavegameVisitor.vist(unnamed.toPath(), new RawSavegameVisitor() {
            @Override
            public void visitEu4(Path file) {
                try {
                    Eu4RawSavegame un = Eu4RawSavegame.fromFile(unnamed.toPath());
                    Eu4RawSavegame n = Eu4RawSavegame.fromFile(named.toPath());
                    System.out.println(NamespaceCreator.createNamespace(
                            Eu4Savegame.fromSavegame(un).getNodes(), Eu4Savegame.fromSavegame(n).getNodes()));
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitHoi4(Path file) {
                try {
                    Hoi4RawSavegame un = Hoi4RawSavegame.fromFile(unnamed.toPath());
                    Hoi4RawSavegame n = Hoi4RawSavegame.fromFile(named.toPath());
                    System.out.println(NamespaceCreator.createNamespace(
                            Hoi4Savegame.fromSavegame(un).getNodes(), Hoi4Savegame.fromSavegame(n).getNodes()));
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitStellaris(Path file) {
                throw new IllegalArgumentException("No need to create namespace");
            }

            @Override
            public void visitCk3(Path file) {
                try {
                    Ck3RawSavegame un = Ck3RawSavegame.fromFile(unnamed.toPath());
                    Ck3RawSavegame n = Ck3RawSavegame.fromFile(named.toPath());
                    System.out.println(NamespaceCreator.createNamespace(
                            Ck3Savegame.fromSavegame(un).getNodes(), Ck3Savegame.fromSavegame(n).getNodes()));
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitOther(Path file) {

            }
        });
    }

    public static Alert createAlert() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        //alert.getDialogPane().styleProperty().setValue("-fx-font-size: " + Settings.getInstance().getFontSize() + "pt;");
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

    public static void setIcon(Alert a) {
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

    public static Optional<Path> showImportArchiveDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Import savegames from archive");
        alert.setHeaderText("Do you want to import all savegames from a pdxu savegame archive? This may take a while.");

        HBox dialogPaneContent = new HBox();
        Label label = new Label("Archive: ");
        label.setAlignment(Pos.BOTTOM_CENTER);
        TextField textArea = new TextField();
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
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
        HBox.setHgrow(textArea, Priority.ALWAYS);
        alert.getDialogPane().setContent(dialogPaneContent);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton() ? Optional.of(Paths.get(textArea.getText())) : Optional.empty();
    }

    public static Optional<Path> showExportDialog(boolean archive) {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Export all savegames to an archive");
        if (archive) {
            alert.setHeaderText("Do you want to export all savegames to a pdxu savegame archive?\n\n" +
                    "This allows you to import the archive " +
                    "into a Pdx-Unlimiter running on a different computer and may take a while.");
        } else {
            alert.setHeaderText("Do you want to export all savegames into an archive? This may take a while.");
        }

        HBox dialogPaneContent = new HBox();
        Label label = new Label("Archive name: ");
        label.setAlignment(Pos.BOTTOM_CENTER);
        TextField textArea = new TextField();
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
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
        HBox.setHgrow(textArea, Priority.ALWAYS);
        alert.getDialogPane().setContent(dialogPaneContent);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton() ? Optional.of(Paths.get(textArea.getText())) : Optional.empty();
    }

    public static boolean showSavegameDeleteDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Do you want to delete the selected savegame?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }

    public static boolean showCampaignDeleteDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Do you want to delete the selected campaign? This will delete all savegames of it.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.get().getButtonData().isDefaultButton();

    }
}
