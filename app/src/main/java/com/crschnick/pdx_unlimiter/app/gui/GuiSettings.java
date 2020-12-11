package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.ComponentManager;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class GuiSettings {

    private static Node eu4InstallLocationNode(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("EU4 location: ");

        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
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
            s.setEu4(n.equals("") ? null : Path.of(n));
        });
        textArea.setText(s.getEu4().map(Path::toString).orElse(""));

        HBox hbox = new HBox(label, textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node hoi4InstallLocationNode(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("HOI4 location: ");

        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
        if (s.getHoi4().isPresent()) {
            b.setDisable(true);
        }
        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select HOI4 installation directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        textArea.textProperty().addListener((change, o, n) -> {
            s.setHoi4(n.equals("") ? null : Path.of(n));
        });
        textArea.setText(s.getHoi4().map(Path::toString).orElse(""));

        HBox hbox = new HBox(label, textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node ck3InstallLocationNode(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("CK3 location: ");

        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
        if (s.getCk3().isPresent()) {
            b.setDisable(true);
        }
        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select CK3 installation directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        textArea.textProperty().addListener((change, o, n) -> {
            s.setCk3(n.equals("") ? null : Path.of(n));
        });
        textArea.setText(s.getCk3().map(Path::toString).orElse(""));

        HBox hbox = new HBox(label, textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node stellarisInstallLocationNode(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("Stellaris location: ");

        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
        if (s.getStellaris().isPresent()) {
            b.setDisable(true);
        }
        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select Stellaris installation directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        textArea.textProperty().addListener((change, o, n) -> {
            s.setStellaris(n.equals("") ? null : Path.of(n));
        });
        textArea.setText(s.getStellaris().map(Path::toString).orElse(""));

        HBox hbox = new HBox(label, textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node fontSize(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("Font size: ");

        JFXSlider slider = new JFXSlider(10, 24, s.getFontSize());
        slider.valueProperty().addListener((c, o, n) -> {
            s.setFontSize(n.intValue());
        });
        HBox hbox = new HBox(label, slider);
        HBox.setHgrow(slider, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node startSteam(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("Start Steam");

        JFXCheckBox cb = new JFXCheckBox();
        cb.setSelected(s.startSteam());
        cb.selectedProperty().addListener((c, o, n) -> {
            s.setStartSteam(n);
        });
        Region spacer = new Region();
        HBox hbox = new HBox(cb, label, spacer);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node rakalyUserId(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("rakaly.com User ID:");

        TextField textArea = new TextField();

        textArea.textProperty().addListener((change, o, n) -> {
            s.setRakalyUserId(n.equals("") ? null : n);
        });
        textArea.setText(s.getRakalyUserId().orElse(""));

        HBox hbox = new HBox(label, textArea);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    private static Node rakalyApiKey(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("rakaly.com API Key:");

        TextField textArea = new TextField();

        textArea.textProperty().addListener((change, o, n) -> {
            s.setRakalyApiKey(n.equals("") ? null : n);
        });
        textArea.setText(s.getRakalyApiKey().orElse(""));

        HBox hbox = new HBox(label, textArea);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER);
        dialogPaneContent.getChildren().add(hbox);
        hbox.prefWidthProperty().bind(dialogPaneContent.widthProperty());
        return dialogPaneContent;
    }

    public static void showSettings(boolean noInstalls) {
        Alert alert = DialogHelper.createAlert();
        alert.getButtonTypes().add(ButtonType.APPLY);
        alert.setTitle("Settings");
        if (noInstalls) {
            alert.setHeaderText("A valid installation of one these games is required to use the Pdx-Unlimiter: ");
        }
        alert.getDialogPane().setMinWidth(600);

        Settings s = Settings.getInstance().copy();
        VBox vbox = new VBox(
                eu4InstallLocationNode(s),
                hoi4InstallLocationNode(s),
                ck3InstallLocationNode(s),
                stellarisInstallLocationNode(s),
                fontSize(s),
                startSteam(s),
                rakalyUserId(s),
                rakalyApiKey(s));
        vbox.setSpacing(10);
        alert.getDialogPane().setContent(vbox);

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().equals(ButtonType.APPLY)) {
            Settings.updateSettings(s);
            ComponentManager.reloadSettings();
        }
    }

}
