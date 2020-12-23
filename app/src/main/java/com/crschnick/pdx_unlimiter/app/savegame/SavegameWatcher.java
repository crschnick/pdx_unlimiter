package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.FileWatchManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SavegameWatcher {

    public static SavegameWatcher EU4;
    public static SavegameWatcher HOI4;
    public static SavegameWatcher STELLARIS;
    public static SavegameWatcher CK3;
    private GameInstallation install;
    private ObjectProperty<List<FileImportTarget>> savegames = new SimpleObjectProperty<>();

    private SavegameWatcher(GameInstallation install) {
        this.install = install;
    }

    public static void init() {
        if (GameInstallation.EU4 != null) {
            SavegameWatcher.EU4 = new SavegameWatcher(GameInstallation.EU4);
            SavegameWatcher.EU4.initSavegames();
        }
        if (GameInstallation.HOI4 != null) {
            SavegameWatcher.HOI4 = new SavegameWatcher(GameInstallation.HOI4);
            SavegameWatcher.HOI4.initSavegames();
        }
        if (GameInstallation.STELLARIS != null) {
            SavegameWatcher.STELLARIS = new SavegameWatcher(GameInstallation.STELLARIS);
            SavegameWatcher.STELLARIS.initSavegames();
        }
        if (GameInstallation.CK3 != null) {
            SavegameWatcher.CK3 = new SavegameWatcher(GameInstallation.CK3);
            SavegameWatcher.CK3.initSavegames();
        }
    }

    public static void reset() {
        EU4 = null;
        HOI4 = null;
        STELLARIS = null;
        CK3 = null;
    }

    private void initSavegames() {
        savegames.set(getLatestSavegames());

        List<Path> savegameDirs = install.getAllSavegameDirectories();
        FileWatchManager.getInstance().startWatchersInDirectories(savegameDirs, (p) -> {
            savegames.set(getLatestSavegames());
        });
    }

    private List<FileImportTarget> getLatestSavegames() {
        return install.getAllSavegameDirectories().stream()
                .map(Path::toString)
                .map(FileImportTarget::createTargets)
                .map(List::stream)
                .flatMap(Stream::distinct)
                .sorted(Comparator.comparingLong(t -> t.getLastModified().toEpochMilli()))
                .collect(Collectors.toList());
    }

    public List<FileImportTarget> getSavegames() {
        return savegames.get();
    }

    public ObjectProperty<List<FileImportTarget>> savegamesProperty() {
        return savegames;
    }
}
