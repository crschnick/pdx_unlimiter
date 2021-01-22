package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EditorState {

    private String fileName;
    private TextFormatParser parser;
    private TextFormatWriter writer;
    private BooleanProperty dirty;
    private Map<String,EditorNode> rootNodes;
    private EditorExternalState externalState;
    private ListProperty<EditorNode> nodePath;
    private EditorFilter filter;
    private ListProperty<EditorNode> content;
    private Consumer<Map<String, Node>> saveFunc;

    public EditorState(String fileName, Map<String, Node> nodes, TextFormatParser parser, TextFormatWriter writer, Consumer<Map<String, Node>> saveFunc) {
        this.writer = writer;
        this.parser = parser;
        this.fileName = fileName;
        this.saveFunc = saveFunc;

        dirty = new SimpleBooleanProperty();
        externalState = new EditorExternalState();
        nodePath = new SimpleListProperty<>(FXCollections.observableArrayList());
        filter = new EditorFilter(this);
        content = new SimpleListProperty<>(FXCollections.observableArrayList());

        rootNodes = new HashMap<>();
        int counter = 0;
        for (var e : nodes.entrySet()) {
            rootNodes.put(e.getKey(), new SimpleNode(null, e.getKey(), counter, e.getValue()));
        }

        update(false);
    }

    public void save() {
        saveFunc.accept(rootNodes.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toWritableNode())));
        dirtyProperty().set(false);
    }

    public List<EditorNode> createEditorNodes(EditorNode parent) {
        var editorNodes = parent.open();
        var filtered = filter.filter(editorNodes);
        return filtered;
    }

    private void rebuildPath() {
        EditorNode current = null;
        List<EditorNode> newPath = new ArrayList<>();
        for (var pathEl : nodePath) {
            if (current == null) {
                current = rootNodes.get(pathEl.getKeyName().get());
                newPath.add(current);
                continue;
            }

            var newEl = current.open().stream()
                    .filter(en -> en.displayKeyName().equals(pathEl.displayKeyName()))
                    .findFirst();
            if (newEl.isPresent()) {
                current = newEl.get();
                newPath.add(newEl.get());
            } else {
                break;
            }
        }
        nodePath.set(FXCollections.observableList(newPath));
    }

    public void onDelete() {
        update(true);
        dirtyProperty().set(true);
    }

    public void onTextChanged() {
        dirtyProperty().set(true);
    }

    public void onFileChanged() {
        update(true);
        dirtyProperty().set(true);
    }

    void update(boolean updatePath) {
        if (updatePath) {
            rebuildPath();
        }
        var selected = nodePath.size() > 0 ? nodePath.get(nodePath.size() - 1) : null;
        content.set(FXCollections.observableArrayList(
                selected != null ? createEditorNodes(selected) : rootNodes.values()));
    }

    public void navigateTo(EditorNode newNode) {
        if (newNode == null) {
            nodePath.clear();
        } else {
            int index = nodePath.indexOf(newNode);
            if (index == -1) {
                nodePath.add(newNode);
            } else {
                nodePath.removeIf(n -> nodePath.indexOf(n) > index);
            }
        }

        update(false);
    }

    public ObservableList<EditorNode> getNodePath() {
        return nodePath.get();
    }

    public ListProperty<EditorNode> nodePathProperty() {
        return nodePath;
    }

    public EditorFilter getFilter() {
        return filter;
    }

    public ObservableList<EditorNode> getContent() {
        return content.get();
    }

    public ListProperty<EditorNode> contentProperty() {
        return content;
    }

    public EditorExternalState getExternalState() {
        return externalState;
    }

    public String getFileName() {
        return fileName;
    }

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public TextFormatWriter getWriter() {
        return writer;
    }

    public TextFormatParser getParser() {
        return parser;
    }
}
