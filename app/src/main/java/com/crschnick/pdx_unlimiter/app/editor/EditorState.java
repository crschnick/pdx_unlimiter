package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditorState {

    private EditorExternalState externalState;
    private ListProperty<EditorNode> nodePath;
    private EditorFilter filter;
    private ListProperty<EditorNode> content;

    public EditorState(ArrayNode node) {
        externalState = new EditorExternalState();
        nodePath = new SimpleListProperty<>(FXCollections.observableArrayList());
        nodePath.add(new EditorNode(null, 0, null, node, false));
        filter = new EditorFilter(this);
        content = new SimpleListProperty<>(FXCollections.observableArrayList());
        update();
    }

    public List<EditorNode> createEditorNodes(EditorNode parent) {
        var filtered = filter.filter(parent.getNode().getNodeArray());
        var uniqueKeys = filtered.stream()
                .filter(node -> node instanceof KeyValueNode)
                .map(node -> node.getKeyValueNode().getKeyName())
                .distinct()
                .collect(Collectors.toMap(n -> n, n -> parent.getNode().getNodesForKey(n)));

        var toShow = new ArrayList<EditorNode>();
        int index = 0;
        for (var e : uniqueKeys.entrySet()) {
            if (e.getValue().size() == 1) {
                toShow.add(new EditorNode(parent, index, e.getKey(), e.getValue().get(0), false));
            } else {
                toShow.add(new EditorNode(parent, index, e.getKey(), new ArrayNode(e.getValue()), true));
            }
            index++;
        }
        return toShow;
    }

    public void update() {
        var selected = nodePath.get(nodePath.size() - 1);
        content.set(FXCollections.observableArrayList(createEditorNodes(selected)));
    }

    public void navigateTo(EditorNode newNode) {
        int index = nodePath.indexOf(newNode);
        if (index == -1) {
            nodePath.add(newNode);
        } else {
            nodePath.removeIf(n -> nodePath.indexOf(n) > index);
        }

        update();
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
