package com.crschnick.pdx_unlimiter.app.installation;

public enum Game {

    EU4("eu4", "EU4", "Europa Universalis IV", "abc", GameInstallType.EU4),
    HOI4("hoi4", "HOI4", "Hearts of Iron IV", null, GameInstallType.HOI4),
    CK3("ck3", "CK3", "Crusader Kings III", "ParadoxInteractive.ProjectTitus", GameInstallType.CK3),
    STELLARIS("stellaris", "Stellaris", "Stellaris", "ParadoxInteractive.Stellaris-MicrosoftStoreEdition", GameInstallType.STELLARIS);

    private final String id;
    private final String abbreviation;
    private final String fullName;
    private final String windowsStoreName;
    private final GameInstallType installType;

    Game(String id, String abbreviation, String fullName, String windowsStoreName, GameInstallType installType) {
        this.id = id;
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.windowsStoreName = windowsStoreName;
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
}
