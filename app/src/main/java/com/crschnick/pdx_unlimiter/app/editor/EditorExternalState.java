package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.FileWatchManager;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class EditorExternalState {

    private static final Path TEMP = FileUtils.getTempDirectory().toPath()
            .resolve("pdxu").resolve("editor");
    private Set<Entry> openEntries = new HashSet<>();

    public static void init() {
        try {
            FileUtils.forceMkdir(TEMP.toFile());
            FileUtils.cleanDirectory(TEMP.toFile());
            FileWatchManager.getInstance().startWatchersInDirectories(List.of(TEMP), changed -> {
                if (!Files.exists(changed)) {
                    removeForFile(changed);
                } else {
                    // Files that are created initially are not yet added to entries (sometimes)
                    getForFile(changed).ifPresent(e -> {
                        try {
                            Node newNode = TextFormatParser.textFileParser().parse(Files.newInputStream(changed));
                            e.update(newNode);
                            e.state.update();
                        } catch (Exception ex) {
                            ErrorHandler.handleException(ex);
                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeForFile(Path file) {
        for (var ed : Editor.getEditors().keySet()) {
            ed.getExternalState().openEntries.removeIf(es -> es.file.equals(file));
        }
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
        Path file = TEMP.resolve(UUID.randomUUID().toString() + ".pdxt");

        try {
            Files.writeString(file, TextFormatWriter.write(node.getNode()));
            openEntries.add(new Entry(file, node, state));
            ThreadHelper.open(file);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static class Entry {
        private Path file;
        private EditorNode editorNode;
        private EditorState state;

        public Entry(Path file, EditorNode editorNode, EditorState state) {
            this.file = file;
            this.editorNode = editorNode;
            this.state = state;
        }

        public void update(Node newNode) {
            if (editorNode.isSynthetic()) {
                editorNode.getParent().getNode().getNodeArray().removeIf(n -> n instanceof KeyValueNode &&
                        n.getKeyValueNode().getKeyName().equals(editorNode.getKeyName().get()));

                editorNode.getParent().getNode().getNodeArray().addAll(newNode.getNodeArray());
                return;
            }

            editorNode.getKeyName().ifPresentOrElse(s -> {
                editorNode.getParent().getNode().getNodeArray().set(editorNode.getKeyIndex(),
                        KeyValueNode.create(s, newNode));
            }, () -> {
                editorNode.getParent().getNode().getNodeArray().set(editorNode.getKeyIndex(), newNode);
            });
        }
    }
}
