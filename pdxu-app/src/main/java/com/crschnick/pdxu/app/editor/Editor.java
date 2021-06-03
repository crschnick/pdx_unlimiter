package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.editor.target.EditTarget;
import com.crschnick.pdxu.app.gui.editor.GuiEditor;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Editor {

    private static final Map<EditorState, Stage> editors = new ConcurrentHashMap<>();

    public static void createNewEditor(EditTarget target) {
        SavegameContent content;
        try {
            content = target.parse();
        } catch (Exception e) {
            ErrorHandler.handleException(e, null, target.getFile());
            return;
        }
        EditorState state = new EditorState(target.getName(), content, target.getParser(), n -> {
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
