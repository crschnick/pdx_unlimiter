package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.util.InstallLocationHelper;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Settings {

    private static Settings INSTANCE;
    private Path eu4;
    private Path hoi4;
    private Path ck3;
    private Path stellaris;
    private Path activeGame;
    private int maxLoadedSavegames = 5;
    private int fontSize = 12;
    private boolean deleteOnImport = false;
    private boolean startSteam = true;
    private String rakalyUserId;
    private String rakalyApiKey;

    public static void init() throws Exception {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("installations.json");
        if (!file.toFile().exists()) {
            INSTANCE = defaultSettings();
        } else {
            INSTANCE = loadConfig(file);
        }
        INSTANCE.validate();
        INSTANCE.apply();
    }

    public static Settings getInstance() {
        return INSTANCE;
    }

    public static void updateSettings(Settings newS) {
        INSTANCE = newS;
        INSTANCE.validate();
        INSTANCE.apply();
        try {
            saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private static Settings defaultSettings() {
        Settings s = new Settings();
        s.eu4 = InstallLocationHelper.getInstallPath("Europa Universalis IV").orElse(null);
        s.hoi4 = InstallLocationHelper.getInstallPath("Hearts of Iron IV").orElse(null);
        s.ck3 = InstallLocationHelper.getInstallPath("Crusader Kings III").orElse(null);
        s.stellaris = InstallLocationHelper.getInstallPath("Stellaris").orElse(null);
        s.fontSize = 12;
        s.startSteam = true;
        s.rakalyUserId = null;
        s.rakalyApiKey = null;

        if (s.eu4 != null) {
            s.activeGame = s.eu4;
        } else if (s.hoi4 != null) {
            s.activeGame = s.hoi4;
        } else if (s.ck3 != null) {
            s.activeGame = s.ck3;
        } else if (s.stellaris != null) {
            s.activeGame = s.stellaris;
        } else {
            s.activeGame = null;
        }
        return s;
    }

    private static Settings loadConfig(Path file) throws Exception {
        JsonNode node = new ObjectMapper().readTree(Files.readAllBytes(file));
        JsonNode i = node.required("installations");
        Settings s = defaultSettings();
        s.eu4 = Optional.ofNullable(i.get("eu4")).map(n -> Paths.get(n.textValue())).orElse(s.eu4);
        s.hoi4 = Optional.ofNullable(i.get("hoi4")).map(n -> Paths.get(n.textValue())).orElse(s.hoi4);
        s.ck3 = Optional.ofNullable(i.get("ck3")).map(n -> Paths.get(n.textValue())).orElse(s.ck3);
        s.stellaris = Optional.ofNullable(i.get("stellaris")).map(n -> Paths.get(n.textValue())).orElse(s.stellaris);
        s.activeGame = Optional.ofNullable(i.get("activeGame")).map(n -> Paths.get(n.textValue())).orElse(s.activeGame);
        s.fontSize = i.required("fontSize").intValue();
        s.startSteam = i.required("startSteam").booleanValue();
        s.rakalyUserId = Optional.ofNullable(i.get("rakalyUserId")).map(JsonNode::textValue).orElse(null);
        s.rakalyApiKey = Optional.ofNullable(i.get("rakalyApiKey")).map(JsonNode::textValue).orElse(null);
        return s;
    }

    public static void saveConfig() throws IOException {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("installations.json");
        FileUtils.forceMkdirParent(file.toFile());

        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ObjectNode i = n.putObject("installations");
        Settings s = Settings.INSTANCE;
        if (s.eu4 != null) {
            i.set("eu4", new TextNode(s.eu4.toString()));
        }
        if (s.hoi4 != null) {
            i.set("hoi4", new TextNode(s.hoi4.toString()));
        }
        if (s.ck3 != null) {
            i.set("ck3", new TextNode(s.ck3.toString()));
        }
        if (s.stellaris != null) {
            i.set("stellaris", new TextNode(s.stellaris.toString()));
        }
        if (s.activeGame != null) {
            i.set("activeGame", new TextNode(s.activeGame.toString()));
        }

        i.put("fontSize", s.fontSize);
        i.put("startSteam", s.startSteam);
        i.put("rakalyUserId", s.rakalyUserId);
        i.put("rakalyApiKey", s.rakalyApiKey);

        JsonHelper.write(n, Files.newOutputStream(file));
    }

    public Settings copy() {
        Settings c = new Settings();
        c.eu4 = eu4;
        c.hoi4 = hoi4;
        c.ck3 = ck3;
        c.stellaris = stellaris;
        c.activeGame = activeGame;
        c.fontSize = fontSize;
        c.startSteam = startSteam;
        c.rakalyUserId = rakalyUserId;
        c.rakalyApiKey = rakalyApiKey;
        return c;
    }

    public Optional<Path> getEu4() {
        return Optional.ofNullable(eu4);
    }

    public void setEu4(Path eu4) {
        this.eu4 = eu4;
    }

    public Optional<Path> getHoi4() {
        return Optional.ofNullable(hoi4);
    }

    public void setHoi4(Path hoi4) {
        this.hoi4 = hoi4;
    }

    public Optional<Path> getCk3() {
        return Optional.ofNullable(ck3);
    }

    public void setCk3(Path ck3) {
        this.ck3 = ck3;
    }

    public Optional<Path> getStellaris() {
        return Optional.ofNullable(stellaris);
    }

    public void setStellaris(Path stellaris) {
        this.stellaris = stellaris;
    }

    public Optional<Path> getActiveGame() {
        return Optional.ofNullable(activeGame);
    }

    public Optional<String> getRakalyApiKey() {
        return Optional.ofNullable(rakalyApiKey);
    }

    public void setRakalyApiKey(String rakalyApiKey) {
        this.rakalyApiKey = rakalyApiKey;
    }

    public boolean startSteam() {
        return startSteam;
    }

    public void setStartSteam(boolean startSteam) {
        this.startSteam = startSteam;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Optional<String> getRakalyUserId() {
        return Optional.ofNullable(rakalyUserId);
    }

    public void setRakalyUserId(String rakalyUserId) {
        this.rakalyUserId = rakalyUserId;
    }

    public void updateActiveGame(Path activeGame) {
        this.activeGame = activeGame;
        try {
            saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public void validate() {
        if (eu4 != null && !new Eu4Installation(eu4).isValid()) {
            eu4 = null;
        }
        if (hoi4 != null && !new Hoi4Installation(hoi4).isValid()) {
            hoi4 = null;
        }
        if (ck3 != null && !new Ck3Installation(ck3).isValid()) {
            ck3 = null;
        }
        if (stellaris != null && !new StellarisInstallation(stellaris).isValid()) {
            stellaris = null;
        }

        if (activeGame != null && !activeGame.equals(eu4) && !activeGame.equals(hoi4) && !activeGame.equals(ck3) && !activeGame.equals(stellaris)) {
            activeGame = null;
        }
    }

    public void apply() {
        if (eu4 != null) {
            GameInstallation.EU4 = new Eu4Installation(eu4);
        }
        if (hoi4 != null) {
            GameInstallation.HOI4 = new Hoi4Installation(hoi4);
        }
        if (ck3 != null) {
            GameInstallation.CK3 = new Ck3Installation(ck3);
        }
        if (stellaris != null) {
            GameInstallation.STELLARIS = new StellarisInstallation(stellaris);
        }
        try {
            GameInstallation.initInstallations();
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
        GameIntegration.reload();
        GuiLayout.setFontSize(fontSize);
    }

    public boolean deleteOnImport() {
        return deleteOnImport;
    }

    public int getMaxLoadedSavegames() {
        return maxLoadedSavegames;
    }
}
