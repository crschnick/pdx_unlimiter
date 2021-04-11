package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EditorNodePointer {

    private final List<String> path;

    public EditorNodePointer(List<String> path) {
        this.path = path;
    }

    public boolean isValid(EditorState state) {
        Node current = null;
        for (var e : state.getRootNodes().values()) {
            var ar = ((EditorSimpleNode) e).getBackingNode();
            if (ar.hasKey(path.get(0))) {
                current = ar;
                break;
            }
        }
        if (current == null) {
            return false;
        }

        for (String value : path) {
            var found = current.getNodeForKeyIfExistent(value);
            if (found.isEmpty()) {
                return false;
            } else {
                current = found.get();
            }
        }
        return true;
    }

    public Optional<List<EditorState.NavEntry>> createNavPath(EditorState state) {
        EditorNode current = null;
        List<EditorState.NavEntry> newPath = new ArrayList<>();

        for (var e : state.getRootNodes().values()) {
            if (e.expand().stream().anyMatch(n -> n.filterKey(s -> s.equals(path.get(0))))) {
                current = e;
                newPath.add(new EditorState.NavEntry(e, 0));
                break;
            }
        }
        if (newPath.size() == 0) {
            return Optional.empty();
        }

        for (String value : path) {
            var exp = current.expand();
            var found = exp.stream()
                    .filter(en -> en.filterKey(s -> s.equals(value)))
                    .findFirst();
            if (found.isEmpty()) {
                return Optional.empty();
            } else {
                //TODO: Fix scroll
                newPath.add(new EditorState.NavEntry(found.get(), 0));
                current = found.get();
            }
        }
        return Optional.of(newPath);
    }
}
