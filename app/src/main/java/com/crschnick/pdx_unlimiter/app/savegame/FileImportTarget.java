package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.util.HttpHelper;
import com.crschnick.pdx_unlimiter.core.savegame.RawSavegameVisitor;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class FileImportTarget {

    public static List<FileImportTarget> createTargets(String toImport) {
        if (SavegameCache.EU4 != null && toImport.startsWith("pdxu")) {
            try {
                URL url = new URL(toImport.replace("pdxu", "https"));
                String id = Path.of(url.getPath()).getFileName().toString();
                URL fileUrl = new URL("https://rakaly.com/api/saves/" + id + "/file");
                return List.of(new DownloadImportTarget(fileUrl));
            } catch (Exception ignored) {
            }
        }

        try {
            List<FileImportTarget> targets = new ArrayList<>();
            Path p = Path.of(toImport);
            RawSavegameVisitor.vist(p, new RawSavegameVisitor() {
                @Override
                public void visitEu4(Path file) {
                    if (SavegameCache.EU4 == null) {
                        return;
                    }

                    targets.add(new StandardImportTarget(SavegameCache.EU4, file));
                }

                @Override
                public void visitHoi4(Path file) {
                    if (SavegameCache.HOI4 == null) {
                        return;
                    }

                    targets.add(new StandardImportTarget(SavegameCache.HOI4, file));
                }

                @Override
                public void visitStellaris(Path file) {
                    if (SavegameCache.STELLARIS == null) {
                        return;
                    }

                    if (file.getFileName().toString().equals("ironman.sav")) {
                        targets.add(new StellarisIronmanImportTarget(file));
                    } else {
                        targets.add(new StellarisNormalImportTarget(file));
                    }
                }

                @Override
                public void visitCk3(Path file) {
                    if (SavegameCache.CK3 == null) {
                        return;
                    }

                    targets.add(new StandardImportTarget(SavegameCache.CK3, file));
                }

                @Override
                public void visitOther(Path file) {
                    if (Files.isDirectory(file)) {
                        try {
                            Files.list(file).forEach(f -> targets.addAll(FileImportTarget.createTargets(f.toString())));
                        } catch (IOException e) {
                            ErrorHandler.handleException(e);
                        }
                    }
                }
            });
            return targets;
        } catch (InvalidPathException ignored) {
        }

        LoggerFactory.getLogger(FileImportTarget.class).warn("Unable to determine import target for " + toImport);
        return List.of();
    }

    public abstract void importTarget(Consumer<SavegameParser.Status> onFinish);

    public abstract Instant getLastModified();

    public abstract void delete();

    public abstract String getName();

    public abstract String toImportString();

    public abstract Path getPath();

    public static final class DownloadImportTarget extends FileImportTarget {

        private URL url;
        private Path downloadedFile;

        public DownloadImportTarget(URL url) {
            this.url = url;
        }

        @Override
        public void importTarget(Consumer<SavegameParser.Status> onFinish) {
            if (GameInstallation.EU4 == null) {
                return;
            }

            TaskExecutor.getInstance().submitTask(() -> {
                try {
                    byte[] data = HttpHelper.executeGet(url);

                    String tempDir = System.getProperty("java.io.tmpdir");
                    this.downloadedFile = Paths.get(tempDir).resolve("pdxu")
                            .resolve(Path.of(url.getPath()).getFileName().toString() + ".eu4");
                    FileUtils.forceMkdirParent(downloadedFile.toFile());
                    Files.write(downloadedFile, data);

                    onFinish.accept(SavegameCache.EU4.importSavegame(downloadedFile, null));
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                }
            }, true);
        }

        @Override
        public Instant getLastModified() {
            return Instant.now();
        }

        @Override
        public void delete() {
            TaskExecutor.getInstance().submitTask(() -> {
                if (!Files.exists(downloadedFile)) {
                    return;
                }

                try {
                    Files.delete(downloadedFile);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }, false);
        }

        @Override
        public String getName() {
            return "Rakaly.com savegame";
        }

        @Override
        public String toImportString() {
            return url.toString();
        }

        @Override
        public Path getPath() {
            return downloadedFile;
        }
    }

    public static class StandardImportTarget extends FileImportTarget {

        protected Path path;
        private SavegameCache<?, ?> savegameCache;

        public StandardImportTarget(SavegameCache<?, ?> savegameCache, Path path) {
            this.savegameCache = savegameCache;
            this.path = path;
        }

        public void importTarget(Consumer<SavegameParser.Status> onFinish) {
            TaskExecutor.getInstance().submitTask(() -> {
                onFinish.accept(savegameCache.importSavegame(path, null));
            }, true);
        }

        public Instant getLastModified() {
            try {
                return Files.getLastModifiedTime(path).toInstant();
            } catch (IOException e) {
                ErrorHandler.handleException(e);
                return Instant.MIN;
            }
        }

        @Override
        public void delete() {
            TaskExecutor.getInstance().submitTask(() -> {
                if (!Files.exists(path)) {
                    return;
                }

                try {
                    Files.delete(path);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }, false);
        }

        @Override
        public String getName() {
            return FilenameUtils.getBaseName(path.toString());
        }

        @Override
        public String toImportString() {
            return path.toString();
        }

        public Path getPath() {
            return path;
        }
    }

    public static final class StellarisNormalImportTarget extends StandardImportTarget {

        public StellarisNormalImportTarget(Path path) {
            super(SavegameCache.STELLARIS, path);
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString().split("_")[0] + " " + path.getFileName().toString();
        }
    }

    public static final class StellarisIronmanImportTarget extends StandardImportTarget {

        public StellarisIronmanImportTarget(Path path) {
            super(SavegameCache.STELLARIS, path);
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString().split("_")[0];
        }
    }
}
