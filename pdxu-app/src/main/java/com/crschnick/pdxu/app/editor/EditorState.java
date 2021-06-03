package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
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
    private final ListProperty<NavEntry> navPath;
    private final EditorFilter filter;
    private final ListProperty<EditorNode> content;
    private final Consumer<SavegameContent> saveFunc;

    public EditorState(String fileName, SavegameContent sgContent, TextFormatParser parser, Consumer<SavegameContent> saveFunc) {
        this.parser = parser;
        this.fileName = fileName;
        this.saveFunc = saveFunc;

        dirty = new SimpleBooleanProperty();
        externalState = new EditorExternalState();
        navPath = new SimpleListProperty<>(FXCollections.observableArrayList(new CopyOnWriteArrayList<NavEntry>()));
        filter = new EditorFilter(this);
        content = new SimpleListProperty<>(FXCollections.observableArrayList());

        rootNodes = new HashMap<>();
        int counter = 0;
        for (var e : sgContent.entrySet()) {
            rootNodes.put(e.getKey(), new EditorSimpleNode(null, e.getKey(), counter, counter, e.getValue()));
        }
    }

    public void save() {
        if (!dirty.get()) {
            return;
        }

        var content = new SavegameContent(rootNodes.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toWritableNode())));
        saveFunc.accept(content);
        dirtyProperty().set(false);
    }

    public List<EditorNode> createEditorNodes(EditorNode parent) {
        var editorNodes = parent.expand();
        var filtered = filter.filter(editorNodes);
        return filtered;
    }

    private void rebuildPath() {
        EditorNode current = null;
        List<NavEntry> newPath = new ArrayList<>();
        for (var navEl : navPath) {
            if (current == null) {
                current = rootNodes.get(navEl.editorNode.getKeyName().get());
                newPath.add(new NavEntry(current, 0));
                continue;
            }

            var newEditorNode = current.expand().stream()
                    .filter(en -> navEl.editorNode.getParentIndex() == en.getParentIndex() &&
                            en.getDisplayKeyName().equals(navEl.editorNode.getDisplayKeyName()))
                    .findFirst();
            if (newEditorNode.isPresent()) {
                current = newEditorNode.get();
                newPath.add(new NavEntry(newEditorNode.get(), navEl.getScroll()));
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
                selected != null ? createEditorNodes(selected.editorNode) : rootNodes.values()));
    }

    public void navigateTo(EditorNode newNode) {
        if (newNode == null) {
            navPath.clear();
        } else {
            int index = navPath.stream()
                    .map(NavEntry::getEditorNode)
                    .collect(Collectors.toList())
                    .indexOf(newNode);
            if (index == -1) {
                navPath.add(new NavEntry(newNode, 0));
            } else {
                navPath.removeIf(n -> navPath.indexOf(n) > index);
            }
        }

        update(false);
    }

    public ObservableList<NavEntry> getNavPath() {
        return navPath.get();
    }

    public ListProperty<NavEntry> navPathProperty() {
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

    public static class NavEntry {
        private final EditorNode editorNode;
        private final DoubleProperty scroll;

        private NavEntry(EditorNode editorNode, double scroll) {
            this.editorNode = editorNode;
            this.scroll = new SimpleDoubleProperty(scroll);
        }

        public EditorNode getEditorNode() {
            return editorNode;
        }

        public double getScroll() {
            return scroll.get();
        }

        public DoubleProperty scrollProperty() {
            return scroll;
        }
    }
}
