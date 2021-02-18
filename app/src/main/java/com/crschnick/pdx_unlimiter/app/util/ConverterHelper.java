package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.Settings;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiConverterConfig;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import javafx.application.Platform;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConverterHelper {

    private static void writeLine(BufferedWriter w, String key, Object value) throws IOException {
        w.write(key + " = \"" + value.toString() + "\"\n");
    }

    public static Map<String, String> loadConfig() {
        Map<String, String> map = new HashMap<>();
        var config = Settings.getInstance().getCk3toEu4Dir().get()
                .resolve("CK3toEU4").resolve("configuration.txt");
        if (!Files.exists(config)) {
            return map;
        }

        try {
            var reader = Files.newBufferedReader(config);
            String line;
            while ((line = reader.readLine()) != null) {
                var split = line.split(" = ");
                map.put(split[0], split[1].replace("\"", ""));
            }
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
        map.remove("CK3DocDirectory");
        map.remove("CK3directory");
        map.remove("EU4directory");
        map.remove("targetGameModPath");
        map.remove("SaveGame");
        map.remove("output_name");
        return map;
    }

    public static void writeConfig(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry, Map<String, String> values) {
        var config = Settings.getInstance().getCk3toEu4Dir().get()
                .resolve("CK3toEU4").resolve("configuration.txt");
        try {
            var writer = Files.newBufferedWriter(config);
            writeLine(writer, "CK3DocDirectory", GameInstallation.CK3.getUserPath().toString());
            writeLine(writer, "CK3directory", GameInstallation.CK3.getPath().toString());
            writeLine(writer, "EU4directory", GameInstallation.EU4.getPath().toString());
            writeLine(writer, "targetGameModPath", GameInstallation.EU4.getUserPath().resolve("mod").toString());
            writeLine(writer, "SaveGame", SavegameStorage.CK3.getSavegameFile(entry).toString());
            for (var e : values.entrySet()) {
                writeLine(writer, e.getKey(), e.getValue());
            }
            writer.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void convertCk3ToEu4(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        if (Settings.getInstance().getCk3toEu4Dir().isEmpty()) {
            GuiConverterConfig.showUsageDialog();
            return;
        }

        if (!GuiConverterConfig.showConfirmConversionDialog()) {
            return;
        }

        var values = ConverterHelper.loadConfig();
        if (!GuiConverterConfig.showConfig(values)) {
            return;
        }
        ConverterHelper.writeConfig(entry, values);

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                var handle = new ProcessBuilder(Settings.getInstance().getCk3toEu4Dir().get()
                        .resolve("CK3toEU4")
                        .resolve("CK3ToEU4Converter" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")).toString())
                        .directory(Settings.getInstance().getCk3toEu4Dir().get().resolve("CK3toEU4").toFile())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start();

                try {
                    int returnCode = handle.waitFor();
                    Platform.runLater(() -> {
                        if (returnCode == 0) {
                            GuiConverterConfig.showConversionSuccessDialog();
                        } else {
                            GuiConverterConfig.showConversionErrorDialog();
                        }
                    });
                } catch (InterruptedException ignored) {
                }

            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }
}
