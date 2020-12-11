package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.ComponentManager;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class GuiSettings {

    private static Region storageLocationNode(Settings s) {
        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);

        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select savegame storage directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        textArea.textProperty().addListener((change, o, n) -> {
            s.setStorageDirectory(
                    n.equals(PdxuInstallation.getInstance().getDefaultSavegameLocation().toString()) ? null : Path.of(n));
        });
        textArea.setText(s.getStorageDirectory().map(Path::toString)
                .orElse(PdxuInstallation.getInstance().getDefaultSavegameLocation().toString()));

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Region eu4InstallLocationNode(Settings s) {
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

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Node hoi4InstallLocationNode(Settings s) {
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

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Node ck3InstallLocationNode(Settings s) {
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

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Node stellarisInstallLocationNode(Settings s) {
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

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Node installationLocations(Settings s) {
        GridPane grid = new GridPane();

        var t = new Text("Installations");
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 0, 0, 2, 1);

        grid.add(new Label("EU4:"), 0, 1);
        var eu4 = eu4InstallLocationNode(s);
        grid.add(eu4, 1, 1);
        GridPane.setHgrow(eu4, Priority.ALWAYS);

        grid.add(new Label("HOI4:"), 0, 2);
        var hoi4 = hoi4InstallLocationNode(s);
        grid.add(hoi4, 1, 2);
        GridPane.setHgrow(hoi4, Priority.ALWAYS);

        grid.add(new Label("CK3:"), 0, 3);
        var ck3 = ck3InstallLocationNode(s);
        grid.add(ck3, 1, 3);
        GridPane.setHgrow(ck3, Priority.ALWAYS);

        grid.add(new Label("Stellaris:"), 0, 4);
        var stellaris = stellarisInstallLocationNode(s);
        grid.add(stellaris, 1, 4);
        GridPane.setHgrow(stellaris, Priority.ALWAYS);

        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }

    private static Node misc(Settings s) {
        GridPane grid = new GridPane();

        var t = new Text("Miscellaneous");
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 0, 0, 2, 1);

        grid.add(new Label("Font size:"), 0, 1);
        JFXSlider slider = new JFXSlider(10, 24, s.getFontSize());
        slider.valueProperty().addListener((c, o, n) -> {
            s.setFontSize(n.intValue());
        });
        grid.add(slider, 1, 1);
        GridPane.setHgrow(slider, Priority.ALWAYS);

        grid.add(new Label("Start steam:"), 0, 2);
        JFXCheckBox cb = new JFXCheckBox();
        cb.setSelected(s.startSteam());
        cb.selectedProperty().addListener((c, o, n) -> {
            s.setStartSteam(n);
        });
        grid.add(cb, 1, 2);
        GridPane.setHgrow(cb, Priority.ALWAYS);

        grid.add(new Label("Storage directory:"), 0, 3);
        Node loc = storageLocationNode(s);
        grid.add(loc, 1, 3);
        GridPane.setHgrow(loc, Priority.ALWAYS);

        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }

    private static Node rakaly(Settings s) {
        GridPane grid = new GridPane();

        var t = new Text("Rakaly.com");
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 0, 0, 2, 1);

        grid.add(new Label("User ID:"), 0, 1);
        TextField textArea = new TextField();
        textArea.textProperty().addListener((change, o, n) -> {
            s.setRakalyUserId(n.equals("") ? null : n);
        });
        textArea.setText(s.getRakalyUserId().orElse(""));
        grid.add(textArea, 1, 1);

        grid.add(new Label("API key:"), 0, 2);
        TextField apikey = new TextField();
        apikey.textProperty().addListener((change, o, n) -> {
            s.setRakalyApiKey(n.equals("") ? null : n);
        });
        apikey.setText(s.getRakalyApiKey().orElse(""));
        grid.add(apikey, 1, 2);
        grid.setHgap(10);
        grid.setVgap(10);

        return grid;
    }

    public static void showSettings() {
        Alert alert = DialogHelper.createAlert();
        alert.getButtonTypes().add(ButtonType.APPLY);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.setTitle("Settings");
        alert.getDialogPane().setMinWidth(600);

        Settings s = Settings.getInstance().copy();
        VBox vbox = new VBox(
                installationLocations(s),
                new Separator(),
                misc(s),
                new Separator(),
                rakaly(s));
        vbox.setSpacing(10);
        alert.getDialogPane().setContent(vbox);

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().equals(ButtonType.APPLY)) {
            Settings.updateSettings(s);
            ComponentManager.reloadSettings();
        }
    }

}
