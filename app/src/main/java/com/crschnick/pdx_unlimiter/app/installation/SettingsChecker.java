package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.Ck3Installation;
import com.crschnick.pdx_unlimiter.app.game.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.game.Hoi4Installation;
import com.crschnick.pdx_unlimiter.app.game.StellarisInstallation;
import com.crschnick.pdx_unlimiter.app.gui.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.gui.GuiSettings;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;

import java.nio.file.Files;

public class SettingsChecker {

    private static void showInstallErrorMessage(Exception e, String game) {
        String msg = e.getClass().getSimpleName() + ": " + e.getMessage() +
                ".\n\n" + game + " support has been disabled.\n" +
                "If you believe that your installation is valid, " +
                "please check in the settings menu whether the installation directory was correctly set.";
        GuiErrorReporter.showSimpleErrorMessage(
                "An error occured while loading your " + game + " installation:\n" + msg);
    }

    private static Settings newS;
    private static Settings newValidatedS;

    public static void onSettingsChange(Settings newS, Settings newValidatedS) {
        SettingsChecker.newS = newS;
        SettingsChecker.newValidatedS = newValidatedS;
    }

    public static void checkSettings() {
        ThreadHelper.create("settings checker", false, () -> {
            if (newS.getEu4().isPresent() && newValidatedS.getEu4().isEmpty()) {
                try {
                    new Eu4Installation(newS.getEu4().get()).loadData();
                } catch (Exception e) {
                    showInstallErrorMessage(e, "EU4");
                }
            }
            if (newS.getHoi4().isPresent() && newValidatedS.getHoi4().isEmpty()) {
                try {
                    new Hoi4Installation(newS.getHoi4().get()).loadData();
                } catch (Exception e) {
                    showInstallErrorMessage(e, "HOI4");
                }
            }
            if (newS.getCk3().isPresent() && newValidatedS.getCk3().isEmpty()) {
                try {
                    new Ck3Installation(newS.getCk3().get()).loadData();
                } catch (Exception e) {
                    showInstallErrorMessage(e, "CK3");
                }
            }
            if (newS.getStellaris().isPresent() && newValidatedS.getStellaris().isEmpty()) {
                try {
                    new StellarisInstallation(newS.getStellaris().get()).loadData();
                } catch (Exception e) {
                    showInstallErrorMessage(e, "Stellaris");
                }
            }

            if (newS.getCk3toEu4Dir().isPresent() && newValidatedS.getCk3toEu4Dir().isEmpty()) {
                GuiErrorReporter.showSimpleErrorMessage("Invalid converter directory");
            }

            if (newValidatedS.hasNoValidInstallation()) {
                GuiErrorReporter.showSimpleErrorMessage("""
                        No supported or compatible Paradox game has been detected.
                        To fix this, you can set the installation directories of games manually in the settings menu.

                        Note that you can't do anything useful with the Pdx-Unlimiter until at least one installation is set.
                                                                        """);
                Platform.runLater(GuiSettings::showSettings);
            }
        }).start();
    }
}
