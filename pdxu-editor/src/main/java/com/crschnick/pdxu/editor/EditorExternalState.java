package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.app.core.AppFileWatcher;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.FileSystemHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
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
    private static final int INTERVAL = 1500;
    private final Set<Entry> openEntries = new CopyOnWriteArraySet<>();

    public static void init() {
        try {
            FileUtils.forceMkdir(TEMP.toFile());

            try {
                // Remove old editor files in dir
                FileUtils.cleanDirectory(TEMP.toFile());
            } catch (IOException ignored) {
            }

            AppFileWatcher.getInstance().startWatchersInDirectories(List.of(TEMP), (changed, kind) -> {
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    logger.trace("Editor entry file " + changed.toString() + " has been removed");
                    removeForFile(changed);
                } else {
                    getForFile(changed).ifPresent(e -> {
                        if (e.editorNode.isRoot()) {
                            ThreadHelper.sleep(INTERVAL);
                        }

                        // Wait for edit to finish in case external editor has write lock
                        if (!Files.exists(changed)) {
                            logger.trace("File " + TEMP.relativize(e.file) + " is probably still writing ...");
                            ThreadHelper.sleep(INTERVAL);

                            // If still no read lock after 500ms, just don't parse it
                            if (!Files.exists(changed)) {
                                logger.trace("Could not obtain read lock even after timeout. Ignoring change ...");
                                return;
                            }
                        }

                        try {
                            logger.trace("Registering modification for file " + TEMP.relativize(e.file));
                            logger.trace("Last modification for file: " + e.lastModified.toString() +
                                                 " vs current one: " + e.getLastModified());
                            if (e.hasChanged()) {
                                logger.trace("Registering change for file " + TEMP.relativize(e.file) +
                                                     " for editor node " + e.editorNode.getNavigationName());
                                boolean valid = e.editorNode.isValid();
                                logger.trace("Editor node " + e.editorNode.getNavigationName() + " validity: " + valid);
                                if (valid) {
                                    e.registerChange();
                                    // Use strict parsing rules!
                                    var name = e.editorNode.getNavigationName();
                                    ArrayNode newNode = e.state.getParser().parse(name, changed, true);
                                    boolean empty = newNode.size() == 0;
                                    if (!empty) {
                                        e.editorNode.update(newNode);
                                        e.state.onFileChanged(e.editorNode);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ErrorEventFactory.fromMessage(ex.getMessage()).handle();
                        }
                    });
                }
            });
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
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
        logger.trace("No editor entry found for change file " + file.toString());
        return Optional.empty();
    }

    public void startEdit(EditorState state, EditorRealNode node) {
        var ext = getForNode(node);
        if (ext.isPresent()) {
            openFile(ext.get().file.toString());
            return;
        }

        var name = FileSystemHelper.getFileSystemCompatibleName(node.getNavigationName()) + " - " + UUID.randomUUID() + ".pdxt";
        Path file = TEMP.resolve(name);
        try {
            FileUtils.forceMkdirParent(file.toFile());
            try (var out = Files.newOutputStream(file)) {
                NodeWriter.write(out, state.getParser().getCharset(), node.toWritableNode(),
                        AppPrefs.get().editorIndentation().getValue().getValue(), 0
                );
                var entry = new Entry(file, node, state);
                entry.registerChange();
                openEntries.add(entry);
                openFile(file.toString());
            }
        } catch (IOException ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
        }
    }

    public void clearEditorLeftovers(EditorState destroyed) {
        openEntries.removeIf(e -> e.state.equals(destroyed));
    }

    private void openFile(String file) {
        var editor = AppPrefs.get().editorExternalProgram().getValue();
        if (editor == null || editor.length() == 0) {
            AppPrefs.get().selectCategory("editor");
            return;
        }

        // Compatibility fix for quoted values!
        if (editor.startsWith("\"") && editor.endsWith("\"") && editor.length() >= 2) {
            editor = editor.substring(1, editor.length() - 1);
        }

        try {
            var command = SystemUtils.IS_OS_WINDOWS ?
                    List.of("cmd.exe", "/c", "start \"\" \"" + editor + "\" \"" + file + "\"") : SystemUtils.IS_OS_LINUX ?
                    List.of("sh", "-c", "\"" + editor + "\" \"" + file + "\"") : List.of("open", "-a", editor, file);
            logger.trace("Executing command: " + command);
            Runtime.getRuntime().exec(command.toArray(String[]::new));
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }
    }

    public static class Entry {
        private final Path file;
        private final EditorRealNode editorNode;
        private final EditorState state;
        private Instant lastModified;

        public Entry(Path file, EditorRealNode editorNode, EditorState state) {
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
