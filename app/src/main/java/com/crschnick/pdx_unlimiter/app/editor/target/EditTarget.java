package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameFileContext;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.RawSavegameVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class EditTarget {

    protected final GameFileContext fileContext;
    protected final Path file;
    protected final TextFormatParser parser;

    public EditTarget(GameFileContext fileContext, Path file, TextFormatParser parser) {
        this.fileContext = fileContext;
        this.file = file;
        this.parser = parser;
    }

    public static Optional<EditTarget> create(Path file) {
        final EditTarget[] toReturn = {null};
        RawSavegameVisitor.vist(file, new RawSavegameVisitor() {
            @Override
            public void visitEu4(Path file) {
                try {
                    if (new Eu4SavegameParser().isCompressed(file)) {
                        toReturn[0] = new CompressedEditTarget(
                                GameFileContext.forGame(Game.EU4),
                                file,
                                TextFormatParser.eu4SavegameParser(),
                                Set.of("meta", "ai", "gamestate"));
                    } else {
                        toReturn[0] = new FileEditTarget(
                                GameFileContext.forGame(Game.EU4),
                                file,
                                TextFormatParser.eu4SavegameParser());
                    }
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitHoi4(Path file) {
                toReturn[0] = new FileEditTarget(
                        GameFileContext.forGame(Game.HOI4),
                        file,
                        TextFormatParser.hoi4SavegameParser());
            }

            @Override
            public void visitStellaris(Path file) {
                toReturn[0] = new CompressedEditTarget(
                        GameFileContext.forGame(Game.STELLARIS),
                        file,
                        TextFormatParser.stellarisSavegameParser(),
                        Set.of("meta", "gamestate"));
            }

            @Override
            public void visitCk3(Path file) {
                try {
                    if (Ck3SavegameParser.isCompressed(file)) {
                        toReturn[0] = new Ck3CompressedEditTarget(
                                GameFileContext.forGame(Game.CK3),
                                file);
                    } else {
                        toReturn[0] = new FileEditTarget(
                                GameFileContext.forGame(Game.CK3),
                                file);
                    }
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            @Override
            public void visitOther(Path file) {
                toReturn[0] = new FileEditTarget(GameFileContext.empty(), file);
            }
        });
        return Optional.ofNullable(toReturn[0]);
    }

    public abstract Map<String, Node> parse() throws Exception;

    public abstract void write(Map<String, Node> nodeMap) throws Exception;

    public abstract String getName();

    public TextFormatParser getParser() {
        return parser;
    }

    public Path getFile() {
        return file;
    }

    public GameFileContext getFileContext() {
        return fileContext;
    }
}
