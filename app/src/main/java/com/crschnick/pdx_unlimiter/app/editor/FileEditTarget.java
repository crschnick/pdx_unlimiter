package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileEditTarget extends EditTarget {

    public FileEditTarget(Path file) {
        super(file, TextFormatParser.textFileParser());
    }

    public FileEditTarget(Path file, TextFormatParser parser) {
        super(file, parser);
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        return Map.of("root", parser.parse(file));
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        try (var out = Files.newOutputStream(file)) {
            NodeWriter.write(out, getParser().getCharset(), nodeMap.get("root"), "\t");
        }
    }
}
