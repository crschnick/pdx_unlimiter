package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;

public class EditorState {

    private List<ArrayNode> nodePath;
    private EditorFilter filter;
    private List<Node> content;

    public void navigateUp() {
        nodePath.remove(nodePath.get(nodePath.size() - 1));
    }

    public void update() {
        content = filter.filter(nodePath.get(nodePath.size() - 1).getNodes());
    }

    public void navigateTo(ArrayNode newNode) {
        int index = nodePath.indexOf(newNode);
        if (index == -1) {
            nodePath.add(newNode);
        } else {
            nodePath.removeIf(n -> nodePath.indexOf(n) > index);
        }

        update();
    }
}
