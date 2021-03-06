package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.FileWatchManager;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.*;

public class EditorExternalState {

    private static final Path TEMP = FileUtils.getTempDirectory().toPath()
            .resolve("pdxu").resolve("editor");
    private final Set<Entry> openEntries = new HashSet<>();

    public static void init() {
        try {
            FileUtils.forceMkdir(TEMP.toFile());
            FileUtils.cleanDirectory(TEMP.toFile());
            FileWatchManager.getInstance().startWatchersInDirectories(List.of(TEMP), (changed, kind) -> {
                if (!Files.exists(changed)) {
                    removeForFile(changed);
                } else {
                    getForFile(changed).ifPresent(e -> {
                        try {
                            // Wait for first write
                            if (!e.registered) {
                                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    e.registered = true;
                                }
                                return;
                            }

                            ArrayNode newNode = e.state.getParser().parse(changed);
                            e.editorNode.update(newNode);
                            e.state.onFileChanged();
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
        var ex = getForNode(node);
        if (ex.isPresent()) {
            ThreadHelper.open(ex.get().file);
            return;
        }

        Path file = TEMP.resolve(UUID.randomUUID().toString() + ".pdxt");

        try {
            openEntries.add(new Entry(file, node, state));
            try (var out = Files.newOutputStream(file)) {
                NodeWriter.write(out, state.getParser().getCharset(), node.toWritableNode(), "  ");
            }
            ThreadHelper.open(file);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static class Entry {
        private final Path file;
        private final EditorNode editorNode;
        private final EditorState state;
        private boolean registered;

        public Entry(Path file, EditorNode editorNode, EditorState state) {
            this.file = file;
            this.editorNode = editorNode;
            this.state = state;
        }
    }
}
