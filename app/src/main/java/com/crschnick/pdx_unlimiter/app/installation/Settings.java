package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.gui.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.util.ConfigHelper;
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
    private GameDirectory eu4;
    private GameDirectory hoi4;
    private GameDirectory ck3;
    private GameDirectory stellaris;
    private int fontSize;
    private boolean deleteOnImport;
    private boolean startSteam;
    private boolean confirmDeletion = true;
    private String rakalyUserId;
    private String rakalyApiKey;
    private String skanderbegApiKey;
    private Path storageDirectory;
    private Path ck3toeu4Dir;
    private boolean enableAutoUpdate;

    public static void init() {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("settings.json");
        INSTANCE = loadConfig(file);
        INSTANCE.validate();
        try {
            saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static Settings getInstance() {
        return INSTANCE;
    }

    public static void updateSettings(Settings newS) {
        var oldValue = INSTANCE.getStorageDirectory();
        var oldDir = PdxuInstallation.getInstance().getSavegamesLocation();

        INSTANCE = newS;
        INSTANCE.validate();

        var newDir = PdxuInstallation.getInstance().getSavegamesLocation();
        if (!oldDir.equals(newDir)) {
            if (FileUtils.listFiles(newDir.toFile(), null, false).size() > 0) {
                GuiErrorReporter.showSimpleErrorMessage("New storage directory " + newDir + " must be empty!");
                INSTANCE.setStorageDirectory(oldValue.orElse(null));
            } else {
                try {
                    Files.delete(newDir);
                    FileUtils.moveDirectory(oldDir.toFile(), newDir.toFile());
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }
        }

        try {
            saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private static abstract class GameDirectory {

        abstract boolean isDisabled();
        abstract Path getPath();

        private static Optional<TextNode> toNode(GameDirectory d) {
            if (d.isDisabled()) {
                return Optional.of(new TextNode("disabled"));
            }
            return Optional.ofNullable(d.getPath()).map(Path::toString).map(TextNode::new);
        }

        private static GameDirectory disabled() {
            return new GameDirectory() {
                @Override
                boolean isDisabled() {
                    return true;
                }

                @Override
                Path getPath() {
                    return null;
                }
            };
        }

        private static GameDirectory ofPath(Path p) {
            return new GameDirectory() {
                @Override
                boolean isDisabled() {
                    return false;
                }

                @Override
                Path getPath() {
                    return p;
                }
            };
        }

        private static GameDirectory fromNode(JsonNode node, String name) {
            if (node != null && node.textValue().equals("disabled")) {
                return disabled();
            }

            var r = Optional.ofNullable(node).map(n -> Paths.get(n.textValue()))
                    .orElse(InstallLocationHelper.getInstallPath(name).orElse(null));;
            return ofPath(r);
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
        s.eu4 = GameDirectory.fromNode(sNode.get("eu4"), "Europa Universalis IV");
        s.hoi4 = GameDirectory.fromNode(sNode.get("hoi4"), "Hearts of Iron IV");
        s.ck3 =  GameDirectory.fromNode(sNode.get("ck3"), "Crusader Kings III");
        s.stellaris = GameDirectory.fromNode(sNode.get("stellaris"), "Stellaris");

        s.fontSize = Optional.ofNullable(sNode.get("fontSize")).map(JsonNode::intValue).orElse(11);
        s.startSteam = Optional.ofNullable(sNode.get("startSteam")).map(JsonNode::booleanValue).orElse(true);
        s.deleteOnImport = Optional.ofNullable(sNode.get("deleteOnImport")).map(JsonNode::booleanValue).orElse(false);
        s.rakalyUserId = Optional.ofNullable(sNode.get("rakalyUserId")).map(JsonNode::textValue).orElse(null);
        s.rakalyApiKey = Optional.ofNullable(sNode.get("rakalyApiKey")).map(JsonNode::textValue).orElse(null);
        s.skanderbegApiKey = Optional.ofNullable(sNode.get("skanderbegApiKey")).map(JsonNode::textValue).orElse(null);
        s.storageDirectory = Optional.ofNullable(sNode.get("storageDirectory")).map(n -> Paths.get(n.textValue())).orElse(null);
        s.ck3toeu4Dir = Optional.ofNullable(sNode.get("ck3toeu4Dir")).map(n -> Paths.get(n.textValue())).orElse(null);

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
        GameDirectory.toNode(s.eu4).ifPresent(dir -> {
            i.set("eu4", dir);
        });
        GameDirectory.toNode(s.hoi4).ifPresent(dir -> {
            i.set("hoi4", dir);
        });
        GameDirectory.toNode(s.ck3).ifPresent(dir -> {
            i.set("ck3", dir);
        });
        GameDirectory.toNode(s.stellaris).ifPresent(dir -> {
            i.set("stellaris", dir);
        });

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
        if (s.ck3toeu4Dir != null) {
            i.put("ck3toeu4Dir", s.ck3toeu4Dir.toString());
        }

        ConfigHelper.writeConfig(file, n);
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
        c.ck3toeu4Dir = ck3toeu4Dir;
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
        return Optional.ofNullable(eu4.getPath());
    }

    public void setEu4(Path eu4) {
        this.eu4 = GameDirectory.ofPath(eu4);
    }

    public Optional<Path> getHoi4() {
        return Optional.ofNullable(hoi4.getPath());
    }

    public void setHoi4(Path hoi4) {
        this.hoi4 = GameDirectory.ofPath(hoi4);
    }

    public Optional<Path> getCk3() {
        return Optional.ofNullable(ck3.getPath());
    }

    public void setCk3(Path ck3) {
        this.ck3 = GameDirectory.ofPath(ck3);
    }

    public Optional<Path> getStellaris() {
        return Optional.ofNullable(stellaris.getPath());
    }

    public void setStellaris(Path stellaris) {
        this.stellaris = GameDirectory.ofPath(stellaris);
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

    private void showInstallErrorMessage(Exception e, String game) {
        String msg = e.getMessage() + ".\n\n" + game + " support has been disabled.\n" +
                "If you believe that your installation is valid, " +
                "please check in the settings menu whether the installation directory was correctly set.";
        GuiErrorReporter.showSimpleErrorMessage(
                "An error occured while loading your " + game + " installation:\n" +  msg);
    }

    public void validate() {
        if (eu4.getPath() != null) {
            try {
                new Eu4Installation(eu4.getPath()).loadData();
            } catch (Exception e) {
                showInstallErrorMessage(e, "EU4");
                eu4 = GameDirectory.disabled();
            }
        }
        if (hoi4.getPath() != null) {
            try {
                new Hoi4Installation(hoi4.getPath()).loadData();
            } catch (Exception e) {
                showInstallErrorMessage(e, "HOI4");
                hoi4 = GameDirectory.disabled();
            }
        }
        if (ck3.getPath() != null) {
            try {
                new Ck3Installation(ck3.getPath()).loadData();
            } catch (Exception e) {
                showInstallErrorMessage(e, "CK3");
                ck3 = GameDirectory.disabled();
            }
        }
        if (stellaris.getPath() != null) {
            try {
                new StellarisInstallation(stellaris.getPath()).loadData();
            } catch (Exception e) {
                showInstallErrorMessage(e, "Stellaris");
                stellaris = GameDirectory.disabled();
            }
        }

        if (eu4.getPath() == null && ck3.getPath() == null && hoi4.getPath() == null && stellaris.getPath() == null) {
            GuiErrorReporter.showSimpleErrorMessage("""
No supported or compatible Paradox game has been detected.
To fix this, you can set the installation directories of games manually in the settings menu.

Note that you can't do anything useful with the Pdx-Unlimiter until at least one installation is set.
                            """);
        }

        if (ck3toeu4Dir != null) {
            if (!Files.exists(ck3toeu4Dir.resolve("CK3toEU4"))) {
                GuiErrorReporter.showSimpleErrorMessage("Invalid converter directory");
                ck3toeu4Dir = null;
            }
        }
    }

    public void setCk3toEu4Dir(Path ck3toeu4Dir) {
        this.ck3toeu4Dir = ck3toeu4Dir;
    }

    public Optional<Path> getCk3toEu4Dir() {
        return Optional.ofNullable(ck3toeu4Dir);
    }

    public boolean confirmDeletion() {
        return confirmDeletion;
    }

    public boolean deleteOnImport() {
        return deleteOnImport;
    }
}
