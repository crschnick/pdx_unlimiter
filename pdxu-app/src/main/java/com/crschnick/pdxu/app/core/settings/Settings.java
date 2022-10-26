package com.crschnick.pdxu.app.core.settings;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.gui.dialog.GuiSettings;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.lang.Language;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.util.integration.Eu4SeHelper;
import com.crschnick.pdxu.app.util.integration.IronyHelper;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Settings extends AbstractSettings {

    private static Settings INSTANCE;

    public final SettingsEntry.GameDirectory eu4 = new SettingsEntry.GameDirectory(
            "eu4",
            Game.EU4);
    public final SettingsEntry.GameDirectory hoi4 = new SettingsEntry.GameDirectory(
            "hoi4",
            Game.HOI4);
    public final SettingsEntry.GameDirectory ck3 = new SettingsEntry.GameDirectory(
            "ck3",
            Game.CK3);
    public final SettingsEntry.GameDirectory stellaris = new SettingsEntry.GameDirectory(
            "stellaris",
            Game.STELLARIS);
    public final SettingsEntry.GameDirectory ck2 = new SettingsEntry.GameDirectory(
            "ck2",
            Game.CK2);
    public final SettingsEntry.GameDirectory vic2 = new SettingsEntry.GameDirectory(
            "vic2",
            Game.VIC2);
    public final SettingsEntry.GameDirectory vic3 = new SettingsEntry.GameDirectory(
            "vic3",
            Game.VIC3);

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
    public final SettingsEntry.BooleanEntry playSoundOnBackgroundImport = new SettingsEntry.BooleanEntry(
            "PLAY_SOUND_ON_BACKGROUND_IMPORT",
            "playSoundOnBackgroundImport",
            true
    );
    public final SettingsEntry.BooleanEntry confirmDeletion = new SettingsEntry.BooleanEntry(
            "CONFIRM_DELETION",
            "confirmDeletion",
            true
    );
    public final SettingsEntry.BooleanEntry importOnGameNormalExit = new SettingsEntry.BooleanEntry(
            "IMPORT_ON_NORMAL_GAME_EXIT",
            "importOnNormalGameExit",
            false
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
            boolean changedToTrue = !newValue.equals(value.get()) && newValue;

            Path eu4seFile = PdxuInstallation.getInstance().getSettingsLocation().resolve("eu4saveeditor");
            try {
                Files.writeString(eu4seFile, Boolean.toString(newValue));
                super.set(newValue);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            if (changedToTrue) {
                Eu4SeHelper.showEnabledDialog();
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
    public final SettingsEntry.StringEntry skanderbegApiKey = new SettingsEntry.StringEntry(
            "SKANDERBEG_API_KEY",
            "skanderbegApiKey",
            null,
            true
    );
    public final SettingsEntry.StorageDirectory storageDirectory = new SettingsEntry.StorageDirectory(
            "STORAGE_DIR",
            "storageDirectory"
    );
    public final SettingsEntry.ThirdPartyDirectory ck3toeu4Dir = new SettingsEntry.ThirdPartyDirectory(
            "CK3_TO_EU4_DIR",
            "ck3toeu4Dir",
            "CK3 to EU4 converter",
            Path.of("Ck3ToEu4", "CK3ToEU4Converter.exe"),
            () -> null
    );
    public final SettingsEntry.IntegerEntry maxTooltipWidth = new SettingsEntry.IntegerEntry(
            "MAX_TOOLTIP_SIZE",
            "maxTooltipWidth",
            600,
            100,
            2000
    );
    public final SettingsEntry.ThirdPartyDirectory ironyDir = new SettingsEntry.ThirdPartyDirectory(
            "IRONY_DIR",
            "ironyDir",
            "Irony Mod Manager",
            Path.of("IronyModManager.exe"),
            () -> IronyHelper.getIronyDefaultInstallPath().orElse(null)
    );
    public final SettingsEntry.BooleanEntry enabledTimedImports = new SettingsEntry.BooleanEntry(
            "TIMED_IMPORTS",
            "enabledTimedImports",
            false
    );
    public final SettingsEntry.IntegerEntry timedImportsInterval = new SettingsEntry.IntegerEntry(
            "TIMED_IMPORTS_INTERVAL",
            "timedImportsInterval",
            15,
            1,
            60
    );
    public final SettingsEntry.ChoiceEntry<Language> language = new SettingsEntry.ChoiceEntry<>(
            "LANGUAGE",
            "language",
            LanguageManager.DEFAULT,
            LanguageManager.getInstance().getLanguages().inverseBidiMap(),
            l -> l.getDisplayName()
    );
    public final SettingsEntry.BooleanEntry useGameLanguage = new SettingsEntry.BooleanEntry(
            "USE_GAME_LANGUAGE",
            "useGameLanguage",
            false
    );

    public static void init() {
        INSTANCE = new Settings();
        INSTANCE.load();
    }

    public static Settings getInstance() {
        return INSTANCE;
    }

    @Override
    protected String createName() {
        return "settings";
    }

    @Override
    public void check() {
        boolean hasNoValidInstallation =
                eu4.getValue() == null && ck3.getValue() == null &&
                        hoi4.getValue() == null && stellaris.getValue() == null &&
                        ck2.getValue() == null && vic2.getValue() == null && vic3.getValue() == null;
        if (hasNoValidInstallation) {
            var res = GuiErrorReporter.showSimpleErrorMessage("""
                    Welcome to the Pdx-Unlimiter!
                                            
                    The automatic game detection did not detect any supported Paradox game.
                    To get started, you can set the installation directories of games manually in the settings menu.

                    Note that you can't do anything useful with the Pdx-Unlimiter until at least one installation is set.
                                        """);
            if (res) {
                Platform.runLater(GuiSettings::showSettings);
            }
        }

        // Disable irony if needed
        if (ironyDir.getValue() == null) {
            launchIrony.set(false);
        }
    }
}
