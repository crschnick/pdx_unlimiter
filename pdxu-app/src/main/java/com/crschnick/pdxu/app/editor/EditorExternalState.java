package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.FileWatchManager;
import com.crschnick.pdxu.app.editor.node.EditorNode;
import com.crschnick.pdxu.app.gui.dialog.GuiEditorSettings;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class EditorExternalState {

    private static final Path TEMP = FileUtils.getTempDirectory().toPath()
            .resolve("pdxu").resolve("editor");
    private static final Logger logger = LoggerFactory.getLogger(EditorExternalState.class);
    private final Set<Entry> openEntries = new CopyOnWriteArraySet<>();

    public static void init() {
        try {
            FileUtils.forceMkdir(TEMP.toFile());

            try {
                // Remove old editor files in dir
                FileUtils.cleanDirectory(TEMP.toFile());
            } catch (IOException ignored) {
            }

            FileWatchManager.getInstance().startWatchersInDirectories(List.of(TEMP), (changed, kind) -> {
                if (!Files.exists(changed)) {
                    removeForFile(changed);
                } else {
                    getForFile(changed).ifPresent(e -> {
                        try {
                            logger.trace("Registering modification for file " + TEMP.relativize(e.file));
                            logger.trace("Last modification for file: " + e.lastModified.toString() +
                                    " vs current one: " + e.getLastModified());
                            if (e.hasChanged()) {
                                logger.trace("Registering change for file " + TEMP.relativize(e.file) +
                                        " for editor node " + e.editorNode.getDisplayKeyName());

                                e.registerChange();
                                ArrayNode newNode = e.state.getParser().parse(changed);
                                e.editorNode.update(newNode);
                                e.state.onFileChanged();
                            }
                        } catch (Exception ex) {
                            ErrorHandler.handleException(ex, null, changed);
                        }
                    });
                }
            });
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private static void removeForFile(Path file) {
        for (var ed : Editor.getEditors().keySet()) {
            ed.getExternalState().openEntries.removeIf(es -> es.file.equals(file));
        }
    }

    private static Optional<Entry> getForNode(EditorNode node) {
        for (var ed : Editor.getEditors().keySet()) {
            for (var es : ed.getExternalState().openEntries) {
                if (es.editorNode.equals(node)) {
                    return Optional.of(es);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Entry> getForFile(Path file) {
        for (var ed : Editor.getEditors().keySet()) {
            for (var es : ed.getExternalState().openEntries) {
                if (es.file.equals(file)) {
                    return Optional.of(es);
                }
            }
        }
        return Optional.empty();
    }

    public void startEdit(EditorState state, EditorNode node) {
        var ext = getForNode(node);
        if (ext.isPresent()) {
            openFile(ext.get().file.toString());
            return;
        }

        Path file = TEMP.resolve(UUID.randomUUID() + ".pdxt");
        try (var out = Files.newOutputStream(file)) {
            NodeWriter.write(out, state.getParser().getCharset(), node.toWritableNode(),
                    EditorSettings.getInstance().indentation.getValue(), 0);
            var entry = new Entry(file, node, state);
            entry.registerChange();
            openEntries.add(entry);
            openFile(file.toString());
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }
    }

    private void openFile(String file) {
        var editor = EditorSettings.getInstance().externalEditor.getValue();
        if (editor == null) {
            GuiEditorSettings.showEditorSettings();
            return;
        }

        try {
            Runtime.getRuntime().exec(editor + " \"" + file + "\"");
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static class Entry {
        private final Path file;
        private final EditorNode editorNode;
        private final EditorState state;
        private Instant lastModified;

        public Entry(Path file, EditorNode editorNode, EditorState state) {
            this.file = file;
            this.editorNode = editorNode;
            this.state = state;
        }

        public boolean hasChanged() {
            try {
                var newDate = Files.getLastModifiedTime(file).toInstant();
                return !newDate.equals(lastModified);
            } catch (IOException e) {
                return false;
            }
        }

        public Instant getLastModified() {
            try {
                return Files.getLastModifiedTime(file).toInstant();
            } catch (IOException e) {
                return Instant.EPOCH;
            }
        }

        public void registerChange() {
            lastModified = getLastModified();
        }
    }
}
