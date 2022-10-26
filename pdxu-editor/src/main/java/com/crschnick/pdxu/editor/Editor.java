package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.EditorProvider;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.editor.gui.GuiEditor;
import com.crschnick.pdxu.editor.target.EditTarget;
import com.crschnick.pdxu.editor.target.ExternalEditTarget;
import com.crschnick.pdxu.editor.target.SavegameEditTarget;
import com.crschnick.pdxu.editor.target.StorageEditTarget;
import com.crschnick.pdxu.io.savegame.SavegameContent;
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

import static com.crschnick.pdxu.editor.gui.GuiEditor.showCloseConfirmAlert;

public class Editor implements EditorProvider {

    private static final Map<EditorState, Stage> editors = new ConcurrentHashMap<>();

    @Override
    public void init() {
        EditorSettings.init();
        EditorExternalState.init();
    }

    @Override
    public <T, I extends SavegameInfo<T>> void openSavegame(SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
        var file = storage.getSavegameFile(entry);
        var target = new StorageEditTarget<>(storage, entry,
                new SavegameEditTarget(file, SavegameType.getTypeForFile(file)));
        createNewEditor(target);
    }

    public void browseExternalFile() {
        Platform.runLater(() -> {
            FileChooser c = new FileChooser();
            List<File> file = c.showOpenMultipleDialog(PdxuApp.getApp().getStage());
            if (file != null) {
                file.forEach(f -> createNewEditor(new ExternalEditTarget(f.toPath())));
            }
        });
    }

    public void openExternalFileIfNoSavegame(Path file) {
        if (Files.isDirectory(file)) {
            try {
                Files.list(file).forEach(this::openExternalFileIfNoSavegame);
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

    public void createNewEditor(EditTarget target) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContent nodes;
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
            }, target.isSavegame(), target.canSave());

            Platform.runLater(() -> {
                Stage stage = GuiEditor.createStage(state);
                state.init();
                editors.put(state, stage);

                stage.setOnCloseRequest(e -> {
                    if (state.dirtyProperty().get()) {
                        e.consume();
                        if (!showCloseConfirmAlert(stage)) {
                            return;
                        }
                    }

                    editors.remove(state);
                    state.getExternalState().clearEditorLeftovers(state);
                });
            });
        }, true);
    }

    public static Map<EditorState, Stage> getEditors() {
        return editors;
    }
}
