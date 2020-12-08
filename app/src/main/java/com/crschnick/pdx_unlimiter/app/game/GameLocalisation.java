package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameLocalisation {

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

    private static Map<Key, Map<String,String>> LOCALISATIONS = new HashMap<>();

    public static String getTagNameForEntry(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, Eu4Tag tag) {
        if (tag.isCustom()) {
            return tag.getName();
        }

        Key key = new Key(GameInstallation.EU4,
                entry.getInfo().getMods().stream()
                        .map(m -> GameInstallation.EU4.getModForName(m))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                entry.getInfo().getDlcs().stream()
                        .map(m -> GameInstallation.EU4.getDlcForName(m))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));

        if (!LOCALISATIONS.containsKey(key)) {

            Map<String, String> i18n = new HashMap<>();

            Pattern p = Pattern.compile("\\s+([A-Za-z_]+):\\d?\\s+\"(.+)\"");
            CascadeDirectoryHelper.traverseDirectory(Path.of("localisation"), entry, GameInstallation.EU4, in -> {
                var reader = new BufferedReader(new InputStreamReader(in));
                try {
                    String line = reader.readLine();
                    if (!line.contains("l_english")) {
                        return;
                    }

                    reader.lines().forEach(s -> {
                        Matcher m = p.matcher(s);
                        if (m.matches()) {
                            i18n.put(m.group(1), m.group(2));
                        }
                    });
                } catch (IOException e) {
                }
            });

            LOCALISATIONS.put(key, i18n);
        }

        return LOCALISATIONS.get(key).getOrDefault(tag.getTag(), "Unknown");
    }
}
