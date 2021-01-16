package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class Editor {

    private static Map<EditorState, Stage> editors = new HashMap<>();

    public static void createNewEditor(Node input) {
        EditorState state = new EditorState((ArrayNode) input);
        Stage stage = new Stage();
        editors.put(state, stage);
        stage.setScene(new Scene(GuiEditor.create(state), 720, 600));
        stage.show();
        stage.setOnCloseRequest(e -> {
            editors.remove(state);
        });
    }

    public static Map<EditorState, Stage> getEditors() {
        return editors;
    }
}
