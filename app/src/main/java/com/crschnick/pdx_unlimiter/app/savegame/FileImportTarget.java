package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.core.savegame.RawSavegameVisitor;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class FileImportTarget {

    public static List<FileImportTarget> createTargets(String toImport) {
        if (SavegameStorage.ALL.get(Game.EU4) != null && toImport.startsWith("pdxu")) {
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
                    if (!Game.EU4.isEnabled()) {
                        return;
                    }

                    targets.add(new StandardImportTarget(SavegameStorage.ALL.get(Game.EU4), file));
                }

                @Override
                public void visitHoi4(Path file) {
                    if (!Game.HOI4.isEnabled()) {
                        return;
                    }

                    targets.add(new StandardImportTarget(SavegameStorage.ALL.get(Game.HOI4), file));
                }

                @Override
                public void visitStellaris(Path file) {
                    if (!Game.STELLARIS.isEnabled()) {
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
                    if (!Game.CK3.isEnabled()) {
                        return;
                    }

                    targets.add(new StandardImportTarget(SavegameStorage.ALL.get(Game.CK3), file));
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

    public final boolean hasImportedSourceFile() {
        var cs = getSourceFileChecksum();
        if (cs == null) {
            return false;
        }

        return SavegameStorage.get(SavegameManagerState.get().current()).hasImportedSourceFile(cs);
    }

    public abstract void importTarget(Consumer<SavegameParser.Status> onFinish);

    public abstract Instant getLastModified();

    public abstract void delete();

    public abstract String getName();

    public abstract Path getPath();

    public abstract String getSourceFileChecksum();

    public static final class DownloadImportTarget extends FileImportTarget {

        private final URL url;
        private Path downloadedFile;

        public DownloadImportTarget(URL url) {
            this.url = url;
        }

        @Override
        public void importTarget(Consumer<SavegameParser.Status> onFinish) {
            if (GameInstallation.ALL.containsKey(Game.EU4)) {
                return;
            }

            TaskExecutor.getInstance().submitTask(() -> {
                try {
                    HttpClient client = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2)
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(url.toURI())
                            .GET()
                            .build();

                    HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    byte[] data = response.body();

                    String tempDir = System.getProperty("java.io.tmpdir");
                    this.downloadedFile = Paths.get(tempDir).resolve("pdxu")
                            .resolve(Path.of(url.getPath()).getFileName().toString() + ".eu4");
                    FileUtils.forceMkdirParent(downloadedFile.toFile());
                    Files.write(downloadedFile, data);

                    onFinish.accept(SavegameStorage.ALL.get(Game.EU4)
                            .importSavegame(downloadedFile, null, true,
                                    getSourceFileChecksum(), null));
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
        public Path getPath() {
            return downloadedFile;
        }

        @Override
        public String getSourceFileChecksum() {
            try {
                var md = MessageDigest.getInstance("MD5");
                md.update(url.toString().getBytes());

                StringBuilder c = new StringBuilder();
                ByteBuffer b = ByteBuffer.wrap(md.digest());
                for (int i = 0; i < 16; i++) {
                    var hex = String.format("%02x", b.get());
                    c.append(hex);
                }
                return c.toString();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                return null;
            }
        }
    }

    public static class StandardImportTarget extends FileImportTarget {

        private final SavegameStorage<?, ?> savegameStorage;
        protected Path path;

        public StandardImportTarget(SavegameStorage<?, ?> savegameStorage, Path path) {
            this.savegameStorage = savegameStorage;
            this.path = path;
        }

        public void importTarget(Consumer<SavegameParser.Status> onFinish) {
            TaskExecutor.getInstance().submitTask(() -> {
                // File might no longer exist, since this is executed asynchronously in the task executor queue
                if (!Files.exists(path)) {
                    return;
                }

                onFinish.accept(savegameStorage.importSavegame(
                        path, null, true, getSourceFileChecksum(), null));
            }, true);
        }

        public Instant getLastModified() {
            try {
                return Files.getLastModifiedTime(path).toInstant();
            } catch (IOException e) {
                // In some conditions, the import target may already not exist anymore.
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

        public Path getPath() {
            return path;
        }

        @Override
        public String getSourceFileChecksum() {
            if (!Files.exists(path)) {
                return null;
            }

            try {
                var md = MessageDigest.getInstance("MD5");

                // Don't use name, since this would introduce problems when importing two differently named
                // copies of the same savegame. Size and date should be enough!
                // md.update(getName().getBytes());


                long timestamp = Files.getLastModifiedTime(path).toInstant().toEpochMilli();
                md.update(Long.toString(timestamp).getBytes());
                md.update(Long.toString(Files.size(path)).getBytes());

                StringBuilder c = new StringBuilder();
                ByteBuffer b = ByteBuffer.wrap(md.digest());
                for (int i = 0; i < 16; i++) {
                    var hex = String.format("%02x", b.get());
                    c.append(hex);
                }
                return c.toString();
            } catch (Exception e) {
                // Even the exists check before is no guarantee that an IO exception
                // will be thrown because the file doesn't exist anymore
                return null;
            }
        }
    }

    public static final class StellarisNormalImportTarget extends StandardImportTarget {

        public StellarisNormalImportTarget(Path path) {
            super(SavegameStorage.ALL.get(Game.STELLARIS), path);
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString().split("_")[0] + " " + path.getFileName().toString();
        }
    }

    public static final class StellarisIronmanImportTarget extends StandardImportTarget {

        public StellarisIronmanImportTarget(Path path) {
            super(SavegameStorage.ALL.get(Game.STELLARIS), path);
        }

        @Override
        public String getName() {
            return path.getParent().getFileName().toString().split("_")[0];
        }
    }
}
