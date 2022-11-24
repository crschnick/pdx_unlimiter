package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRootNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.LinkedHashMap;
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
    private final boolean editable;

    public EditorState(
            String fileName, GameFileContext fileContext, SavegameContent nodes, TextFormatParser parser, Consumer<Map<String, ArrayNode>> saveFunc,
            boolean savegame, boolean editable
    ) {
        this.parser = parser;
        this.fileName = fileName;
        this.saveFunc = saveFunc;

        this.fileContext = new SimpleObjectProperty<>(fileContext);
        this.savegame = savegame;
        this.editable = editable;
        dirty = new SimpleBooleanProperty();
        externalState = new EditorExternalState();
        filter = new EditorFilter(this);
        content = new EditorContent(this);

        rootNodes = new LinkedHashMap<>();
        int counter = 0;
        for (var e : nodes.entrySet()) {
            rootNodes.put(e.getKey(), new EditorRootNode(e.getKey(), counter, this, e.getValue()));
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

    public void onFileChanged(EditorNode changed) {
        // Rebuild content if either an element in the currently selected nav path is changed
        // or if an editor node of the current nav path is no longer valid
        if (navigation.affectsCurrent(changed) || this.navigation.rebaseNavPathsToValid()) {
            this.content.completeContentChange();
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

    public boolean isEditable() {
        return editable;
    }

    public boolean isContextGameEnabled() {
        return fileContext.get().getGame() == null || fileContext.get().getGame().isEnabled();
    }
}
