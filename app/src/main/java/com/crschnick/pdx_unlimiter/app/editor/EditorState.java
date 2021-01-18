package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditorState {

    private EditorNode root;
    private EditorExternalState externalState;
    private ListProperty<EditorNode> nodePath;
    private EditorFilter filter;
    private ListProperty<EditorNode> content;

    public EditorState(ArrayNode node) {
        externalState = new EditorExternalState();
        nodePath = new SimpleListProperty<>(FXCollections.observableArrayList());
        filter = new EditorFilter(this);
        content = new SimpleListProperty<>(FXCollections.observableArrayList());
        root = new SimpleNode(null, "root", 0, node);
        update(false);
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
                current = root;
                newPath.add(root);
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

    public void onFileChanged() {
        update(true);
    }

    void update(boolean updatePath) {
        if (updatePath) {
            rebuildPath();
        }
        var selected = nodePath.size() > 0 ? nodePath.get(nodePath.size() - 1) : null;
        content.set(FXCollections.observableArrayList(selected != null ? createEditorNodes(selected) : List.of(root)));
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
}
