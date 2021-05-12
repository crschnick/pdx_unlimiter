package com.crschnick.pdx_unlimiter.app.installation;

public enum Game {

    EU4("eu4", "EU4", "Europa Universalis IV", "abc", 236850, GameInstallType.EU4),
    HOI4("hoi4", "HOI4", "Hearts of Iron IV", null, 394360, GameInstallType.HOI4),
    CK3("ck3", "CK3", "Crusader Kings III", "ParadoxInteractive.ProjectTitus", 1158310, GameInstallType.CK3),
    STELLARIS("stellaris", "Stellaris", "Stellaris",
            "ParadoxInteractive.Stellaris-MicrosoftStoreEdition", 281990, GameInstallType.STELLARIS),
    CK2("ck2", "CK2", "Crusader Kings II", null, 203770, GameInstallType.CK2),
    VIC2("vic2", "VIC2", "Victoria 2", null, 42960, GameInstallType.VIC2);

    private final String id;
    private final String abbreviation;
    private final String fullName;
    private final String windowsStoreName;
    private final int steamAppId;
    private final GameInstallType installType;

    Game(String id, String abbreviation, String fullName, String windowsStoreName, int steamAppId, GameInstallType installType) {
        this.id = id;
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.windowsStoreName = windowsStoreName;
        this.steamAppId = steamAppId;
        this.installType = installType;
    }

    public static Game byId(String id) {
        for (Game g : values()) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        throw new AssertionError("Invalid id");
    }

    public static Game byType(GameInstallType type) {
        for (Game g : values()) {
            if (g.getInstallType().equals(type)) {
                return g;
            }
        }
        throw new AssertionError("Invalid type");
    }

    public boolean isEnabled() {
        return GameInstallation.ALL.containsKey(this);
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getWindowsStoreName() {
        return windowsStoreName;
    }

    public GameInstallType getInstallType() {
        return installType;
    }

    public int getSteamAppId() {
        return steamAppId;
    }
}
