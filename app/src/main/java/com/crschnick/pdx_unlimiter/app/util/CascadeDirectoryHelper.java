package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.game.GameDlc;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameMod;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CascadeDirectoryHelper {

    public static void traverseDirectory(
            Path dir,
            SavegameEntry<?, ? extends SavegameInfo<?>> entry,
            GameInstallation install,
            Consumer<Path> consumer) {
        traverseDir(dir, getCascadingDirectories(entry, install), consumer);
    }

    public static Optional<Path> openFile(
            Path file,
            SavegameEntry<?, ? extends SavegameInfo<?>> entry,
            GameInstallation install) {
        return openFile(file, getCascadingDirectories(entry, install));
    }

    private static List<Path> getCascadingDirectories(
            SavegameEntry<?, ? extends SavegameInfo<?>> entry, GameInstallation install) {
        boolean loaded = entry != null && entry.getInfo() != null;
        List<Path> dirs = new ArrayList<>();
        if (loaded) {
            dirs.addAll(entry.getInfo().getMods().stream()
                    .map(install::getModForName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(GameMod::getPath)
                    .collect(Collectors.toList()));

            dirs.addAll(entry.getInfo().getDlcs().stream()
                    .map(install::getDlcForName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(GameDlc::getDataPath)
                    .collect(Collectors.toList()));
        }
        dirs.add(install.getModBasePath());
        return dirs;
    }

    private static void traverseDir(Path traverseDir, List<Path> cascadingDirectories, Consumer<Path> consumer) {
        for (Path dir : cascadingDirectories) {
            if (!Files.isDirectory(dir)) {
                continue;
            }

            for (Iterator<File> it = FileUtils.iterateFilesAndDirs(
                    dir.toFile(),
                    FileFilterUtils.asFileFilter(f -> dir.relativize(f.toPath()).startsWith(traverseDir)),
                    FileFilterUtils.asFileFilter(f -> dir.relativize(f.toPath()).startsWith(traverseDir)));
                 it.hasNext(); ) {
                File f = it.next();
                if (f.isDirectory()) {
                    continue;
                }

                consumer.accept(f.toPath());
            }
        }
    }

    private static Optional<Path> openFile(Path file, List<Path> cascadingDirectories) {
        for (Path dir : cascadingDirectories) {
            Optional<Path> r;
            if (Files.isRegularFile(dir)) {
                r = fromZip(file, dir);
            } else {
                r = fromDir(file, dir);
            }
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    private static Optional<Path> fromDir(Path file, Path dir) {
        var abs = dir.resolve(file);
        try {
            if (Files.isRegularFile(abs)) {
                return Optional.of(abs);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(CascadeDirectoryHelper.class)
                    .trace("Exception while loading file " + file + " from directory " + dir, e);
        }
        return Optional.empty();
    }

    private static Optional<Path> fromZip(Path file, Path zip) {
        try {
            try (var fs = FileSystems.newFileSystem(zip)) {
                var entry = fs.getRootDirectories().iterator().next().resolve(file);
                if (Files.exists(entry)) {
                    return Optional.of(entry);
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(CascadeDirectoryHelper.class)
                    .trace("Exception while loading zip file " + zip.getFileName().toString(), e);
        }
        return Optional.empty();
    }
}
