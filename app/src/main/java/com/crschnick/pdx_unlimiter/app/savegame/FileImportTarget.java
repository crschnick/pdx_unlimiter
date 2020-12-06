package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.eu4.savegame.RawSavegameVisitor;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public abstract class FileImportTarget {

    private SavegameCache<?,?,?,?> savegameCache;
    protected Path path;

    public FileImportTarget(SavegameCache<?,?,?,?> savegameCache, Path path) {
        this.savegameCache = savegameCache;
        this.path = path;
    }

    public final boolean importTarget() {
        return savegameCache.importSavegame(path);
    }

    public abstract void delete() throws IOException;

    public abstract String getName();

    public Path getPath() {
        return path;
    }

    public static Optional<FileImportTarget> create(Path toImport) {
        final FileImportTarget[] target = {null};
        RawSavegameVisitor.vist(toImport, new RawSavegameVisitor() {
            @Override
            public void visitEu4(Path file) {
                target[0] = new StandardImportTarget(SavegameCache.EU4_CACHE, file);
            }

            @Override
            public void visitHoi4(Path file) {
                target[0] = new StandardImportTarget(SavegameCache.HOI4_CACHE, file);
            }

            @Override
            public void visitStellaris(Path file) {
                if (file.getFileName().toString().equals("ironman.sav")) {
                    target[0] = new StellarisIronmanImportTarget(file);
                } else {
                    target[0] = new StellarisNormalImportTarget(file);
                }
            }

            @Override
            public void visitCk3(Path file) {
                target[0] = new StandardImportTarget(SavegameCache.CK3_CACHE, file);
            }

            @Override
            public void visitOther(Path file) {
                Path p = file.resolve("ironman.sav");
                if (Files.isDirectory(file) && Files.isRegularFile(p)) {
                    target[0] = new StellarisIronmanImportTarget(p);
                }
            }
        });
        return Optional.ofNullable(target[0]);
    }

    public static final class StandardImportTarget extends FileImportTarget {

        public StandardImportTarget(SavegameCache<?, ?, ?, ?> savegameCache, Path path) {
            super(savegameCache, path);
        }

        @Override
        public void delete() throws IOException {
            Files.delete(path);
        }

        @Override
        public String getName() {
            return path.getFileName().toString();
        }
    }

    public static final class StellarisNormalImportTarget extends FileImportTarget {

        public StellarisNormalImportTarget(Path path) {
            super(SavegameCache.STELLARIS_CACHE, path);
        }

        @Override
        public void delete() throws IOException {
            Files.delete(path);
            if (Files.list(path.getParent()).count() == 0) {
                Files.delete(path.getParent());
            }
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString() + " " + path.getFileName().toString();
        }
    }

    public static final class StellarisIronmanImportTarget extends FileImportTarget {

        public StellarisIronmanImportTarget(Path path) {
            super(SavegameCache.STELLARIS_CACHE, path);
        }

        @Override
        public void delete() throws IOException {
            Files.delete(path.getParent());
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString();
        }
    }
}
