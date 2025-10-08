package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.gui.dialog.GuiConverterConfig;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.dist.SteamDist;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.prefs.AppPrefs;
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
import java.util.*;
import java.util.function.Supplier;

@FieldDefaults(
        makeFinal = true,
        level = AccessLevel.PRIVATE
)
@AllArgsConstructor
@Getter
public class ConverterSupport {

    public static final ConverterSupport CK3_TO_EU4 = new ConverterSupport(
            Game.CK3, Game.EU4, "CK3", "EU4", () -> AppPrefs.get().ck3toeu4Directory().getValue(), "https://github.com/ParadoxGameConverters/CK3toEU4/releases",
            "D5A23363-3092-48F1-8838-7AC4B2B5BBC5", true
    );

    public static final ConverterSupport EU4_TO_VIC3 = new ConverterSupport(
            Game.EU4, Game.VIC3, "EU4", "VIC3", () -> AppPrefs.get().eu4tovic3Directory().getValue(), "https://github.com/ParadoxGameConverters/EU4ToVic3/releases",
            "38314A5E-B83C-4C08-B031-F4596A091C11", false
    ) {
        @Override
        public String getToConfigurationName() {
            return "Vic3";
        }
    };

    public static final ConverterSupport VIC3_TO_HOI4 = new ConverterSupport(
            Game.VIC3, Game.HOI4, "VIC3", "HOI4", () -> AppPrefs.get().vic3tohoi4Directory().getValue(), "https://github.com/ParadoxGameConverters/Vic3ToHoI4/releases",
            "820C5B58-D3E7-4BCA-A9E4-4805A4A6CFA1", true
    ) {
        @Override
        public String getFromConfigurationName() {
            return "Vic3";
        }

        @Override
        protected void removePathKeys(Map<String, String> map) {
            super.removePathKeys(map);
            map.remove(getToConfigurationName() + "directory");
            map.remove(getFromConfigurationName() + "SteamModDirectory");
            map.remove("sourceGameModPath");
        }

        @Override
        public String getToConfigurationName() {
            return "HoI4";
        }

        public Path getExecutable() {
            return getBackendDir().resolve("Vic3ToHoi4" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        }

        protected void writePaths(BufferedWriter writer, SavegameEntry<?, ?> entry) throws IOException {
            writeLine(writer, getToConfigurationName() + "directory", GameInstallation.ALL.get(getToGame()).getInstallDir().toString());
            writeLine(writer, getFromConfigurationName() + "SteamModDirectory", ((SteamDist) GameInstallation.ALL.get(getFromGame()).getDist()).getWorkshopDir().orElseThrow().toString());
            writeLine(writer, getFromConfigurationName() + "directory", GameInstallation.ALL.get(getFromGame()).getInstallDir().toString());
            writeLine(writer, "sourceGameModPath", GameInstallation.ALL.get(getFromGame()).getUserDir().resolve("mod"));
            writeLine(writer, "targetGameModPath", getTargetModDir());
            writeLine(writer, "SaveGame", SavegameStorage.ALL.get(getFromGame()).getSavegameFile(entry).toString());
            writeLine(writer, "output_name", getOutputName(entry));
        }
    };

    public static final List<ConverterSupport> ALL = List.of(CK3_TO_EU4, EU4_TO_VIC3, VIC3_TO_HOI4);

    Game fromGame;
    Game toGame;

    String fromName;
    String toName;

    Supplier<Path> directorySetting;

    String downloadLink;
    String guid;

    boolean hasModFile;

    public String getFromConfigurationName() {
        return fromName;
    }

    public Optional<Path> determineInstallationDirectory() {
        if (OsType.ofLocal() != OsType.WINDOWS) {
            return Optional.empty();
        }

        try {
            var dir = WindowsRegistry.of().readStringValueIfPresent(
                    WindowsRegistry.HKEY_CURRENT_USER,
                    String.format("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\{%s}_is1", guid),
                    "Inno Setup: App Path"
            );
            return dir.map(s -> Path.of(s)).filter(s -> Files.exists(s));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public String getToConfigurationName() {
        return fromName;
    }

    public String getName() {
        return fromName + "to" + toName;
    }

    public Path getBaseDir() {
        return directorySetting.get();
    }

    public Path getBackendDir() {
        return directorySetting.get().resolve(getName());
    }

    public Path getWorkingDir() {
        return getBackendDir();
    }

    public Path getExecutable() {
        return getBackendDir().resolve(getName() + "Converter" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
    }

    public Map<String, String> loadConfig() {
        Map<String, String> map = new HashMap<>();
        var config = directorySetting.get()
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
            ErrorEventFactory.fromThrowable(e).handle();
        }
        removePathKeys(map);
        return map;
    }

    protected void removePathKeys(Map<String, String> map) {
        map.remove(getFromConfigurationName() + "DocDirectory");
        map.remove(getFromConfigurationName() + "directory");
        map.remove(getToConfigurationName() + "directory");
        map.remove("targetGameModPath");
        map.remove("SaveGame");
        map.remove("output_name");
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
        return directorySetting.get()
                .resolve(getName()).resolve("output").resolve(getOutputName(entry)).toString();
    }

    private static void writeLine(BufferedWriter w, String key, Object value) throws IOException {
        w.write(key + " = \"" + value.toString() + "\"\n");
    }

    public void writeConfig(SavegameEntry<?, ?> entry, Map<String, String> values) throws IOException {
        var config = directorySetting.get()
                .resolve(getName()).resolve("configuration.txt");
        FileUtils.forceMkdirParent(config.toFile());

        var writer = Files.newBufferedWriter(config);
        writePaths(writer, entry);
        for (var e : values.entrySet()) {
            writeLine(writer, e.getKey(), e.getValue());
        }
        writer.close();
    }

    protected void writePaths(BufferedWriter writer, SavegameEntry<?, ?> entry) throws IOException {
        writeLine(writer, getFromConfigurationName() + "DocDirectory", GameInstallation.ALL.get(fromGame).getUserDir().toString());
        writeLine(writer, getFromConfigurationName() + "directory", GameInstallation.ALL.get(fromGame).getInstallDir().toString());
        writeLine(writer, getToConfigurationName() + "directory", GameInstallation.ALL.get(toGame).getInstallDir().toString());
        writeLine(writer, "targetGameModPath", getTargetModDir());
        writeLine(writer, "SaveGame", SavegameStorage.ALL.get(fromGame).getSavegameFile(entry).toString());
        writeLine(writer, "output_name", getOutputName(entry));
    }

    public void convert(SavegameEntry<?, ?> entry) {
        var gui = new GuiConverterConfig(this);
        if (directorySetting.get() == null) {
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
                        Optional<Path> latestDir;
                        try (var s = Files.list(getBackendDir().resolve("output"))) {
                            latestDir = s.filter(Files::isDirectory)
                                         .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                        }

                        Optional<Path> latestFile;
                        try (var s = Files.list(getBackendDir().resolve("output"))) {
                            latestFile = s.filter(Files::isRegularFile)
                                    .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                        }

                        if (latestDir.isPresent() && (!hasModFile || latestFile.isPresent())) {
                            var outDir = Path.of(getTargetModDir()).resolve(latestDir.get().getFileName());
                            if (Files.exists(outDir)) {
                                FileUtils.deleteDirectory(outDir.toFile());
                            }
                            FileUtils.moveDirectory(
                                    latestDir.get().toFile(),
                                    outDir.toFile()
                            );

                            if (hasModFile) {
                                var outFile = Path.of(getTargetModDir()).resolve(latestFile.get().getFileName());
                                Files.deleteIfExists(outFile);
                                FileUtils.moveFile(
                                        latestFile.get().toFile(),
                                        outFile.toFile()
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    ErrorEventFactory.fromThrowable(e).handle();
                }

            } catch (IOException e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }
        }, true);
    }
}
