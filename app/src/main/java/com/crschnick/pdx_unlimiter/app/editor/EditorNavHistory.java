package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.NodePointer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class EditorNavHistory {

    private List<EditorNavPath> history;
    private ObjectProperty<EditorNavPath> currentPath;
    private final Map<String, EditorNode> rootNodes;

    public EditorNavHistory(Map<String, EditorNode> rootNodes) {
        this.history = new ArrayList<>(10);
        this.currentPath = new SimpleObjectProperty<>();
        this.rootNodes = rootNodes;
    }

    public void goBack() {

    }


    public void goForward() {
        history.remo
    }

    private void changeNavPath(EditorNavPath p) {
        var old = currentPath.get();
        if (old != null) {
            this.history.add(old);
        }
        this.currentPath.set(p);
    }

    public void navigateTo(NodePointer pointer) {
        EditorNavPath.createNavPath(rootNodes.values(), pointer).ifPresent(n -> {
            changeNavPath(n);
        });
    }
}
