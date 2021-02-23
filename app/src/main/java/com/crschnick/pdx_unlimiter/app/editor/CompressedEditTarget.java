package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompressedEditTarget extends EditTarget {

    private Set<String> entries;

    public CompressedEditTarget(Path file, TextFormatParser parser, TextFormatWriter writer, Set<String> entries) {
        super(file, parser, writer);
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
                writer.write(nodeMap.get(s), Integer.MAX_VALUE, "\t", path);
            }
        }
    }
}
