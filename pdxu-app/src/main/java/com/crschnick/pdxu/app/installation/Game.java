package com.crschnick.pdxu.app.installation;



import com.crschnick.pdxu.app.core.AppI18n;

import java.util.List;

public enum Game {

    EU4("eu4", "EU4", "Europa Universalis IV",
            "ParadoxInteractive.EuropaUniversalisIV-MicrosoftSt",
            236850,
            "eu4",
            "EuropaUniversalis4",
            List.of("Europa Universalis IV", "Europa Universalis 4"),
            GameInstallType.EU4),

    HOI4("hoi4", "HOI4", "Hearts of Iron IV",
            "ParadoxInteractive.HeartsofIronIV-MicrosoftStoreEd",
            394360,
            "hoi4",
            null, List.of("Hearts of Iron IV", "Hearts of Iron 4"),
            GameInstallType.HOI4),

    CK3("ck3", "CK3", "Crusader Kings III",
            "ParadoxInteractive.ProjectTitus",
            1158310,
            "ck3",
            null, List.of("Crusader Kings III", "Crusader Kings 3"),
            GameInstallType.CK3),

    STELLARIS("stellaris", "STELLARIS", "Stellaris",
            "ParadoxInteractive.Stellaris-MicrosoftStoreEdition",
            281990,
            "stellaris",
            null, List.of("Stellaris"),
            GameInstallType.STELLARIS),

    CK2("ck2", "CK2", "Crusader Kings II",
            null, 203770, null,
            null, List.of("Crusader Kings II", "Crusader Kings 2"),
            GameInstallType.CK2),

    VIC2("vic2", "VIC2", "Victoria 2",
            null, 42960, null,
            null, List.of("Victoria II", "Victoria 2"),
            GameInstallType.VIC2),

    VIC3("vic3", "VIC3", "Victoria 3",
                 null, 529340, null,
                 null, List.of("Victoria III", "Victoria 3"),
    GameInstallType.VIC3);

    private final String id;
    private final String abbreviation;
    private final String installName;

    private final String windowsStoreName;
    private final int steamAppId;
    private final String paradoxGamesLauncherName;
    private final String epicGamesName;
    private final List<String> installDirNames;

    private final GameInstallType installType;

    Game(String id, String abbreviation, String installName, String windowsStoreName, int steamAppId, String paradoxGamesLauncherName, String epicGamesName, List<String> installDirNames, GameInstallType installType) {
        this.id = id;
        this.abbreviation = abbreviation;
        this.installName = installName;
        this.windowsStoreName = windowsStoreName;
        this.steamAppId = steamAppId;
        this.paradoxGamesLauncherName = paradoxGamesLauncherName;
        this.epicGamesName = epicGamesName;
        this.installDirNames = installDirNames;
        this.installType = installType;
    }

    public static Game byId(String id) {
        for (Game g : values()) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return GameInstallation.ALL.containsKey(this);
    }

    public String getTranslatedAbbreviation() {
        String base = abbreviation.toLowerCase();
        return AppI18n.get(base + "Abbreviation");
    }

    public String getId() {
        return id;
    }

    public String getInstallationName() {
        return installName;
    }

    public String getTranslatedFullName() {
        return AppI18n.get(id);
    }

    public String getWindowsStoreName() {
        return windowsStoreName;
    }

    public GameInstallType getInstallType() {
        return installType;
    }

    public String getParadoxGamesLauncherName() {
        return paradoxGamesLauncherName;
    }

    public String getEpicGamesName() {
        return epicGamesName;
    }

    public List<String> getCommonInstallDirNames() {
        return installDirNames;
    }

    public int getSteamAppId() {
        return steamAppId;
    }
}
