package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.editor.node.EditorNode;

import java.util.List;

public record EditorNavLocation(EditorNavPath path, int page, double scroll) {

    public static EditorNavLocation nodeInFocus(List<EditorNode> content, int totalSize,
                                                EditorNavPath path, EditorNode focus) {
        if (path.getPath().size() == 1) {
            return new EditorNavLocation(path);
        }

        var approxIndex = focus.getIndexInParent() * ((double) content.size() / totalSize);
        var nodeSize = content.size();
        var pageSizes = EditorContent.calculatePageSizes(nodeSize);
        int sum = 0;
        for (int page = 0; page < pageSizes.size(); page++) {
            if (sum + pageSizes.get(page) > approxIndex) {
                double scroll = (approxIndex - sum + 1) / pageSizes.get(page);
                return new EditorNavLocation(path, page, scroll);
            }

            sum += pageSizes.get(page);
        }
        return new EditorNavLocation(path, pageSizes.size() -1, 1.0);
    }

    public EditorNavLocation(EditorNavPath path) {
        this(path, 0, 0.0);
    }

    public EditorNode getEditorNode() {
        return path.getEditorNode();
    }
}