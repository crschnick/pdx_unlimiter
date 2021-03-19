package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.core.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.core.settings.SettingsEntry;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class GuiSettingsComponents {

    private static Region pathEntryNode(SettingsEntry<Path> de, Set<Runnable> applyFuncs) {
        TextField textArea = new TextField();
        EventHandler<MouseEvent> eh = (m) -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            if (!textArea.getText().isEmpty()) {
                Path p = Path.of(textArea.getText());
                dirChooser.setInitialDirectory(p.toFile());
            }
            dirChooser.setTitle(PdxuI18n.get("SELECT_DIR", de.getName()));
            File file = dirChooser.showDialog(((Node) m.getTarget()).getScene().getWindow());
            if (file != null && file.exists()) {
                textArea.setText(file.toString());
            }
            m.consume();
        };
        textArea.setEditable(false);
        textArea.setOnMouseClicked(eh);

        Button b = new Button();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(GuiStyle.CLASS_BROWSE);
        b.setOnMouseClicked(eh);

        textArea.setText(Optional.ofNullable(de.getValue()).map(Path::toString)
                .orElse(""));
        applyFuncs.add(() -> {
            de.set(textArea.getText().equals("") ? null : Path.of(textArea.getText()));
        });

        HBox hbox = new HBox(textArea, b);
        HBox.setHgrow(textArea, Priority.ALWAYS);
        return hbox;
    }

    private static Region booleanEntryNode(SettingsEntry.BooleanEntry be, Set<Runnable> applyFuncs) {
        JFXCheckBox cb = new JFXCheckBox();
        cb.setSelected(be.getValue());
        applyFuncs.add(() -> {
            be.set(cb.isSelected());
        });
        return cb;
    }

    private static Region integerEntryNode(SettingsEntry.IntegerEntry ie, Set<Runnable> applyFuncs) {
        JFXSlider slider = new JFXSlider(ie.getMin(), ie.getMax(), ie.getValue());
        applyFuncs.add(() -> {
            ie.set((int) Math.round(slider.getValue()));
        });
        return slider;
    }

    private static Region stringEntryNode(SettingsEntry.StringEntry se, Set<Runnable> applyFuncs) {
        TextField tf = new TextField();
        applyFuncs.add(() -> {
            se.set(tf.getText().equals("") ? null : tf.getText());
        });
        tf.setText(Optional.ofNullable(se.getValue()).orElse(""));
        return tf;
    }

    public static Node section(String id, Set<Runnable> applyFuncs, SettingsEntry<?>... entries) {
        GridPane grid = new GridPane();

        var t = new Text(PdxuI18n.get(id));
        t.setStyle("-fx-font-weight: bold");
        TextFlow name = new TextFlow(t);
        grid.add(name, 0, 0, 3, 1);

        int row = 1;
        for (var entry : entries) {
            grid.add(GuiTooltips.helpNode(entry.getDescription()), 0, row);
            grid.add(new Label(entry.getName() + ":"), 1, row);

            Region val;
            if (entry.getType().equals(SettingsEntry.Type.PATH)) {
                val = pathEntryNode((SettingsEntry<Path>) entry, applyFuncs);
            } else if (entry.getType().equals(SettingsEntry.Type.BOOLEAN)) {
                val = booleanEntryNode((SettingsEntry.BooleanEntry) entry, applyFuncs);
            } else if (entry.getType().equals(SettingsEntry.Type.INTEGER)) {
                val = integerEntryNode((SettingsEntry.IntegerEntry) entry, applyFuncs);
            } else if (entry.getType().equals(SettingsEntry.Type.STRING)) {
                val = stringEntryNode((SettingsEntry.StringEntry) entry, applyFuncs);
            } else {
                throw new IllegalArgumentException();
            }

            grid.add(val, 2, row);
            GridPane.setHgrow(val, Priority.ALWAYS);
            row++;
        }

        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }
}
