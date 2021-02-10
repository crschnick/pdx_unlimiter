package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.Settings;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiSettings {

    private static Region ck3toeu4LocationNode(Settings s) {
        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);

        b.setOnMouseClicked((m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select CK3 to EU4 converter directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        });

        textArea.setText(s.getCk3toEu4Dir().map(Path::toString)
                .orElse(""));
        textArea.textProperty().addListener((change, o, n) -> {
            s.setCk3toEu4Dir(Path.of(n));
        });

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

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
                    n.equals(PdxuInstallation.getInstance().getDefaultSavegamesLocation().toString()) ? null : Path.of(n));
        });
        textArea.setText(s.getStorageDirectory().map(Path::toString)
                .orElse(PdxuInstallation.getInstance().getDefaultSavegamesLocation().toString()));

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Node installLocationNode(String game, Supplier<Optional<Path>> getDir, Consumer<Path> setDir) {
        TextField textArea = new TextField();
        textArea.setEditable(false);
        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);

        EventHandler<MouseEvent> click = (m) -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select " + game + " installation directory");
            File file = fileChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
        };
        b.setOnMouseClicked(click);
        textArea.setOnMouseClicked(click);

        textArea.textProperty().addListener((change, o, n) -> {
            if (!n.equals("")) {
                setDir.accept(Path.of(n));
            }
        });
        textArea.setText(getDir.get().map(Path::toString).orElse(""));

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Node installHelpNode(String game) {
        return GuiTooltips.helpNode("The " + game + " installation directory.\n\n" +
                "Note that this should be the installation directory that contains the game executable and assets.\n" +
                "NOT the user directory that contains your savegames and mods.");
    }

    private static Node installationLocations(Settings s) {
        GridPane grid = new GridPane();

        var t = new Text("Game Installations");
        t.setStyle("-fx-font-weight: bold");
        grid.add(t, 0, 0, 2, 1);

        grid.add(installHelpNode("EU4"), 0, 1);
        grid.add(new Label("EU4:"), 1, 1);
        var eu4 = installLocationNode("EU4", s::getEu4, s::setEu4);
        grid.add(eu4, 2, 1);
        GridPane.setHgrow(eu4, Priority.ALWAYS);

        grid.add(installHelpNode("HOI4"), 0, 2);
        grid.add(new Label("HOI4:"), 1, 2);
        var hoi4 = installLocationNode("HOI4", s::getHoi4, s::setHoi4);
        grid.add(hoi4, 2, 2);
        GridPane.setHgrow(hoi4, Priority.ALWAYS);

        grid.add(installHelpNode("CK3"), 0, 3);
        grid.add(new Label("CK3:"), 1, 3);
        var ck3 = installLocationNode("CK3", s::getCk3, s::setCk3);
        grid.add(ck3, 2, 3);
        GridPane.setHgrow(ck3, Priority.ALWAYS);

        grid.add(installHelpNode("Stellaris"), 0, 4);
        grid.add(new Label("Stellaris:"), 1, 4);
        var stellaris = installLocationNode("Stellaris", s::getStellaris, s::setStellaris);
        grid.add(stellaris, 2, 4);
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


        grid.add(GuiTooltips.helpNode("""
                        The font size within the app.

                        If you have a high display resolution, you can turn this up to increase readability."""),
                0, 1);
        grid.add(new Label("Font size:"), 1, 1);
        JFXSlider slider = new JFXSlider(10, 24, s.getFontSize());
        slider.valueProperty().addListener((c, o, n) -> {
            s.setFontSize(n.intValue());
        });
        grid.add(slider, 2, 1);
        GridPane.setHgrow(slider, Priority.ALWAYS);


        grid.add(GuiTooltips.helpNode("""
                        Specifies whether to automatically update the Pdx-Unlimiter when launching it."""),
                0, 2);
        grid.add(new Label("Enable auto update:"), 1, 2);
        JFXCheckBox eau = new JFXCheckBox();
        eau.setSelected(s.enableAutoUpdate());
        eau.selectedProperty().addListener((c, o, n) -> {
            s.setEnableAutoUpdate(n);
        });
        grid.add(eau, 2, 2);
        GridPane.setHgrow(eau, Priority.ALWAYS);


        grid.add(GuiTooltips.helpNode("""
                        Specifies whether to ask for confirmation when deleting a campaign or savegame."""),
                0, 3);
        grid.add(new Label("Confirm deletion:"), 1, 3);
        JFXCheckBox cd = new JFXCheckBox();
        cd.setSelected(s.confirmDeletion());
        cd.selectedProperty().addListener((c, o, n) -> {
            s.setConfirmDeletion(n);
        });
        grid.add(cd, 2, 3);
        GridPane.setHgrow(cd, Priority.ALWAYS);


        grid.add(GuiTooltips.helpNode("""
                        Specifies whether to start Steam when launching a game through the Pdx-Unlimiter.

                        If you disable this, Steam might not register your achievements while playing."""),
                0, 4);
        grid.add(new Label("Start steam:"), 1, 4);
        JFXCheckBox cb = new JFXCheckBox();
        cb.setSelected(s.startSteam());
        cb.selectedProperty().addListener((c, o, n) -> {
            s.setStartSteam(n);
        });
        grid.add(cb, 2, 4);
        GridPane.setHgrow(cb, Priority.ALWAYS);


        grid.add(GuiTooltips.helpNode("""
                Specifies whether to delete savegames after succesfully importing it into the Pdx-Unlimiter storage.
                              
                Recommended to keep this disabled while the Pdx-Unlimiter is in beta."""), 0, 5);
        grid.add(new Label("Delete on import:"), 1, 5);
        JFXCheckBox doi = new JFXCheckBox();
        doi.setSelected(s.deleteOnImport());
        doi.selectedProperty().addListener((c, o, n) -> {
            s.setDeleteOnImport(n);
        });
        grid.add(doi, 2, 5);
        GridPane.setHgrow(doi, Priority.ALWAYS);


        grid.add(GuiTooltips.helpNode("""
                The directory where the Pdx-Unlimiter stores all imported savegames.
                """), 0, 6);
        grid.add(new Label("Storage directory:"), 1, 6);
        Node loc = storageLocationNode(s);
        grid.add(loc, 2, 6);
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
        grid.add(name, 0, 0, 3, 1);

        grid.add(GuiTooltips.helpNode("""
                Your Rakaly.com User ID.

                You can find this by going to the 'Account' page on Rakaly.com.
                """), 0, 1);
        grid.add(new Label("User ID:"), 1, 1);
        TextField userId = new TextField();
        userId.textProperty().addListener((change, o, n) -> {
            s.setRakalyUserId(n.equals("") ? null : n);
        });
        userId.setText(s.getRakalyUserId().orElse(""));
        grid.add(userId, 2, 1);
        GridPane.setHgrow(userId, Priority.ALWAYS);

        grid.add(GuiTooltips.helpNode("""
                Your Rakaly.com API key.

                You can generate an API key by going to the 'Account' page on Rakaly.com.
                """), 0, 2);
        grid.add(new Label("API key:"), 1, 2);
        TextField apikey = new TextField();
        apikey.textProperty().addListener((change, o, n) -> {
            s.setRakalyApiKey(n.equals("") ? null : n);
        });
        apikey.setText(s.getRakalyApiKey().orElse(""));
        grid.add(apikey, 2, 2);
        GridPane.setHgrow(apikey, Priority.ALWAYS);
        grid.setHgap(10);
        grid.setVgap(10);

        return grid;
    }

    private static Node skanderbeg(Settings s) {
        GridPane grid = new GridPane();

        var t = new Text("Skanderbeg.pm");
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 0, 0, 3, 1);

        grid.add(GuiTooltips.helpNode("""
                Your Skanderbeg.pm API key.

                You can find this by going to the 'API' page on Skanderbeg.pm.
                """), 0, 1);
        grid.add(new Label("API key:"), 1, 1);
        TextField apiKey = new TextField();
        apiKey.textProperty().addListener((change, o, n) -> {
            s.setSkanderbegApiKey(n.equals("") ? null : n);
        });
        apiKey.setText(s.getSkanderbegApiKey().orElse(""));
        grid.add(apiKey, 2, 1);
        GridPane.setHgrow(apiKey, Priority.ALWAYS);

        grid.setHgap(10);
        grid.setVgap(10);

        return grid;
    }

    private static Node converters(Settings s) {
        GridPane grid = new GridPane();

        var t = new Text("Paradox Converters");
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 0, 0, 3, 1);

        grid.add(GuiTooltips.helpNode("""
                The path to the CK3ToEU4 converter.
                """), 0, 1);
        grid.add(new Label("CK3 to EU4 converter location:"), 1, 1);
        var lc = ck3toeu4LocationNode(s);
        grid.add(lc, 2, 1);
        GridPane.setHgrow(lc, Priority.ALWAYS);

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
                rakaly(s),
                new Separator(),
                skanderbeg(s),
                new Separator(),
                converters(s));
        vbox.setSpacing(10);
        alert.getDialogPane().setContent(vbox);

        Optional<ButtonType> r = alert.showAndWait();
        if (r.isPresent() && r.get().equals(ButtonType.APPLY)) {
            ComponentManager.reloadSettings(s);
        }
    }

}
