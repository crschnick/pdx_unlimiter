package com.crschnick.pdxu.editor.target;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DataFileEditTarget extends EditTarget {

    private static final List<String> EXCLUDED_EXTENSIONS = List.of(
            "dll", "exe", "bin", "json", "dds", "csv", "map", "bmp", "png", "html", "ttf", "ico", "ogg");

    private final GameFileContext context;

    public DataFileEditTarget(GameFileContext fileContext, Path file) {
        super(file);
        this.context = fileContext;
    }

    @Override
    public boolean isSavegame() {
        return false;
    }

    @Override
    public SavegameContent parse() throws Exception {
        // Prevent users from opening non text files
        if (EXCLUDED_EXTENSIONS.stream().anyMatch(end -> file.toString().endsWith("." + end))) {
            throw new IllegalArgumentException("Files of type ." +
                    FilenameUtils.getExtension(file.toString()) + " are not supported by the editor");
        }

        return new SavegameContent(Map.of(getName(), getParser().parse(file)));
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        try (var out = Files.newOutputStream(file)) {
            NodeWriter.write(out, getParser().getCharset(), nodeMap.values().iterator().next(),
                    AppPrefs.get().editorIndentation().getValue().getValue(), 0);
        }
    }

    @Override
    public TextFormatParser getParser() {
        return context.getParser();
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }

    @Override
    public GameFileContext getFileContext() {
        return context;
    }
}
