package com.crschnick.pdx_unlimiter.app.core.settings;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.game.Ck3Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.Hoi4Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.StellarisInstallation;
import com.crschnick.pdx_unlimiter.app.util.ConfigHelper;
import com.crschnick.pdx_unlimiter.app.util.Eu4SeHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class Settings {

    private static Settings INSTANCE;

    private InstallDirectory eu4;
    private InstallDirectory hoi4;
    private InstallDirectory ck3;
    private InstallDirectory stellaris;

    public final SettingsEntry.IntegerEntry fontSize = new SettingsEntry.IntegerEntry(
            "FONT_SIZE",
            "fontSize",
            12,
            10,
            20
    );

    public final SettingsEntry.BooleanEntry deleteOnImport = new SettingsEntry.BooleanEntry(
            "DELETE_ON_IMPORT",
            "deleteOnImport",
            false
    );

    public final SettingsEntry.BooleanEntry startSteam = new SettingsEntry.BooleanEntry(
            "START_STEAM",
            "startSteam",
            true
    );

    public final SettingsEntry.BooleanEntry confirmDeletion = new SettingsEntry.BooleanEntry(
            "CONFIRM_DELETION",
            "confirmDeletion",
            true
    );

    public final SettingsEntry.BooleanEntry launchIrony = new SettingsEntry.BooleanEntry(
            "LAUNCH_IRONY",
            "launchIrony",
            false
    );

    public final SettingsEntry.BooleanEntry enableEu4SaveEditor = new SettingsEntry.BooleanEntry(
            "ENABLE_EU4SE",
            "enableEu4SaveEditor",
            false
    ) {
        @Override
        public void set(Boolean newValue) {
            if (newValue) {
                Eu4SeHelper.showEnabledDialog();
            }

            Path eu4seFile = PdxuInstallation.getInstance().getSettingsLocation().resolve("eu4saveeditor");
            try {
                Files.writeString(eu4seFile, Boolean.toString(newValue));
                super.set(newValue);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
    };

    public final SettingsEntry.BooleanEntry enableAutoUpdate = new SettingsEntry.BooleanEntry(
            "ENABLE_AUTOUPDATE",
            "enableAutoUpdate",
            true
    ) {
        @Override
        public void set(Boolean newValue) {
            Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("update");
            try {
                Files.writeString(file, Boolean.toString(newValue));
                super.set(newValue);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
    };

    public final SettingsEntry.StringEntry rakalyUserId = new SettingsEntry.StringEntry(
            "RAKALY_USER_ID",
            "rakalyUserId",
            null
    );

    public final SettingsEntry.StringEntry rakalyApiKey = new SettingsEntry.StringEntry(
            "RAKALY_API_KEY",
            "rakalyApiKey",
            null
    );

    public final SettingsEntry.StringEntry skanderbegApiKey = new SettingsEntry.StringEntry(
            "SKANDERBEG_API_KEY",
            "skanderbegApiKey",
            null
    );

    public final SettingsEntry.StorageDirectory storageDirectory = new SettingsEntry.StorageDirectory(
            "STORAGE_DIR",
            "storageDirectory"
    );

    public final SettingsEntry.ThirdPartyDirectory ck3toeu4Dir = new SettingsEntry.ThirdPartyDirectory(
            "CK3_TO_EU4_DIR",
            "ck3toeu4Dir",
            Path.of("Ck3ToEu4", "CK3ToEU4Converter.exe")
    );

    public final SettingsEntry.ThirdPartyDirectory ironyDir = new SettingsEntry.ThirdPartyDirectory(
            "IRONY_DIR",
            "ironyDir",
            Path.of("IronyModManager.exe")
    );


    public static void init() {
        Settings loaded = loadConfig();
        INSTANCE = loaded;
        INSTANCE.validate();
        SettingsChecker.onSettingsChange(loaded, loaded, INSTANCE);
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

        var oldSettings = INSTANCE.copy();
        INSTANCE = newS.copy();
        INSTANCE.validate();
        SettingsChecker.onSettingsChange(oldSettings, newS, INSTANCE);

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

    public static Settings loadConfig() {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("settings.json");
        JsonNode sNode;
        if (Files.exists(file)) {
            JsonNode node = ConfigHelper.readConfig(file);
            sNode = Optional.ofNullable(node.get("settings")).orElse(JsonNodeFactory.instance.objectNode());
        } else {
            sNode = JsonNodeFactory.instance.objectNode();
        }

        Settings s = new Settings();
        for (var field : Settings.class.getFields()) {
            try {
                SettingsEntry<?> e = (SettingsEntry<?>) field.get(s);
                var node = sNode.get(e.getSerializationName());
                if (node != null) {
                    e.set(node);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
        return s;
    }

    public static void saveConfig() throws IOException {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("settings.json");
        FileUtils.forceMkdirParent(file.toFile());

        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ObjectNode i = n.putObject("settings");

        Settings s = Settings.INSTANCE;
        for (var field : Settings.class.getFields()) {
            try {
                SettingsEntry<?> e = (SettingsEntry<?>) field.get(s);
                var node = e.toNode();
                if (node != null) {
                    i.set(e.getSerializationName(), node);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public boolean hasNoValidInstallation() {
        return eu4.getPath() == null && ck3.getPath() == null && hoi4.getPath() == null && stellaris.getPath() == null;
    }
}
