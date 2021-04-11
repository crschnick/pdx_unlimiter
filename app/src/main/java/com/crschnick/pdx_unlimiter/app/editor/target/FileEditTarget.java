package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.app.installation.GameFileContext;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileEditTarget extends EditTarget {

    public FileEditTarget(GameFileContext context, Path file) {
        super(context, file, TextFormatParser.textFileParser());
    }

    public FileEditTarget(GameFileContext context, Path file, TextFormatParser parser) {
        super(context, file, parser);
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        return Map.of("root", parser.parse(file));
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        try (var out = Files.newOutputStream(file)) {
            NodeWriter.write(out, getParser().getCharset(), (ArrayNode) nodeMap.get("root"), "\t");
        }
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }
}
