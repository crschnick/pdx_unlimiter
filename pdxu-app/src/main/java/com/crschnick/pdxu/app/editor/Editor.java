package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.editor.target.EditTarget;
import com.crschnick.pdxu.app.editor.target.ExternalEditTarget;
import com.crschnick.pdxu.app.gui.editor.GuiEditor;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.savegame.SavegameType;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Editor {

    private static final Map<EditorState, Stage> editors = new ConcurrentHashMap<>();

    public static void openExternalFile() {
        if (SavegameManagerState.get().current() == null) {
            return;
        }

        Platform.runLater(() -> {
            FileChooser c = new FileChooser();
            List<File> file = c.showOpenMultipleDialog(PdxuApp.getApp().getStage());
            if (file != null) {
                file.forEach(f -> openExternalDataFile(f.toPath()));
            }
        });
    }

    public static void openExternalDataFile(Path file) {
        if (Files.isDirectory(file)) {
            try {
                Files.list(file).forEach(f -> openExternalDataFile(f));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
            return;
        }

        var type = SavegameType.getTypeForFile(file);
        if (type == null) {
            createNewEditor(new ExternalEditTarget(file));
        }
    }

    public static void createNewEditor(EditTarget target) {
        Map<String, ArrayNode> nodes;
        try {
            nodes = target.parse();
        } catch (Exception e) {
            ErrorHandler.handleException(e, null, target.getFile());
            return;
        }
        EditorState state = new EditorState(target.getName(), target.getFileContext(), nodes, target.getParser(), n -> {
            try {
                target.write(n);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, target.isSavegame());

        Platform.runLater(() -> {
            Stage stage = GuiEditor.createStage(state);
            state.init();
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
