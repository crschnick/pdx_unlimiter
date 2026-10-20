package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.NodePointer;

import javafx.scene.layout.Region;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface EditorSavegameAdapter {

    Game getGame();

    Map<Game, EditorSavegameAdapter> ALL = ServiceLoader.load(EditorSavegameAdapter.class).stream()
            .map(prov -> prov.get())
            .collect(Collectors.toMap(adap -> adap.getGame(), Function.identity()));

    Map<String, NodePointer> createCommonJumps(EditorState state);

    default List<NodePointer> createNodeJumps(EditorState state, EditorRealNode node) throws Exception {
        var j = createNodeJump(state, node);
        if (j == null) {
            return List.of();
        }

        return List.of(j);
    }

    NodePointer createNodeJump(EditorState state, EditorRealNode node);

    static NodePointer keyValueJump(EditorRealNode node, BiFunction<String, String, NodePointer> get) {
        var key = node.getKeyName();
        var parentKey = Optional.ofNullable(node.getParent()).flatMap(EditorNode::getKeyName);
        var backing = node.getBackingNode();
        if (key.isPresent() && backing.isValue()) {
            return get.apply(key.get(), backing.getString());
        }

        if (key.isPresent()
                && backing.isArray()
                && backing.getArrayNode().size() == 1
                && backing.getNodeArray().getFirst().isValue()) {
            return get.apply(key.get(), backing.getNodeArray().getFirst().getString());
        }

        if (key.isEmpty() && parentKey.isPresent() && backing.isValue()) {
            return get.apply(parentKey.get(), backing.getString());
        }
        return null;
    }

    javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay);
}
