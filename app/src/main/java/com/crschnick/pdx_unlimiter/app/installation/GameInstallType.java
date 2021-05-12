package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.lang.Language;
import com.crschnick.pdx_unlimiter.app.lang.LanguageManager;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.OsHelper;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.GameVersion;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface GameInstallType {

    GameInstallType EU4 = new StandardInstallType("eu4") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            // Prefer launcher image!
            var launcherBg = p.resolve("launcher-assets").resolve("app-background.png");
            if (Files.exists(launcherBg)) {
                return launcherBg;
            }

            return null;
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)\\.\\w+\\s.+")
                    .matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        m.group(5)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            var sgPath = FilenameUtils.separatorsToUnix(userDir.relativize(path).toString());
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", name)
                    .put("desc", "")
                    .put("date", lastPlayed.toString())
                    .put("filename", sgPath);
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("launcher-assets").resolve("icon.png");
        }
    };

    GameInstallType HOI4 = new StandardInstallType("hoi4") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(8) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("(\\w+)\\s+v(\\d)\\.(\\d+)\\.(\\d+)").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        0,
                        m.group(1)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug");
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", name)
                    .put("desc", "")
                    .put("date", d.format(new Date(lastPlayed.toEpochMilli())) + "\n")
                    .put("filename", userDir.resolve("save games").relativize(path).toString())
                    .put("is_remote", false);
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("launcher-assets").resolve("game-icon.png");
        }

        @Override
        public String getModId(Path userDir, GameMod mod) {
            return mod.getName();
        }
    };

    GameInstallType STELLARIS = new StandardInstallType("stellaris") {
        @Override
        public Path getWindowsStoreLauncherDataPath(Path p) {
            return p.resolve("launcher");
        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("gfx").resolve("exe_icon.bmp");
        }

        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(16) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("(\\w)+\\s+v?(\\d)\\.(\\d+)\\.(\\d+).+").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        0,
                        m.group(1)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.relativize(path).toString()));
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", name)
                    .put("date", "");
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }
    };

    GameInstallType CK3 = new StandardInstallType("binaries/ck3") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            String[] bgs = new String[]{"assassin", "baghdad", "castle", "council", "duel"};
            return p.resolve("game").resolve("gfx").resolve("interface").resolve("illustrations")
                    .resolve("loading_screens").resolve(bgs[new Random().nextInt(bgs.length)] + ".dds");
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug_mode");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("(\\d)\\.(\\d+)\\.(\\d+)\\s+\\((\\w+)\\)").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        0,
                        m.group(4)));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Path getDlcPath(Path p) {
            return p.resolve("game").resolve("dlc");
        }

        @Override
        public Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("pdx_settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.textFileParser().parse(sf);
            var langId = node.getNodeForKey("\"System\"")
                    .getNodeForKey("\"language\"").getNodeForKey("value").getString();
            return Optional.ofNullable(LanguageManager.getInstance().byId(langId));
        }

        public Path getSteamSpecificFile(Path p) {
            return p.resolve("binaries").resolve("steam_appid.txt");
        }

        public Path getLauncherDataPath(Path p) {
            return p.resolve("launcher");
        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("game").resolve("gfx").resolve("exe_icon.bmp");
        }

        public Path getModBasePath(Path p) {
            return p.resolve("game");
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var date = d.format(new Date(lastPlayed.toEpochMilli()));
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.resolve("save games").relativize(path).toString()));
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", "")
                    .put("date", date);
            JsonHelper.write(n, userDir.resolve("continue_game.json"));
        }
    };

    GameInstallType CK2 = new StandardInstallType("CK2game") {

        @Override
        public Optional<Path> getLegacyLauncherExecutable(Path p) {
            return Optional.of(getExecutable(p));
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug_mode");
        }

        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(20) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-skiplauncher");
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {

        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("gfx").resolve("CK2_icon.bmp");
        }

        @Override
        public Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.textFileParser().parse(sf);
            var langId = node.getNodeForKey("gui").getNodeForKey("language").getString();
            return Optional.ofNullable(LanguageManager.getInstance().byId(langId));
        }

        @Override
        public Optional<GameVersion> determineVersionFromInstallation(Path p) {
            return Optional.of(new GameVersion(3, 3, 3, 0));
        }
    };

    GameInstallType VIC2 = new StandardInstallType("v2game") {

        @Override
        public Optional<Path> getLegacyLauncherExecutable(Path p) {
            return Optional.of(p.resolve("victoria2.exe"));
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.empty();
        }

        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(7) + 1;
            return p.resolve("gfx").resolve("loadingscreens").resolve("load_" + i + ".dds");
        }

        @Override
        public Path getSteamSpecificFile(Path p) {
            return p.resolve("42960_install.vdf");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of();
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException {

        }

        @Override
        public Path determineUserDir(Path p, String name) throws IOException {
            var userDirFile = p.resolve("userdir.txt");
            if (Files.exists(userDirFile)) {
                var s = Files.readString(userDirFile).trim();
                if (!s.isEmpty()) {
                    return Path.of(Files.readString(userDirFile));
                }
            }

            return p;
        }

        @Override
        public Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.textFileParser().parse(sf);
            var langId = node.getNodeForKey("gui").getNodeForKey("language").getString();
            return Optional.ofNullable(LanguageManager.getInstance().byId(langId));
        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("load_bg.jpg");
        }

        @Override
        public Optional<GameVersion> determineVersionFromInstallation(Path p) {
            return Optional.of(new GameVersion(3, 4, 0, 0));
        }
    };


    Path chooseBackgroundImage(Path p);

    default Optional<GameVersion> determineVersionFromInstallation(Path p) {
        return Optional.empty();
    }

    List<String> getLaunchArguments();

    Path getExecutable(Path p);

    default Optional<GameVersion> getVersion(String versionString) {
        throw new UnsupportedOperationException();
    }

    default Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
        return Optional.empty();
    }

    default Path getDlcPath(Path p) {
        return p.resolve("dlc");
    }

    default String getModId(Path userDir, GameMod mod) {
        return userDir.relativize(mod.getModFile()).toString();
    }

    void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path) throws IOException;

    default Path getSteamSpecificFile(Path p) {
        return p.resolve("steam_appid.txt");
    }

    default Path getLauncherDataPath(Path p) {
        return p;
    }

    default Path getWindowsStoreLauncherDataPath(Path p) {
        return getLauncherDataPath(p);
    }

    Path getIcon(Path p);

    default Path getWindowsStoreIcon(Path p) {
        return p.resolve("Square150x150Logo.scale-100.png");
    }

    default Path getModBasePath(Path p) {
        return p;
    }

    default Optional<Path> getLegacyLauncherExecutable(Path p) {
        return Optional.empty();
    }

    default Optional<String> debugModeSwitch() {
        return Optional.empty();
    }

    default Path determineUserDir(Path p, String name) throws IOException {
        var userDirFile = p.resolve("userdir.txt");
        if (Files.exists(userDirFile)) {
            var s = Files.readString(userDirFile).trim();
            if (!s.isEmpty()) {
                return Path.of(Files.readString(userDirFile));
            }
        }

        return OsHelper.getUserDocumentsPath().resolve("Paradox Interactive").resolve(name);
    }

    abstract class StandardInstallType implements GameInstallType {

        private final String executableName;

        public StandardInstallType(String executableName) {
            this.executableName = executableName;
        }

        @Override
        public Path getExecutable(Path p) {
            return p.resolve(executableName + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        }

        @Override
        public Optional<Language> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.textFileParser().parse(sf);
            var langId = node.getNodeForKey("language").getString();
            return Optional.ofNullable(LanguageManager.getInstance().byId(langId));
        }
    }
}
