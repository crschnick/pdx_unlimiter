package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.app.installation.GameFileContext;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompressedEditTarget extends EditTarget {

    private final Set<String> entries;

    public CompressedEditTarget(GameFileContext context, Path file, TextFormatParser parser, Set<String> entries) {
        super(context, file, parser);
        this.entries = entries;
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        var map = new HashMap<String, Node>();
        var fs = FileSystems.newFileSystem(file);
        for (var s : entries) {
            var path = fs.getPath(s);
            var node = parser.parse(path);
            map.put(s, node);
        }
        return map;
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        try (var fs = FileSystems.newFileSystem(file)) {
            for (var s : entries) {
                var path = fs.getPath(s);
                try (var out = Files.newOutputStream(path)) {
                    NodeWriter.write(out, getParser().getCharset(), (ArrayNode) nodeMap.get(s), "\t");
                }
            }
        }
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }
}
