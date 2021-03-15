package com.crschnick.pdx_unlimiter.app.installation;

public enum Game {

    EU4("eu4", "EU4", "Europa Universalis IV"),
    HOI4("hoi4", "HOI4", "Hearts of Iron IV"),
    CK3("ck3", "CK3", "Crusader Kings III"),
    STELLARIS("stellaris", "Stellaris", "Stellaris");

    private final String id;
    private final String abbreviation;
    private final String fullName;

    Game(String id, String abbreviation, String fullName) {
        this.id = id;
        this.abbreviation = abbreviation;
        this.fullName = fullName;
    }

    public boolean isEnabled() {
        return GameInstallation.ALL.containsKey(this);
    }

    public static Game byId(String id) {
        for (Game g : values()) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
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
}
