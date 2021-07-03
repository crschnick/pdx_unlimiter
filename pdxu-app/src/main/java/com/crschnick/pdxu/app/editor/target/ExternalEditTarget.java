package com.crschnick.pdxu.app.editor.target;

import com.crschnick.pdxu.app.editor.EditorSettings;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ExternalEditTarget extends EditTarget {

    public ExternalEditTarget(GameFileContext fileContext, Path file) {
        super(fileContext, file);
    }

    @Override
    public Map<String, ArrayNode> parse() throws Exception {
        return Map.of(getName(), getParser().parse(file));
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        try (var out = Files.newOutputStream(file)) {
            NodeWriter.write(out, getParser().getCharset(), nodeMap.values().iterator().next(),
                    EditorSettings.getInstance().indentation.getValue(), 0);
        }
    }

    @Override
    public TextFormatParser getParser() {
        return fileContext.getParser();
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }
}
