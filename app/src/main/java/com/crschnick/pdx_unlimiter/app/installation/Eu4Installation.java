package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameWatcher;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Eu4Installation extends Installation {

    private Path userDirectory;
    private Map<String,String> countryNames = new HashMap<>();

    public Eu4Installation(Path path) {
        super("Europa Universalis IV", path);
        this.userDirectory = Paths.get(System.getProperty("user.home"), "Documents", "Paradox Interactive", "Europa Universalis IV");
    }

    public void init() {
        for (File f : getPath().resolve("history").resolve("countries").toFile().listFiles()) {
            String[] s = f.getName().split("-");
            countryNames.put(s[0].trim(), s[1].substring(0, s[1].length() - 4).trim());
        }

        try {
            SavegameWatcher.startInDirectory(userDirectory.resolve("save games"), (p) -> {
                try {
                    SavegameCache.EU4_CACHE.importSavegame(Eu4Savegame.fromFile(p));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            Runtime.getRuntime().exec(getPath().resolve("eu4.exe").toString()+ " -load");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCountryName(String tag) {
        return countryNames.get(tag);
    }

    @Override
    public boolean isValid() {
        return getPath().resolve("eu4.exe").toFile().exists();
    }
}
