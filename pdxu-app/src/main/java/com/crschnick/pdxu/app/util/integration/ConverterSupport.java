package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.core.settings.SettingsEntry;
import com.crschnick.pdxu.app.gui.dialog.GuiConverterConfig;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import javafx.application.Platform;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(
        makeFinal = true,
        level = AccessLevel.PRIVATE
)
@AllArgsConstructor
@Getter
public class ConverterSupport {

    public static final ConverterSupport CK3_TO_EU4 = new ConverterSupport(
            Game.CK3, Game.EU4, "CK3", "EU4", Settings.getInstance().ck3toeu4Dir, "https://github.com/ParadoxGameConverters/CK3toEU4/releases");

    public static final List<ConverterSupport> ALL = List.of(CK3_TO_EU4);

    Game fromGame;
    Game toGame;

    String fromName;
    String toName;

    SettingsEntry.ThirdPartyDirectory directorySetting;

    String downloadLink;

    public String getName() {
        return fromName + "to" + toName;
    }

    public Path getBaseDir() {
        return directorySetting.getValue();
    }

    public Path getBackendDir() {
        return directorySetting.getValue().resolve(getName());
    }

    public Path getWorkingDir() {
        return getBackendDir();
    }

    public Path getExecutable() {
        return getBackendDir().resolve(getName() + "Converter" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
    }

    public Map<String, String> loadConfig() {
        Map<String, String> map = new HashMap<>();
        var config = directorySetting.getValue()
                .resolve(getName()).resolve("configuration.txt");
        if (!Files.exists(config)) {
            return map;
        }

        try (var reader = Files.newBufferedReader(config)) {
            String line;
            while ((line = reader.readLine()) != null) {
                var split = line.split(" = ");
                map.put(split[0], split[1].replace("\"", ""));
            }
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
        map.remove(fromName + "DocDirectory");
        map.remove(fromName + "directory");
        map.remove(toName + "directory");
        map.remove("targetGameModPath");
        map.remove("SaveGame");
        map.remove("output_name");
        return map;
    }

    public String getOutputName(SavegameEntry<?, ?> entry) {
        var s = SavegameStorage.get(fromGame)
                .getValidOutputFileName(entry, true, null);
        var name = FilenameUtils.getBaseName(s.toString());
        name = name.replace(" ", "_");
        return name;
    }

    public String getTargetModDir() {
        return GameInstallation.ALL.get(toGame).getUserDir().resolve("mod").toString();
    }

    public String getGeneratedModOutputPath(SavegameEntry<?, ?> entry) {
        return directorySetting.getValue()
                .resolve(getName()).resolve("output").resolve(getOutputName(entry)).toString();
    }

    private static void writeLine(BufferedWriter w, String key, Object value) throws IOException {
        w.write(key + " = \"" + value.toString() + "\"\n");
    }

    public void writeConfig(SavegameEntry<?, ?> entry, Map<String, String> values) throws IOException {
        var config = directorySetting.getValue()
                .resolve(getName()).resolve("configuration.txt");
        FileUtils.forceMkdirParent(config.toFile());

        var writer = Files.newBufferedWriter(config);
        writeLine(writer, fromName + "DocDirectory", GameInstallation.ALL.get(fromGame).getUserDir().toString());
        writeLine(writer, fromName + "directory", GameInstallation.ALL.get(fromGame).getInstallDir().toString());
        writeLine(writer, toName + "directory", GameInstallation.ALL.get(toGame).getInstallDir().toString());
        writeLine(writer, "targetGameModPath", getTargetModDir());
        writeLine(writer, "SaveGame", SavegameStorage.ALL.get(fromGame).getSavegameFile(entry).toString());
        writeLine(writer, "output_name", getOutputName(entry));
        for (var e : values.entrySet()) {
            writeLine(writer, e.getKey(), e.getValue());
        }
        writer.close();
    }

    public void convert(SavegameEntry<?, ?> entry) {
        var gui = new GuiConverterConfig(this);
        if (directorySetting.getValue() == null) {
            gui.showUsageDialog();
            return;
        }

        if (!gui.showConfirmConversionDialog()) {
            return;
        }

        var values = loadConfig();
        if (!gui.showConfig(values)) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                writeConfig(entry, values);

                var handle = new ProcessBuilder(getExecutable().toString())
                        .directory(getWorkingDir().toFile())
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start();

                try {
                    int returnCode = handle.waitFor();

                    Platform.runLater(() -> {
                        if (returnCode == 0) {
                            gui.showConversionSuccessDialog();
                        } else {
                            gui.showConversionErrorDialog();
                        }
                    });

                    if (returnCode == 0) {
                        var latestDir = Files.list(getBackendDir().resolve("output"))
                                .filter(Files::isDirectory)
                                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                        var latestFile = Files.list(getBackendDir().resolve("output"))
                                .filter(Files::isRegularFile)
                                .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                        if (latestDir.isPresent() && latestFile.isPresent()) {
                            var outDir = Path.of(getTargetModDir()).resolve(latestDir.get().getFileName());
                            var outFile = Path.of(getTargetModDir()).resolve(latestFile.get().getFileName());
                            Files.deleteIfExists(outFile);
                            if (Files.exists(outDir)) {
                                FileUtils.deleteDirectory(outDir.toFile());
                            }

                            FileUtils.moveDirectory(
                                    latestDir.get().toFile(),
                                    outDir.toFile()
                            );
                            FileUtils.moveFile(
                                    latestFile.get().toFile(),
                                    outFile.toFile()
                            );
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
