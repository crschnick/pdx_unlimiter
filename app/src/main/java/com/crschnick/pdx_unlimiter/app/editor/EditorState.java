package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.installation.GameFileContext;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.NodePointer;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EditorState {

    private final String fileName;
    private final TextFormatParser parser;
    private final BooleanProperty dirty;
    private final Map<String, EditorNode> rootNodes;
    private final EditorExternalState externalState;
    private final ListProperty<EditorNavPath.NavEntry> navPath;
    private final EditorFilter filter;
    private final ListProperty<EditorNode> content;
    private final Consumer<Map<String, Node>> saveFunc;
    private final ObjectProperty<GameFileContext> fileContext;
    private EditorNavHistory navHistory;

    public EditorState(String fileName, GameFileContext fileContext, Map<String, Node> nodes, TextFormatParser parser, Consumer<Map<String, Node>> saveFunc) {
        this.parser = parser;
        this.fileName = fileName;
        this.saveFunc = saveFunc;

        this.fileContext = new SimpleObjectProperty<>(fileContext);
        dirty = new SimpleBooleanProperty();
        externalState = new EditorExternalState();
        navPath = new SimpleListProperty<>(FXCollections.observableArrayList(new CopyOnWriteArrayList<EditorNavPath.NavEntry>()));
        filter = new EditorFilter(this);
        content = new SimpleListProperty<>(FXCollections.observableArrayList());

        rootNodes = new HashMap<>();
        int counter = 0;
        for (var e : nodes.entrySet()) {
            rootNodes.put(e.getKey(), new EditorSimpleNode(null, e.getKey(), counter, counter, e.getValue()));
        }
        this.navHistory = new EditorNavHistory(rootNodes);
    }

    public void save() {
        if (!dirty.get()) {
            return;
        }

        saveFunc.accept(rootNodes.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toWritableNode())));
        dirtyProperty().set(false);
    }

    public List<EditorNode> createEditorNodes(EditorNode parent) {
        var editorNodes = parent.expand();
        var filtered = filter.filter(editorNodes);
        return filtered;
    }

    private void rebuildPath() {
        EditorNode current = null;
        List<EditorNavPath.NavEntry> newPath = new ArrayList<>();
        for (var navEl : navPath) {
            if (current == null) {
                current = rootNodes.get(navEl.getEditorNode().getKeyName().get());
                newPath.add(new EditorNavPath.NavEntry(current, 0));
                continue;
            }

            var newEditorNode = current.expand().stream()
                    .filter(en -> navEl.getEditorNode().getParentIndex() == en.getParentIndex() &&
                            en.getDisplayKeyName().equals(navEl.getEditorNode().getDisplayKeyName()))
                    .findFirst();
            if (newEditorNode.isPresent()) {
                current = newEditorNode.get();
                newPath.add(new EditorNavPath.NavEntry(newEditorNode.get(), navEl.getScroll()));
            } else {
                break;
            }
        }
        navPath.set(FXCollections.observableList(newPath));
    }

    public void onDelete() {
        update(true);
        dirtyProperty().set(true);
    }

    public void onTextChanged() {
        dirtyProperty().set(true);
    }

    public void onColorChanged() {
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
        var selected = navPath.size() > 0 ? navPath.get(navPath.size() - 1) : null;
        content.set(FXCollections.observableArrayList(
                selected != null ? createEditorNodes(selected.getEditorNode()) : rootNodes.values()));
    }

    public void navigateTo(NodePointer pointer) {
        EditorNavPath.createNavPath(getRootNodes().values(), pointer).ifPresent(n -> {
            this.navPath.set(FXCollections.observableArrayList(n.getPath()));
        });
        update(false);
    }

    public void navigateTo(EditorNode newNode) {
        if (newNode == null) {
            navPath.clear();
        } else {
            int index = navPath.stream()
                    .map(EditorNavPath.NavEntry::getEditorNode)
                    .collect(Collectors.toList())
                    .indexOf(newNode);
            if (index == -1) {
                navPath.add(new EditorNavPath.NavEntry(newNode, 0));
            } else {
                navPath.removeIf(n -> navPath.indexOf(n) > index);
            }
        }

        update(false);
    }

    public ObservableList<EditorNavPath.NavEntry> getNavPath() {
        return navPath.get();
    }

    public ListProperty<EditorNavPath.NavEntry> navPathProperty() {
        return navPath;
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

    public TextFormatParser getParser() {
        return parser;
    }

    public GameFileContext getFileContext() {
        return fileContext.get();
    }

    public ObjectProperty<GameFileContext> fileContextProperty() {
        return fileContext;
    }

    public Map<String, EditorNode> getRootNodes() {
        return rootNodes;
    }
}
