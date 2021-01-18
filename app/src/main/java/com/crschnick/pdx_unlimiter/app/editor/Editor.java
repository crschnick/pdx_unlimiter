package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Editor {

    private static Map<EditorState, Stage> editors = new HashMap<>();

    public static void createNewEditor(Node input) {
        Platform.runLater(() -> {
            EditorState state = new EditorState((ArrayNode) input);
            Stage stage = new Stage();

            var icon = PdxuApp.getApp().getIcon();
            stage.getIcons().add(icon);
            stage.setTitle("Pdx-Unlimiter Editor");

            editors.put(state, stage);
            stage.setScene(new Scene(GuiEditor.create(state), 720, 600));
            GuiStyle.addStylesheets(stage.getScene());
            stage.show();
            stage.setOnCloseRequest(e -> {
                editors.remove(state);
            });
        });
    }

    public static Map<EditorState, Stage> getEditors() {
        return editors;
    }
}
