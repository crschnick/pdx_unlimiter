package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import com.crschnick.pdxu.io.savegame.SavegameType;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class FileImportTarget {

    protected FileImportTarget(boolean cloud) {
        this.cloud = cloud;
    }

    public static List<StandardImportTarget> createStandardImportsTargets(boolean cloud, String toImport) {
        Path p;
        try {
            p = Path.of(toImport);
        } catch (InvalidPathException ignored) {
            LoggerFactory.getLogger(FileImportTarget.class).warn("Unable to determine import target for " + toImport);
            return List.of();
        }

        if (Files.isDirectory(p)) {
            List<StandardImportTarget> targets = new ArrayList<>();
            try (var list = Files.list(p)) {
                list.forEach(f -> targets.addAll(
                        FileImportTarget.createStandardImportsTargets(cloud, f.toString())));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
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
                    targets.add(new StellarisIronmanImportTarget(cloud, p));
                } else {
                    targets.add(new StellarisNormalImportTarget(cloud, p));
                }
            }
        } else {
            var game = SavegameStorage.ALL.inverseBidiMap().get(storage);
            if (game.isEnabled()) {
                targets.add(new StandardImportTarget(cloud, storage, p));
            }
        }
        return targets;
    }

    @Getter
    private final boolean cloud;

    public abstract void importTarget(Consumer<Optional<SavegameParseResult>> onFinish);

    public abstract void delete();

    protected abstract String getRawName();

    private static final Pattern ID_MATCHER = Pattern.compile("^(.+?) \\(([\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12})\\)$");

    public String getName() {
        var raw = getRawName();
        var m = ID_MATCHER.matcher(raw);
        if (m.matches()) {
            return m.group(1);
        }

        return raw;
    }

    public Optional<UUID> getCampaignIdOverride() {
        var raw = getRawName();
        var m = ID_MATCHER.matcher(raw);
        if (m.matches()) {
            var id = m.group(2);
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

        public StandardImportTarget(boolean cloud, SavegameStorage<?, ?> savegameStorage, Path path) {
            super(cloud);
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

            return SavegameStorage.get(SavegameManagerState.get().current()).hasImportedSourceFile(cs);
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
                    ErrorHandler.handleException(e);
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

        public StellarisNormalImportTarget(boolean cloud, Path path) {
            super(cloud, SavegameStorage.ALL.get(Game.STELLARIS), path);
        }

        @Override
        public String getRawName() {
            return path.getParent().getFileName().toString().split("_")[0] + " " + FilenameUtils.getBaseName(
                    path.getFileName().toString());
        }
    }

    public static final class StellarisIronmanImportTarget extends StandardImportTarget {

        public StellarisIronmanImportTarget(boolean cloud, Path path) {
            super(cloud, SavegameStorage.ALL.get(Game.STELLARIS), path);
        }

        @Override
        public String getRawName() {
            return path.getParent().getFileName().toString().split("_")[0];
        }
    }
}
