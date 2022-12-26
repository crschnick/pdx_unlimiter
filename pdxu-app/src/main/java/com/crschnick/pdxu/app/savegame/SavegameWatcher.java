package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.FileWatchManager;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SavegameWatcher {

    public static final BidiMap<Game, SavegameWatcher> ALL = new DualHashBidiMap<>();

    private final GameInstallation install;
    private final ListProperty<FileImportTarget.StandardImportTarget> allSavegames = new SimpleListProperty<>(
            FXCollections.observableArrayList());

    private SavegameWatcher(GameInstallation install) {
        this.install = install;
    }

    public static void init() {
        for (var g : Game.values()) {
            var install = GameInstallation.ALL.get(g);
            if (install != null) {
                ALL.put(g, new SavegameWatcher(install));
                ALL.get(g).initSavegames();
            }
        }
    }

    public static void reset() {
        ALL.clear();
    }

    private void initSavegames() {
        updateSavegames();

        List<Path> savegameDirs = install.getAllSavegameDirectories();

        // In case the savegame dir does not exist, create it anyway to prevent
        // having to reload/restart to get savegame changes later on
        for (var dir : savegameDirs) {
            try {
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
            } catch (AccessDeniedException ignored) {
                // Ignore permission issues
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }

        FileWatchManager.getInstance().startWatchersInDirectories(savegameDirs, (p, k) -> {
            updateSavegames();
        });
    }

    public synchronized Optional<FileImportTarget.StandardImportTarget> getLatest() {
        return allSavegames.stream()
                .findFirst();
    }

    private synchronized void updateSavegames() {
        allSavegames.setAll(getLatestSavegames(install, install.getAllSavegameDirectories()));
    }

    private synchronized List<FileImportTarget.StandardImportTarget> getLatestSavegames(GameInstallation install, List<Path> dirs) {
        return dirs.stream()
                .map(Path::toString)
                .map(v -> FileImportTarget.createStandardImportsTargets(install, v))
                .map(List::stream)
                .flatMap(Stream::distinct)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public List<FileImportTarget.StandardImportTarget> getSavegames() {
        return allSavegames.get();
    }

    public ListProperty<FileImportTarget.StandardImportTarget> savegamesProperty() {
        return allSavegames;
    }
}
