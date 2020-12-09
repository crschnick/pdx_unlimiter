package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameDlc;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameMod;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
            GameCampaignEntry<?, ? extends SavegameInfo<?>> entry,
            GameInstallation install,
            Consumer<InputStream> consumer) {
        traverseDir(dir, getCascadingDirectories(entry, install), consumer);
    }

    public static Optional<InputStream> openFile(
            Path file,
            GameCampaignEntry<?, ? extends SavegameInfo<?>> entry,
            GameInstallation install) throws IOException {
        return openFile(file, getCascadingDirectories(entry, install));
    }

    private static List<Path> getCascadingDirectories(
            GameCampaignEntry<?, ? extends SavegameInfo<?>> entry, GameInstallation install) {
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

    private static void traverseDir(Path traverseDir, List<Path> cascadingDirectories, Consumer<InputStream> consumer) {
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

                try {
                    consumer.accept(Files.newInputStream(f.toPath()));
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }
        }
    }

    private static Optional<InputStream> openFile(Path file, List<Path> cascadingDirectories) throws IOException {
        for (Path dir : cascadingDirectories) {
            Optional<InputStream> r = null;
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

    private static Optional<InputStream> fromDir(Path file, Path dir) throws IOException {
        var abs = dir.resolve(file);
        if (Files.isRegularFile(abs)) {
            return Optional.of(Files.newInputStream(abs));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<InputStream> fromZip(Path file, Path zip) throws IOException {
        ZipFile z = new ZipFile(zip.toString());
        ZipEntry e = z.getEntry(file.toString());
        if (e == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(z.getInputStream(e));
    }
}
