package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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
        var config = Settings.getInstance().ck3toeu4Dir.getValue()
                .resolve("CK3toEU4").resolve("configuration.txt");
        if (!Files.exists(config)) {
            return map;
        }

        try ( var reader = Files.newBufferedReader(config)) {
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
}
