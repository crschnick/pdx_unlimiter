package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
                .flatMap(Optional::stream)
                .map(GameMod::getPath)
                .collect(Collectors.toList()));

//        dirs.addAll(info.getDlcs().stream()
//                .map(install::getDlcForName)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .map(GameDlc::getDataPath)
//                .collect(Collectors.toList()));

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
                // We don't read from zip files because it is almost never required
                // So just ignore zip files
                continue;
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
}
