package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.Ck3Installation;
import com.crschnick.pdx_unlimiter.app.game.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.game.Hoi4Installation;
import com.crschnick.pdx_unlimiter.app.game.StellarisInstallation;
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
    private int fontSize;
    private boolean deleteOnImport;
    private boolean startSteam;
    private boolean confirmDeletion = true;
    private String rakalyUserId;
    private String rakalyApiKey;
    private String skanderbegApiKey;
    private Path storageDirectory;
    private boolean enableAutoUpdate;

    public static void init() {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("settings.json");
        INSTANCE = loadConfig(file);
        INSTANCE.validate();
    }

    public static Settings getInstance() {
        return INSTANCE;
    }

    public static void updateSettings(Settings newS) {
        INSTANCE = newS;
        INSTANCE.validate();
        try {
            saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private static Settings loadConfig(Path file) {
        JsonNode sNode;
        if (Files.exists(file)) {
            try {
                JsonNode node = new ObjectMapper().readTree(Files.readAllBytes(file));
                sNode = node.required("settings");
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                sNode = JsonNodeFactory.instance.objectNode();
            }
        } else {
            sNode = JsonNodeFactory.instance.objectNode();
        }

        Settings s = new Settings();
        s.eu4 = Optional.ofNullable(sNode.get("eu4")).map(n -> Paths.get(n.textValue()))
                .orElse(InstallLocationHelper.getInstallPath("Europa Universalis IV").orElse(null));
        s.hoi4 = Optional.ofNullable(sNode.get("hoi4")).map(n -> Paths.get(n.textValue()))
                .orElse(InstallLocationHelper.getInstallPath("Hearts of Iron IV").orElse(null));
        s.ck3 = Optional.ofNullable(sNode.get("ck3")).map(n -> Paths.get(n.textValue()))
                .orElse(InstallLocationHelper.getInstallPath("Crusader Kings III").orElse(null));
        s.stellaris = Optional.ofNullable(sNode.get("stellaris")).map(n -> Paths.get(n.textValue()))
                .orElse(InstallLocationHelper.getInstallPath("Stellaris").orElse(null));

        s.fontSize = Optional.ofNullable(sNode.get("fontSize")).map(JsonNode::intValue).orElse(11);
        s.startSteam = Optional.ofNullable(sNode.get("startSteam")).map(JsonNode::booleanValue).orElse(true);
        s.deleteOnImport = Optional.ofNullable(sNode.get("deleteOnImport")).map(JsonNode::booleanValue).orElse(false);
        s.rakalyUserId = Optional.ofNullable(sNode.get("rakalyUserId")).map(JsonNode::textValue).orElse(null);
        s.rakalyApiKey = Optional.ofNullable(sNode.get("rakalyApiKey")).map(JsonNode::textValue).orElse(null);
        s.skanderbegApiKey = Optional.ofNullable(sNode.get("skanderbegApiKey")).map(JsonNode::textValue).orElse(null);
        s.storageDirectory = Optional.ofNullable(sNode.get("storageDirectory")).map(n -> Paths.get(n.textValue())).orElse(null);

        Path updateFile = PdxuInstallation.getInstance().getSettingsLocation().resolve("update");
        try {
            s.enableAutoUpdate = !Files.exists(updateFile) || Boolean.parseBoolean(Files.readString(updateFile));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            s.enableAutoUpdate = true;
        }

        return s;
    }

    public static void saveConfig() throws IOException {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("settings.json");
        FileUtils.forceMkdirParent(file.toFile());

        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ObjectNode i = n.putObject("settings");
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

        i.put("deleteOnImport", s.deleteOnImport);
        i.put("fontSize", s.fontSize);
        i.put("startSteam", s.startSteam);
        if (s.rakalyUserId != null) {
            i.put("rakalyUserId", s.rakalyUserId);
        }
        if (s.rakalyApiKey != null) {
            i.put("rakalyApiKey", s.rakalyApiKey);
        }
        if (s.skanderbegApiKey != null) {
            i.put("skanderbegApiKey", s.skanderbegApiKey);
        }
        if (s.storageDirectory != null) {
            i.put("storageDirectory", s.storageDirectory.toString());
        }

        JsonHelper.write(n, Files.newOutputStream(file));


        Path updateFile = PdxuInstallation.getInstance().getSettingsLocation().resolve("update");
        Files.writeString(updateFile, Boolean.toString(s.enableAutoUpdate));
    }

    public Settings copy() {
        Settings c = new Settings();
        c.eu4 = eu4;
        c.hoi4 = hoi4;
        c.ck3 = ck3;
        c.stellaris = stellaris;
        c.fontSize = fontSize;
        c.startSteam = startSteam;
        c.enableAutoUpdate = enableAutoUpdate;
        c.deleteOnImport = deleteOnImport;
        c.rakalyUserId = rakalyUserId;
        c.rakalyApiKey = rakalyApiKey;
        c.skanderbegApiKey = skanderbegApiKey;
        c.storageDirectory = storageDirectory;
        return c;
    }

    public Optional<String> getSkanderbegApiKey() {
        return Optional.ofNullable(skanderbegApiKey);
    }

    public void setSkanderbegApiKey(String skanderbegApiKey) {
        this.skanderbegApiKey = skanderbegApiKey;
    }

    public void setConfirmDeletion(boolean confirmDeletion) {
        this.confirmDeletion = confirmDeletion;
    }

    public boolean enableAutoUpdate() {
        return enableAutoUpdate;
    }

    public void setEnableAutoUpdate(boolean enableAutoUpdate) {
        this.enableAutoUpdate = enableAutoUpdate;
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

    public Optional<Path> getStorageDirectory() {
        return Optional.ofNullable(storageDirectory);
    }

    public void setStorageDirectory(Path storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public void setDeleteOnImport(boolean deleteOnImport) {
        this.deleteOnImport = deleteOnImport;
    }

    public void validate() {
        if (eu4 != null) {
            try {
                new Eu4Installation(eu4).loadData();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                eu4 = null;
            }
        }
        if (hoi4 != null) {
            try {
                new Hoi4Installation(hoi4).loadData();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                hoi4 = null;
            }
        }
        if (ck3 != null) {
            try {
                new Ck3Installation(ck3).loadData();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                ck3 = null;
            }
        }
        if (stellaris != null) {
            try {
                new StellarisInstallation(stellaris).loadData();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                stellaris = null;
            }
        }
    }

    public boolean confirmDeletion() {
        return confirmDeletion;
    }

    public boolean deleteOnImport() {
        return deleteOnImport;
    }
}
