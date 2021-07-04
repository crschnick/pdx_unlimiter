package com.crschnick.pdxu.app.editor;

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
    private final Map<String, EditorSimpleNode> rootNodes;
    private final EditorExternalState externalState;
    private final EditorFilter filter;
    private final EditorContent content;
    private final Consumer<Map<String, ArrayNode>> saveFunc;
    private final ObjectProperty<GameFileContext> fileContext;
    private final EditorNavHistory navHistory;
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
            rootNodes.put(e.getKey(), new EditorSimpleNode(null, e.getKey(), counter, counter, e.getValue()));
        }
        this.navHistory = new EditorNavHistory(this);
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
        content.navigate(navHistory.getCurrent().getEditorNode(), 0, 0.0);
    }

    public void onFilterChange() {
        content.filterChange();
    }

    public void onDelete() {
        content.completeContentChange();
        dirtyProperty().set(true);
    }

    public void onTextChanged() {
        dirtyProperty().set(true);
    }

    public void onColorChanged() {
        dirtyProperty().set(true);
    }

    public void onFileChanged() {
        var newPath = EditorNavPath.verify(this.navHistory.getCurrent());
        if (EditorNavPath.areNodePathsEqual(this.navHistory.getCurrent(), newPath)) {
            this.content.completeContentChange();
        } else {
            this.navHistory.replaceCurrentNavPath(newPath);
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

    public Map<String, EditorSimpleNode> getRootNodes() {
        return rootNodes;
    }

    public EditorNavHistory getNavHistory() {
        return navHistory;
    }

    public ArrayNode getBackingNode() {
        return new LinkedArrayNode(rootNodes.values().stream().map(en -> en.getBackingNode().getArrayNode()).toList());
    }

    public boolean isSavegame() {
        return savegame;
    }
}
