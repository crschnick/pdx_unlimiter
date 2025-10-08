package com.crschnick.pdxu.app.savegame;


import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import com.crschnick.pdxu.io.savegame.SavegameType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class FileImportTarget {

    public static List<StandardImportTarget> createStandardImportsTargets(String toImport) {
        Path p;
        try {
            p = Path.of(toImport);
        } catch (InvalidPathException ignored) {
            TrackEvent.warn("Unable to determine import target for " + toImport);
            return List.of();
        }

        if (Files.isDirectory(p)) {
            List<StandardImportTarget> targets = new ArrayList<>();
            try (var s = Files.list(p)) {
                s.forEach(f -> targets.addAll(
                        FileImportTarget.createStandardImportsTargets(f.toString())));
            } catch (IOException e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }
            return targets;
        }

        List<StandardImportTarget> targets = new ArrayList<>();
        var type = SavegameType.getTypeForFile(p);

        // Don't determine type from file contents with import targets
        if (type == null) {
            return List.of();
        }

        var storage = SavegameStorage.get(type);
        if (storage.equals(SavegameStorage.get(Game.STELLARIS))) {
            if (Game.STELLARIS.isEnabled()) {
                if (p.getFileName().toString().equals("ironman.sav")) {
                    targets.add(new StellarisIronmanImportTarget(p));
                } else {
                    targets.add(new StellarisNormalImportTarget(p));
                }
            }
        } else {
            var game = SavegameStorage.ALL.inverseBidiMap().get(storage);
            if (game.isEnabled()) {
                targets.add(new StandardImportTarget(storage, p));
            }
        }
        return targets;
    }

    public static List<? extends FileImportTarget> createTargets(String toImport) {
        return createStandardImportsTargets(toImport);
    }

    public abstract void importTarget(Consumer<Optional<SavegameParseResult>> onFinish);

    public abstract void delete();

    protected abstract String getRawName();

    private static final Pattern ID_MATCHER = Pattern.compile("\\s*\\(([\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12})\\)");

    public String getName() {
        var raw = getRawName();
        var m = ID_MATCHER.matcher(raw);
        if (m.find()) {
            return m.replaceAll("");
        }

        return raw;
    }

    public Optional<UUID> getCampaignIdOverride() {
        var raw = getRawName();
        var m = ID_MATCHER.matcher(raw);
        if (m.find()) {
            var id = m.group(1);
            try {
                return Optional.of(UUID.fromString(id));
            } catch (Exception ignored) {}
        }

        return Optional.empty();
    }

    public abstract Path getPath();

    public static class StandardImportTarget extends FileImportTarget implements Comparable<StandardImportTarget> {

        private final SavegameStorage<?, ?> savegameStorage;
        protected Path path;
        private Instant timestamp;

        public StandardImportTarget(SavegameStorage<?, ?> savegameStorage, Path path) {
            this.savegameStorage = savegameStorage;
            this.path = path;

            // Calculate timestamp once, since it can change later on and mess up the comparator logic
            try {
                timestamp = Files.getLastModifiedTime(path).toInstant();
            } catch (IOException e) {
                // In some conditions, the import target may already not exist anymore.
                timestamp = Instant.EPOCH;
            }
        }

        public final boolean hasImportedSourceFile() {
            var cs = getSourceFileChecksum();
            if (cs == null) {
                return false;
            }

            return savegameStorage.hasImportedSourceFile(cs);
        }

        @Override
        public int compareTo(StandardImportTarget o) {
            var time = timestamp.compareTo(o.timestamp);
            if (time != 0) {
                return time;
            }

            return getName().compareTo(o.getName());
        }

        public void importTarget(Consumer<Optional<SavegameParseResult>> onFinish) {
            TaskExecutor.getInstance().submitTask(() -> {
                // File might no longer exist, since this is executed asynchronously in the task executor queue
                if (!Files.exists(path)) {
                    return;
                }

                var layout = AppLayoutModel.get();
                if (layout != null) {
                    layout.selectGame(SavegameStorage.ALL.inverseBidiMap().get(savegameStorage));
                }

                onFinish.accept(savegameStorage.importSavegame(
                        path, true, getSourceFileChecksum(), getCampaignIdOverride().orElse(null)));
            }, true);
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
                    ErrorEventFactory.fromThrowable(e).handle();
                }
            }, false);
        }

        @Override
        public String getRawName() {
            return FilenameUtils.getBaseName(path.toString());
        }

        public Path getPath() {
            return path;
        }

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
                // Even the existence check before is no guarantee that no IO exception
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
        public Optional<UUID> getCampaignIdOverride() {
            var gameIdSplit = path.getParent().getFileName().toString().lastIndexOf("_");
            if (gameIdSplit != -1) {
                return Optional.of(
                        UUID.nameUUIDFromBytes(path.getParent().getFileName().toString().substring(gameIdSplit).getBytes(StandardCharsets.UTF_8)));
            }
            return super.getCampaignIdOverride();
        }

        @Override
        public String getRawName() {
            var gameIdSplit = path.getParent().getFileName().toString().lastIndexOf("_");
            var date = FilenameUtils.getBaseName(path.getFileName().toString());
            var campaignName = gameIdSplit != -1 ? path.getParent().getFileName().toString().substring(0, gameIdSplit) : path.getParent().getFileName().toString();
            return campaignName + " (" + date + ")";
        }
    }

    public static final class StellarisIronmanImportTarget extends StandardImportTarget {

        public StellarisIronmanImportTarget(Path path) {
            super(SavegameStorage.ALL.get(Game.STELLARIS), path);
        }

        @Override
        public Optional<UUID> getCampaignIdOverride() {
            var gameIdSplit = path.getParent().getFileName().toString().lastIndexOf("_");
            if (gameIdSplit != -1) {
                return Optional.of(
                        UUID.nameUUIDFromBytes(path.getParent().getFileName().toString().substring(gameIdSplit).getBytes(StandardCharsets.UTF_8)));
            }
            return super.getCampaignIdOverride();
        }

        @Override
        public String getRawName() {
            var n = path.getParent().getFileName();
            var gameIdSplit = n.toString().lastIndexOf("_");
            if (gameIdSplit == -1) {
                return n.toString();
            }

            return n.toString().substring(0, gameIdSplit);
        }
    }
}
