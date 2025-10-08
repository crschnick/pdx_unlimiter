package com.crschnick.pdxu.app.util;


import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameMod;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CascadeDirectoryHelper {

    public static void traverseDirectoryInverse(
            Path dir,
            GameFileContext ctx,
            Consumer<Path> consumer) {
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(dir, ctx, files::add);
        Collections.reverse(files);
        files.forEach(consumer);
    }

    public static void traverseDirectory(
            Path dir,
            GameFileContext ctx,
            Consumer<Path> consumer
    ) {
        var files = new ArrayList<Path>();
        traverseDir(dir, getCascadingDirectories(ctx), files::add);
        files.sort(Comparator.comparing(o -> o.getFileName().toString()));
        files.forEach(consumer);
    }

    public static Optional<Path> openFile(
            Path file,
            SavegameInfo<?> info
    ) {
        return openFile(file, getCascadingDirectories(GameFileContext.fromData(info.getData())));
    }

    public static Optional<Path> openFile(
            Path file,
            GameFileContext ctx
    ) {
        return openFile(file, getCascadingDirectories(ctx));
    }

    private static List<Path> getCascadingDirectories(
            GameFileContext ctx
    ) {
        List<GameMod> mods = List.of();
        try {
            mods = ctx.getMods() == null ? ctx.getInstall().queryEnabledMods() : ctx.getMods();
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }

        List<Path> dirs = mods.stream()
                .map(GameMod::getContentPath)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (ctx.getInstall() != null) {
            dirs.add(ctx.getInstall().getType().getModBasePath(ctx.getInstall().getInstallDir()));
        }

        return dirs;
    }

    private static void traverseDir(Path traverseDir, List<Path> cascadingDirectories, Consumer<Path> consumer) {
        var visited = new ArrayList<Path>();
        for (Path dir : cascadingDirectories) {
            if (!Files.isDirectory(dir)) {
                continue;
            }

            for (Iterator<File> it = FileUtils.iterateFilesAndDirs(
                    dir.toFile(),
                    FileFilterUtils.asFileFilter(f -> dir.relativize(f.toPath()).startsWith(traverseDir)),
                    FileFilterUtils.asFileFilter(f -> traverseDir.startsWith(dir.relativize(f.toPath())))
            );
                 it.hasNext(); ) {
                File f = it.next();
                if (f.isDirectory() || !dir.relativize(f.toPath()).startsWith(traverseDir)) {
                    continue;
                }

                var relativePath = dir.relativize(f.toPath());
                if (visited.contains(relativePath)) {
                    continue;
                }

                visited.add(relativePath);
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
        TrackEvent.warn("File " + file.toString() + " not found");
        return Optional.empty();
    }

    private static Optional<Path> fromDir(Path file, Path dir) {
        var abs = dir.resolve(file);
        try {
            if (Files.isRegularFile(abs)) {
                return Optional.of(abs);
            }
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable("Exception while loading file " + file + " from directory " + dir, e).omit().handle();
        }
        return Optional.empty();
    }
}
