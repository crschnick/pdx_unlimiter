package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;

import java.nio.file.Path;
import java.util.Map;

public class FileEditTarget extends EditTarget {

    public FileEditTarget(Path file) {
        super(file, TextFormatParser.textFileParser(), TextFormatWriter.textFileWriter());
    }

    public FileEditTarget(Path file, TextFormatParser parser, TextFormatWriter writer) {
        super(file, parser, writer);
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        return Map.of("root", parser.parse(file));
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        writer.write(nodeMap.get("root"), Integer.MAX_VALUE, "\t", file);
    }
}
