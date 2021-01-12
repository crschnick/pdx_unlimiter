package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.ComponentManager;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.jfoenix.controls.JFXRadioButton;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.crschnick.pdx_unlimiter.app.gui.DialogHelper.createAlert;

public class GuiConverterConfig {


    public static boolean showConfirmConversionDialog() {
        Alert alert = createAlert();

        var config = new ButtonType("Configure");
        alert.getButtonTypes().add(config);

        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm conversion");
        alert.setHeaderText("""
                Do you want to convert the selected CK3 savegame to an EU4 mod?
                """);
        alert.setContentText("""
                This conversion may take a while.
                """);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().equals(config);
    }

    public static void showUsageDialog() {
        Alert alert = createAlert();

        var download = new ButtonType("Show downloads");
        alert.getButtonTypes().add(download);
        Button val = (Button) alert.getDialogPane().lookupButton(download);
        val.setOnAction(e -> {
            ThreadHelper.browse("https://github.com/ParadoxGameConverters/CK3toEU4/releases");
        });

        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("CK3 to EU4 converter");
        alert.setHeaderText("""
                To use the converter functionality, you first have to download the CK3toEU4 converter,
                extract it, and then set the path to the extracted directory in the settings menu.
                """);
        alert.showAndWait();
    }

    public static void showConversionSuccessDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Conversion succeeded");
        alert.setHeaderText("""
                The Converter has finished successfully.
                """);
        alert.setContentText("""
                The created mod has been added to your mod directory.
                To play it, simply enable the mod your EU4 the launcher.
                """);
        alert.showAndWait();
    }

    public static void showConversionErrorDialog() {
        Alert alert = createAlert();
        var openLog = new ButtonType("Open log");
        alert.getButtonTypes().add(openLog);
        alert.setAlertType(Alert.AlertType.ERROR);
        alert.setTitle("Conversion failed");
        alert.setHeaderText("""
                The converter returned an error.
                """);
        alert.setContentText("""
                If you want to diagnose the error, you can take a look at the log file of the converter.
                """);
        Button val = (Button) alert.getDialogPane().lookupButton(openLog);
        val.setOnAction(e -> {
            ThreadHelper.open(Settings.getInstance().getCk3toEu4Dir().get().resolve("CK3toEU4").resolve("log.txt"));
        });
        alert.showAndWait();
    }



    private static javafx.scene.Node help(String text) {
        Label q = new Label(" ? ");
        q.setStyle("-fx-border-color: black;");
        var t = new Tooltip(text);
        t.setShowDelay(Duration.ZERO);
        t.setShowDuration(Duration.INDEFINITE);
        q.setTooltip(t);
        return q;
    }

    private static javafx.scene.Node createOptionNode(
            Node n,
            Map<String,String> translations,
            Map<String,String> values) {
        GridPane grid = new GridPane();
        if (n.getNodeForKeyIfExistent("radioSelector").isEmpty()) {
            return grid;
        }

        grid.add(help(translations.get(n.getNodeForKey("tooltip").getString())), 0, 0);

        var t = new Text(translations.get(n.getNodeForKey("displayName").getString()));
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 1, 0, 2, 1);

        ToggleGroup tg = new ToggleGroup();
        int row = 1;
        String oName = n.getNodeForKey("name").getString();
        for (var ro : n.getNodeForKey("radioSelector").getNodesForKey("radioOption")) {
            String roValue = ro.getNodeForKey("name").getString();
            var btn = new JFXRadioButton(translations.get(ro.getNodeForKey("displayName").getString()));
            btn.setToggleGroup(tg);
            btn.selectedProperty().addListener((c, o, ne) -> {
                values.put(oName, roValue);
            });
            if (values.get(oName) != null && values.get(oName).equals(roValue)) {
                btn.setSelected(true);
            }
            if (values.get(oName) == null && ro.getNodeForKey("default").getString().equals("true")) {
                btn.setSelected(true);
            }

            grid.add(help(translations.get(ro.getNodeForKeyIfExistent("tooltip")
                    .map(Node::getString).orElse(""))), 0, row);
            grid.add(btn, 1, row);
            row++;
        }

        grid.setHgap(10);
        grid.setVgap(10);

        return grid;
    }

    public static boolean showConfig(Map<String,String> values) {
        Node configNode;
        Map<String,String> translations;
        try {
            configNode = TextFormatParser.textFileParser().parse(Files.readAllBytes(
                    Settings.getInstance().getCk3toEu4Dir().get()
                            .resolve("Configuration").resolve("fronter-options.txt")));
            translations = LocalisationHelper.loadTranslations(Settings.getInstance().getCk3toEu4Dir().get()
                    .resolve("Configuration").resolve("options.yml"), LocalisationHelper.Language.ENGLISH);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return false;
        }

        Alert alert = createAlert();
        alert.getButtonTypes().add(ButtonType.APPLY);
        alert.getButtonTypes().add(ButtonType.CANCEL);
        alert.setTitle("Converter settings");
        alert.initModality(Modality.NONE);
        alert.getDialogPane().setMinWidth(600);

        VBox options = new VBox();
        for (var node : configNode.getNodesForKey("option")) {
            options.getChildren().add(createOptionNode(node, translations, values));
            options.getChildren().add(new Separator());
        }
        options.setSpacing(10);

        var sp = new ScrollPane(options);
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(500);
        alert.getDialogPane().setContent(sp);

        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get().equals(ButtonType.APPLY);
    }
}
