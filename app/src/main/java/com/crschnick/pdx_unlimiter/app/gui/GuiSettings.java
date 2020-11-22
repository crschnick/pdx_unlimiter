package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.Settings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class GuiSettings {

    private static Node eu4InstallLocationNode(Settings s) {
        HBox dialogPaneContent = new HBox();

        Label label = new Label("EU4 location: ");

        TextField textArea = new TextField();
        textArea.setEditable(false);
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
        Button b = new Button("\uD83D\uDCBE");
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
        Button b = new Button("\uD83D\uDCBE");
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
        Button b = new Button("\uD83D\uDCBE");
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

    public static void showSettings() {
        Alert alert = DialogHelper.createAlert();
        alert.getButtonTypes().add(new ButtonType("Apply", ButtonBar.ButtonData.APPLY));
        alert.setTitle("Settings");
        alert.getDialogPane().setMinWidth(600);

        Settings s = Settings.getInstance().copy();
        VBox vbox = new VBox(eu4InstallLocationNode(s), hoi4InstallLocationNode(s), ck3InstallLocationNode(s), stellarisInstallLocationNode(s));
        vbox.setSpacing(10);
        alert.getDialogPane().setContent(vbox);

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().getButtonData().isDefaultButton()) {
            Settings.updateSettings(s);
        }
    }

}
