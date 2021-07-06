package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.app.editor.node.EditorRootNode;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EditorState {

    private final String fileName;
    private final TextFormatParser parser;
    private final BooleanProperty dirty;
    private final Map<String, EditorRootNode> rootNodes;
    private final EditorExternalState externalState;
    private final EditorFilter filter;
    private final EditorContent content;
    private final Consumer<Map<String, ArrayNode>> saveFunc;
    private final ObjectProperty<GameFileContext> fileContext;
    private final EditorNavigation navigation;
    private final boolean savegame;

    public EditorState(String fileName, GameFileContext fileContext, Map<String, ArrayNode> nodes, TextFormatParser parser, Consumer<Map<String, ArrayNode>> saveFunc, boolean savegame) {
        this.parser = parser;
        this.fileName = fileName;
        this.saveFunc = saveFunc;

        this.fileContext = new SimpleObjectProperty<>(fileContext);
        this.savegame = savegame;
        dirty = new SimpleBooleanProperty();
        externalState = new EditorExternalState();
        filter = new EditorFilter(this);
        content = new EditorContent(this);

        rootNodes = new HashMap<>();
        int counter = 0;
        for (var e : nodes.entrySet()) {
            rootNodes.put(e.getKey(), new EditorRootNode(e.getKey(), counter, e.getValue()));
        }
        this.navigation = new EditorNavigation(this);
    }

    public void save() {
        if (!dirty.get()) {
            return;
        }

        saveFunc.accept(rootNodes.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toWritableNode())));
        dirtyProperty().set(false);
    }

    public void init() {
        content.navigate(navigation.getCurrent().getEditorNode(), 0, 0.0);
    }

    public void onFilterChange() {
        content.filterChange();
    }

    public void onTextChanged() {
        dirtyProperty().set(true);
    }

    public void onColorChanged() {
        dirtyProperty().set(true);
    }

    public void onFileChanged() {
        var newPath = EditorNavPath.rebuild(this.navigation.getCurrent().path());
        if (EditorNavPath.areNodePathsEqual(this.navigation.getCurrent().path(), newPath)) {
            this.content.completeContentChange();
        } else {
            this.navigation.replaceCurrentNavPath(newPath);
        }

        dirtyProperty().set(true);
    }

    public EditorFilter getFilter() {
        return filter;
    }

    public EditorContent getContent() {
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

    public Map<String, EditorRootNode> getRootNodes() {
        return rootNodes;
    }

    public EditorNavigation getNavigation() {
        return navigation;
    }

    public ArrayNode getBackingNode() {
        return new LinkedArrayNode(rootNodes.values().stream().map(en -> en.getBackingNode().getArrayNode()).toList());
    }

    public boolean isSavegame() {
        return savegame;
    }
}
