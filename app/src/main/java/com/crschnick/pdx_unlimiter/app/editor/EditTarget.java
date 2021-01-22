package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.RawSavegameVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class EditTarget {

    protected final Path file;
    protected final TextFormatParser parser;
    protected final TextFormatWriter writer;
    public EditTarget(Path file, TextFormatParser parser, TextFormatWriter writer) {
        this.file = file;
        this.parser = parser;
        this.writer = writer;
    }

    public static Optional<EditTarget> create(Path file) {
        final EditTarget[] toReturn = {null};
        RawSavegameVisitor.vist(file, new RawSavegameVisitor() {
            @Override
            public void visitEu4(Path file) {
                try {
                    if (new Eu4SavegameParser().isCompressed(file)) {
                        toReturn[0] = new CompressedEditTarget(file,
                                TextFormatParser.eu4SavegameParser(),
                                TextFormatWriter.eu4SavegameWriter(),
                                Set.of("meta", "ai", "gamestate"));
                    } else {
                        toReturn[0] = new FileEditTarget(file,
                                TextFormatParser.eu4SavegameParser(),
                                TextFormatWriter.eu4SavegameWriter());
                    }
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitHoi4(Path file) {
                toReturn[0] = new CompressedEditTarget(file,
                        TextFormatParser.hoi4SavegameParser(),
                        TextFormatWriter.hoi4SavegameWriter(),
                        Set.of("meta", "gamestate"));
            }

            @Override
            public void visitStellaris(Path file) {
                toReturn[0] = new CompressedEditTarget(file,
                        TextFormatParser.stellarisSavegameParser(),
                        TextFormatWriter.stellarisSavegameWriter(),
                        Set.of("meta", "gamestate"));
            }

            @Override
            public void visitCk3(Path file) {

            }

            @Override
            public void visitOther(Path file) {
                toReturn[0] = new FileEditTarget(file);
            }
        });
        return Optional.ofNullable(toReturn[0]);
    }

    public abstract Map<String, Node> parse() throws Exception;

    public abstract void write(Map<String, Node> nodeMap) throws Exception;

    public String getName() {
        return file.getFileName().toString();
    }

    public TextFormatParser getParser() {
        return parser;
    }

    public TextFormatWriter getWriter() {
        return writer;
    }
}
