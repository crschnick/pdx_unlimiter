package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.RawSavegameVisitor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class EditTarget {

    protected final Path file;
    protected final TextFormatParser parser;
    protected final Function<OutputStream,NodeWriter> writer;

    public EditTarget(Path file, TextFormatParser parser, Function<OutputStream,NodeWriter> writer) {
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
                                NodeWriter::eu4SavegameWriter,
                                Set.of("meta", "ai", "gamestate"));
                    } else {
                        toReturn[0] = new FileEditTarget(file,
                                TextFormatParser.eu4SavegameParser(),
                                NodeWriter::eu4SavegameWriter);
                    }
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitHoi4(Path file) {
                toReturn[0] = new FileEditTarget(file,
                        TextFormatParser.hoi4SavegameParser(),
                        TextFormatWriter.hoi4SavegameWriter());
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
                try {
                    if (Ck3SavegameParser.isCompressed(file)) {
                        toReturn[0] = new Ck3CompressedEditTarget(file);
                    } else {
                        toReturn[0] = new FileEditTarget(file);
                    }
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitOther(Path file) {
                toReturn[0] = new FileEditTarget(file);
            }
        });
        return Optional.ofNullable(toReturn[0]);
    }

    public abstract void save() throws IOException;

    public abstract Map<String, Node> parse() throws Exception;

    public abstract void write(Map<String, Node> nodeMap) throws Exception;

    public String getName() {
        return file.getFileName().toString();
    }

    public TextFormatParser getParser() {
        return parser;
    }
}
