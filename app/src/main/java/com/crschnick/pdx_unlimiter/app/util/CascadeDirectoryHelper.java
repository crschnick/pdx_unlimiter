package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.install.GameDlc;
import com.crschnick.pdx_unlimiter.app.install.GameInstallation;
import com.crschnick.pdx_unlimiter.app.install.GameMod;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CascadeDirectoryHelper {

    public static void traverseDirectory(
            Path dir,
            SavegameInfo<?> info,
            GameInstallation install,
            Consumer<Path> consumer) {
        traverseDir(dir, getCascadingDirectories(info, install), consumer);
    }

    public static Optional<Path> openFile(
            Path file,
            SavegameInfo<?> info,
            GameInstallation install) {
        return openFile(file, getCascadingDirectories(info, install));
    }

    private static List<Path> getCascadingDirectories(
            SavegameInfo<?> info,
            GameInstallation install) {
        List<Path> dirs = new ArrayList<>();
        dirs.addAll(info.getMods().stream()
                .map(install::getModForName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(GameMod::getPath)
                .collect(Collectors.toList()));

        dirs.addAll(info.getDlcs().stream()
                .map(install::getDlcForName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(GameDlc::getDataPath)
                .collect(Collectors.toList()));

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

    private static Path pathTransform(FileSystem fs, Path path) {
        Path ret = fs.getPath(path.isAbsolute() ? fs.getSeparator() : "");
        for (final Path component : path) {
            ret = ret.resolve(component.getFileName().toString());
        }
        return ret;
    }

    private static Optional<Path> fromZip(Path file, Path zip) {
        try {
            try (var fs = FileSystems.newFileSystem(zip)) {
                var entry = fs.getRootDirectories().iterator().next().resolve(pathTransform(fs, file));
                if (Files.exists(entry)) {
                    return Optional.of(entry);
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(CascadeDirectoryHelper.class)
                    .error("Exception while loading zip file " + zip.getFileName().toString(), e);
        }
        return Optional.empty();
    }
}
