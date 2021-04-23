package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.editor.target.EditTarget;
import com.crschnick.pdx_unlimiter.app.gui.editor.GuiEditor;
import com.crschnick.pdx_unlimiter.core.node.Node;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Editor {

    private static final Map<EditorState, Stage> editors = new ConcurrentHashMap<>();

    public static void createNewEditor(EditTarget target) {
        Map<String, Node> nodes;
        try {
            nodes = target.parse();
        } catch (Exception e) {
            ErrorHandler.handleException(e, null, target.getFile());
            return;
        }
        EditorState state = new EditorState(target.getName(), nodes, target.getParser(), n -> {
            try {
                target.write(n);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });

        Platform.runLater(() -> {
            Stage stage = GuiEditor.createStage(state);
            state.update(false);
            editors.put(state, stage);
            stage.setOnCloseRequest(e -> {
                editors.remove(state);
            });
        });
    }

    public static Map<EditorState, Stage> getEditors() {
        return editors;
    }
}
