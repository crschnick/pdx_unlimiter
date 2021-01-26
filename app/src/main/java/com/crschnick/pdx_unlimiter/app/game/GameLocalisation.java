package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GameLocalisation {

    private static Map<Key, Map<String, String>> LOCALISATIONS = new HashMap<>();

    public static String getTagNameForEntry(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        if (tag.isCustom()) {
            return tag.getName();
        }

        Key key = new Key(GameInstallation.EU4,
                info.getMods().stream()
                        .map(m -> GameInstallation.EU4.getModForName(m))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                info.getDlcs().stream()
                        .map(m -> GameInstallation.EU4.getDlcForName(m))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));

        if (!LOCALISATIONS.containsKey(key)) {
            Map<String, String> i18n = new HashMap<>();

            CascadeDirectoryHelper.traverseDirectory(Path.of("localisation"), info, GameInstallation.EU4, file -> {
                if (!LocalisationHelper.isLanguage(file, LocalisationHelper.Language.ENGLISH)) {
                    return;
                }

                var loc = LocalisationHelper.loadTranslations(
                        file);
                i18n.putAll(loc);
            });

            LOCALISATIONS.put(key, i18n);
        }

        return LOCALISATIONS.get(key).getOrDefault(tag.getTag(), "Unknown");
    }

    public static class Key {
        private GameInstallation install;
        private List<GameMod> mods;
        private List<GameDlc> dlcs;

        public Key(GameInstallation install, List<GameMod> mods, List<GameDlc> dlcs) {
            this.install = install;
            this.mods = mods;
            this.dlcs = dlcs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return install.equals(key.install) &&
                    mods.equals(key.mods) &&
                    dlcs.equals(key.dlcs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(install, mods, dlcs);
        }

        public GameInstallation getInstall() {
            return install;
        }

        public List<GameMod> getMods() {
            return mods;
        }

        public List<GameDlc> getDlcs() {
            return dlcs;
        }
    }
}
