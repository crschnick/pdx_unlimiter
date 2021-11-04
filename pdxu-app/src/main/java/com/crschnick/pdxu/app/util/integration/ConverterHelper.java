package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.dialog.GuiConverterConfig;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ConverterHelper {

    private static void writeLine(BufferedWriter w, String key, Object value) throws IOException {
        w.write(key + " = \"" + value.toString() + "\"\n");
    }

    public static Map<String, String> loadConfig() {
        Map<String, String> map = new HashMap<>();
        var config = Settings.getInstance().ck3toeu4Dir.getValue()
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

    public static String getOutputName(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        var s = SavegameStorage.<Ck3Tag, Ck3SavegameInfo>get(Game.CK3)
                .getValidOutputFileName(entry, true, null);
        var name = FilenameUtils.getBaseName(s.toString());
        name = name.replace(" ", "_");
        return name;
    }

    public static String getEu4ModDir() {
        return GameInstallation.ALL.get(Game.EU4).getUserDir().resolve("mod").toString();
    }

    public static String getModOutputPath(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        return Settings.getInstance().ck3toeu4Dir.getValue()
                .resolve("CK3toEU4").resolve("output").resolve(getOutputName(entry)).toString();
    }

    public static void writeConfig(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry, Map<String, String> values) throws IOException {
        var config = Settings.getInstance().ck3toeu4Dir.getValue()
                .resolve("CK3toEU4").resolve("configuration.txt");
        FileUtils.forceMkdirParent(config.toFile());

        var writer = Files.newBufferedWriter(config);
        writeLine(writer, "CK3DocDirectory", GameInstallation.ALL.get(Game.CK3).getUserDir().toString());
        writeLine(writer, "CK3directory", GameInstallation.ALL.get(Game.CK3).getInstallDir().toString());
        writeLine(writer, "EU4directory", GameInstallation.ALL.get(Game.EU4).getInstallDir().toString());
        writeLine(writer, "targetGameModPath", getEu4ModDir());
        writeLine(writer, "SaveGame", SavegameStorage.ALL.get(Game.CK3).getSavegameFile(entry).toString());
        writeLine(writer, "output_name", getOutputName(entry));
        for (var e : values.entrySet()) {
            writeLine(writer, e.getKey(), e.getValue());
        }
        writer.close();
    }

    public static void convertCk3ToEu4(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        if (Settings.getInstance().ck3toeu4Dir.getValue() == null) {
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

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                ConverterHelper.writeConfig(entry, values);

                var handle = new ProcessBuilder(Settings.getInstance().ck3toeu4Dir.getValue()
                        .resolve("CK3toEU4")
                        .resolve("CK3ToEU4Converter" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")).toString())
                        .directory(Settings.getInstance().ck3toeu4Dir.getValue().resolve("CK3toEU4").toFile())
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

                    if (returnCode == 0) {
                        var latestDir = Files.list(Settings.getInstance().ck3toeu4Dir.getValue()
                                .resolve("CK3toEU4").resolve("output"))
                                .filter(Files::isDirectory)
                                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                        var latestFile = Files.list(Settings.getInstance().ck3toeu4Dir.getValue()
                                .resolve("CK3toEU4").resolve("output"))
                                .filter(Files::isRegularFile)
                                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                        if (latestDir.isPresent() && latestFile.isPresent()) {
                            var outDir = Path.of(getEu4ModDir()).resolve(latestDir.get().getFileName());
                            if (Files.exists(outDir)) {
                                GuiConverterConfig.showAlreadyExistsDialog(latestDir.get().getFileName().toString());
                            } else {
                                FileUtils.moveDirectory(
                                        latestDir.get().toFile(),
                                        outDir.toFile());
                                FileUtils.moveFile(
                                        latestFile.get().toFile(),
                                        Path.of(getEu4ModDir()).resolve(latestFile.get().getFileName()).toFile());
                            }
                        }
                    }
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                }

            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }
}
