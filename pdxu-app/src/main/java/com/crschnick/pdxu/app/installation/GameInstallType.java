package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.FileSystemHelper;
import com.crschnick.pdxu.app.util.JacksonMapper;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.GameVersion;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface GameInstallType {

    public static enum ModInfoStorageType {
        STORES_INFO,
        SAVEGAME_DOESNT_STORE_INFO,
        SAVEGAMES_AND_GAME_DONT_STORE_INFO
    }

    public static enum DlcInfoStorageType {
        SAVEGAME_STORES_INFO,
        SAVEGAME_DOESNT_STORE_INFO
    }

    GameInstallType EU4 = new StandardInstallType("eu4") {

        @Override
        public List<GameDlc> loadDlcs(Path directory) throws IOException {
            // Prefer builtin_dlcs in case of duplicates
            var dlcs = new ArrayList<GameDlc>();
            loadDlcsFromDirectory(directory.resolve("builtin_dlc"), dlcs);
            loadDlcsFromDirectory(getDlcPath(directory), dlcs);
            return dlcs;
        }

        @Override
        public DlcInfoStorageType getDlcInfoStorageType() {
            return DlcInfoStorageType.SAVEGAME_STORES_INFO;
        }

        public Path getSteamSpecificFile(Path p) {
            return p.resolve("EmptySteamDepot");
        }

        @Override
        public Path getWindowsStoreLauncherDataPath(Path p) {
            return p.resolve("launcher");
        }

        @Override
        public Path chooseBackgroundImage(Path p) {
            // Prefer launcher image!
            var launcherBg = getLauncherDataPath(p).resolve("launcher-assets").resolve("app-background.png");
            if (Files.exists(launcherBg)) {
                return launcherBg;
            }

            var launcherBgNew = getLauncherDataPath(p).resolve("assets").resolve("app-background.png");
            if (Files.exists(launcherBgNew)) {
                return launcherBgNew;
            }

            var launcherWindowsBg = getWindowsStoreLauncherDataPath(p).resolve("launcher-assets").resolve("app-background.png");
            if (Files.exists(launcherWindowsBg)) {
                return launcherWindowsBg;
            }

            return null;
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-continuelastsave");
        }

        @Override
        public Optional<GameVersion> getVersion(String versionString) {
            Matcher m = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)(?:\\.\\w+\\s.+)?")
                    .matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        m.group(5)
                ));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) throws IOException {
            var sgPath = FilenameUtils.separatorsToUnix(userDir.relativize(path).toString());
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", name)
                    .put("desc", "")
                    .put("date", lastPlayed.toString())
                    .put("filename", sgPath);
            JacksonMapper.getDefault().writeValue(userDir.resolve("continue_game.json").toFile(), n);
        }

        @Override
        public Path getIcon(Path p) {
            var old = p.resolve("launcher-assets").resolve("icon.png");
            if (Files.exists(old)) {
                return old;
            }

            var n = p.resolve("assets").resolve("icon.png");
            return n;
        }
    };

    GameInstallType HOI4 = new StandardInstallType("hoi4") {

        public String getDlcLauncherId(GameInstallation installation, GameDlc dlc) {
            var rel = installation.getInstallDir().relativize(dlc.getInfoFilePath());
            return FilenameUtils.separatorsToUnix(rel.toString());
        }

        public String getDlcSavegameId(GameInstallation installation, GameDlc dlc) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Path getWindowsStoreLauncherDataPath(Path p) {
            return p.resolve("launcher");
        }

        @Override
        public DlcInfoStorageType getDlcInfoStorageType() {
            return DlcInfoStorageType.SAVEGAME_DOESNT_STORE_INFO;
        }

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
                        m.group(1)
                ));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug");
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", name)
                    .put("desc", "")
                    .put("date", d.format(new Date(lastPlayed.toEpochMilli())) + "\n")
                    .put("filename", userDir.resolve("save games").relativize(path).toString())
                    .put("is_remote", false);
            JacksonMapper.getDefault().writeValue(userDir.resolve("continue_game.json").toFile(), n);
        }

        @Override
        public Path getIcon(Path p) {
            var old = p.resolve("launcher-assets").resolve("game-icon.png");
            if (Files.exists(old)) {
                return old;
            }

            var n = p.resolve("assets").resolve("game-icon.png");
            return n;
        }

        @Override
        public String getModSavegameId(GameInstallation installation, GameMod mod) {
            return mod.getName().orElse("invalid mod");
        }

        @Override
        public Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("pdx_settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.text().parse(sf);
            var langId = node
                    .getNodeForKeysIfExistent("\"System\"", "\"language\"", "value")
                    .map(Node::getString);
            return langId.flatMap(l -> Optional.ofNullable(GameLanguage.byId(l)));
        }
    };

    GameInstallType STELLARIS = new StandardInstallType("stellaris") {

        @Override
        public DlcInfoStorageType getDlcInfoStorageType() {
            return DlcInfoStorageType.SAVEGAME_STORES_INFO;
        }

        @Override
        public String getModSavegameId(GameInstallation installation, GameMod mod) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ModInfoStorageType getModInfoStorageType() {
            return ModInfoStorageType.SAVEGAME_DOESNT_STORE_INFO;
        }

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
            Matcher m = Pattern.compile("(\\w*\\s*)v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(versionString);
            if (m.find()) {
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        m.groupCount() == 5 ? Integer.parseInt(m.group(4)) : 0,
                        0, m.group(1).trim()
                ));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) throws IOException {
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.relativize(path).toString()));
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", name)
                    .put("date", "");
            JacksonMapper.getDefault().writeValue(userDir.resolve("continue_game.json").toFile(), n);
        }
    };

    GameInstallType CK3 = new StandardInstallType("binaries/ck3") {
        @Override
        public Path chooseBackgroundImage(Path p) {
            String[] bgs = new String[]{
                    "assassin",
                    "baghdad",
                    "castle",
                    "council",
                    "duel"
            };
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
            Matcher m = Pattern.compile("(\\d)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?\\s+\\((.+)\\)").matcher(versionString);
            if (m.find()) {
                var fourth = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
                var name = m.group(5);
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        fourth,
                        name
                ));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Path getDlcPath(Path p) {
            return p.resolve("game").resolve("dlc");
        }

        @Override
        public Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("pdx_settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.text().parse(sf);
            var langId = node
                    .getNodeForKeysIfExistent("\"System\"", "\"language\"", "value")
                    .map(Node::getString);
            return langId.flatMap(l -> Optional.ofNullable(GameLanguage.byId(l)));
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
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var date = d.format(new Date(lastPlayed.toEpochMilli()));
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.resolve("save games").relativize(path).toString()));
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", "")
                    .put("date", date);
            JacksonMapper.getDefault().writeValue(userDir.resolve("continue_game.json").toFile(), n);
        }
    };

    GameInstallType CK2 = new StandardInstallType(OsType.ofLocal() == OsType.MACOS ? "ck2" : "CK2game") {

        public String getCompatibleSavegameName(String name) {
            return Normalizer.normalize(super.getCompatibleSavegameName(name), Normalizer.Form.NFC)
                    .replaceAll("[^\\p{ASCII}]", "");
        }

        @Override
        public ModInfoStorageType getModInfoStorageType() {
            return ModInfoStorageType.SAVEGAME_DOESNT_STORE_INFO;
        }

        @Override
        public Optional<Path> getLegacyLauncherExecutable(Path p) {
            return Optional.of(getExecutable(p));
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
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) {

        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("gfx").resolve("CK2_icon.bmp");
        }

        @Override
        public Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.text().parse(sf);
            var langId = node.getNodeForKey("gui").getNodeForKey("language").getString();
            return Optional.ofNullable(GameLanguage.byId(langId));
        }

        @Override
        public Optional<GameVersion> determineVersionFromInstallation(Path p) {
            return Optional.of(new GameVersion(3, 3, 3, 0));
        }

        @Override
        public List<String> getEnabledMods(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return List.of();
            }

            var node = TextFormatParser.text().parse(sf);
            var mods = node.getNodeForKeyIfExistent("last_mods");
            if (mods.isEmpty()) {
                return List.of();
            }

            return mods.get().getNodeArray().stream().map(n -> n.getString()).collect(Collectors.toList());
        }
    };

    static enum Vic2InstallType {
        BASE_GAME,
        A_HOUSE_DIVIDED,
        HEART_OF_DARKNESS
    }

    GameInstallType VIC2 = new StandardInstallType("v2game") {

        public String getCompatibleSavegameName(String name) {
            return Normalizer.normalize(super.getCompatibleSavegameName(name), Normalizer.Form.NFC)
                    .replaceAll("[^\\p{ASCII}]", "");
        }

        @Override
        public ModInfoStorageType getModInfoStorageType() {
            return ModInfoStorageType.SAVEGAMES_AND_GAME_DONT_STORE_INFO;
        }

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
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) {

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

            return FileSystemHelper.getUserDocumentsPath().resolve("Paradox Interactive").resolve("Victoria II");
        }

        @Override
        public Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.text().parse(sf);
            var langId = node.getNodeForKey("gui").getNodeForKey("language").getString();
            return Optional.ofNullable(GameLanguage.byId(langId));
        }

        @Override
        public Path getIcon(Path p) {
            return p.resolve("load_bg.jpg");
        }

        @Override
        public Optional<GameVersion> determineVersionFromInstallation(Path p) {
            return Optional.of(new GameVersion(3, 4, 0, 0));
        }

        @Override
        public List<String> getEnabledMods(Path dir, Path userDir) {
            return List.of();
        }
    };

    GameInstallType VIC3 = new StandardInstallType("binaries/victoria3") {

        @Override
        public String getModSavegameId(GameInstallation installation, GameMod mod) {
            return mod.getName().orElse("?");
        }

        @Override
        public String getDlcSavegameId(GameInstallation installation, GameDlc dlc) {
            return dlc.getName();
        }

        @Override
        public Path chooseBackgroundImage(Path p) {
            int i = new Random().nextInt(9);
            return p.resolve("game").resolve("gfx").resolve("loadingscreens")
                    .resolve("victoria3_load_" + i + ".dds");
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
            Matcher m = Pattern.compile("(\\d)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?\\s+\\((.+)\\)").matcher(versionString);
            if (m.find()) {
                var fourth = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
                var name = m.group(5);
                return Optional.of(new GameNamedVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        fourth,
                        name
                ));
            } else {
                return Optional.empty();
            }
        }

        @Override
        public Path getDlcPath(Path p) {
            return p.resolve("game").resolve("dlc");
        }

        @Override
        public Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("pdx_settings.json");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = JacksonMapper.getDefault().readTree(sf.toFile());
            var id = Optional.ofNullable(node.get("System")).flatMap(n -> Optional.ofNullable(n.get("language")));
            return id.flatMap(l -> Optional.ofNullable(GameLanguage.byId(l.asText())));
        }

        public Path getSteamSpecificFile(Path p) {
            return null;
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
        public void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) throws IOException {
            SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var date = d.format(new Date(lastPlayed.toEpochMilli()));
            var sgPath = FilenameUtils.getBaseName(
                    FilenameUtils.separatorsToUnix(userDir.resolve("save games").relativize(path).toString()));
            var rawVersion = String.format("%s.%s.%s", version.getFirst(), version.getSecond(), version.getThird());
            ObjectNode n = JsonNodeFactory.instance.objectNode()
                    .put("title", sgPath)
                    .put("desc", "")
                    .put("date", date)
                    .put("rawVersion", rawVersion);
            JacksonMapper.getDefault().writeValue(userDir.resolve("continue_game.json").toFile(), n);
        }

        public List<String> getEnabledMods(Path dir, Path userDir) throws Exception {
            var file = userDir.resolve("content_load.json");
            if (!Files.exists(file)) {
                return List.of();
            }

            var node = JacksonMapper.getDefault().readTree(file.toFile());
            if (node.get("enabledMods") == null) {
                return List.of();
            }

            return StreamSupport.stream(node.required("enabledMods").spliterator(), false)
                    .map(n -> n.required("path").textValue())
                    .collect(Collectors.toList());
        }

        public List<String> getDisabledDlcs(Path dir, Path userDir) throws Exception {
            var file = userDir.resolve("content_load.json");
            if (!Files.exists(file)) {
                return List.of();
            }

            var node = JacksonMapper.getDefault().readTree(file.toFile());
            if (node.get("disabledDLC") == null) {
                return List.of();
            }

            return StreamSupport.stream(node.required("disabledDLC").spliterator(), false)
                    .map(n -> n.required("paradoxAppId").textValue())
                    .collect(Collectors.toList());
        }


        public void writeModAndDlcLoadFile(GameInstallation installation, List<GameMod> mods, List<GameDlc> dlcs) throws Exception {
            var file = installation.getUserDir().resolve("content_load.json");
            ObjectNode n = JsonNodeFactory.instance.objectNode();

            var modsToUse = mods;
            n.putArray("enabledMods").addAll(modsToUse.stream()
                                                     .map(d -> d.getContentPath())
                                                     .filter(path -> path.isPresent())
                                                     .map(s -> JsonNodeFactory.instance.objectNode().put("path", s.get().toString())).toList());


            var availableDlcs = installation.getDlcs();
            var currentlyDisabledDlcs = installation.queryDisabledDlcs();
            var dlcsToDisable = availableDlcs
                    .stream()
                    .filter(gameDlc -> (gameDlc.isAffectsCompatibility() && !dlcs.contains(gameDlc)) ||
                            (!gameDlc.isAffectsCompatibility() && currentlyDisabledDlcs.contains(gameDlc)))
                    .toList();

            n.putArray("disabledDLC").addAll(dlcsToDisable.stream()
                                                     .map(dlc -> getDlcLauncherId(installation, dlc))
                                                     .map(s -> JsonNodeFactory.instance.objectNode().put("paradoxAppId", s)).toList());
            JacksonMapper.getDefault().writeValue(file.toFile(), n);
        }

        public List<GameMod> loadMods(GameInstallation installation) throws IOException {
            var modPaths = new ArrayList<Path>();

            var workshop = installation.getDist().getWorkshopDir();
            if (workshop.isPresent() && Files.isDirectory(workshop.get())) {
                try (var list = Files.list(workshop.get())) {
                    modPaths.addAll(list.toList());
                }
            }

            var userDirMods = installation.getUserDir().resolve("mod");
            if (Files.isDirectory(userDirMods)) {
                try (var list = Files.list(userDirMods)) {
                    modPaths.addAll(list.toList());
                }
            }

            var mods = new ArrayList<GameMod>();
            modPaths.forEach(f -> {
                GameMod.fromVictoria3Directory(f).ifPresent(m -> {
                    mods.add(m);
                    TrackEvent.debug("Found mod " + m.getName().orElse("?") +
                                                                                 " at " + m.getContentPath().orElse(null) + ".");
                });
            });
            return mods;
        }

        public String getDlcLauncherId(GameInstallation installation, GameDlc dlc) {
            return FilenameUtils.getBaseName(dlc.getInfoFilePath().getParent().getFileName().toString());
        }
    };


    Path chooseBackgroundImage(Path p);

    default Optional<GameVersion> determineVersionFromInstallation(Path p) {
        return Optional.empty();
    }

    List<String> getLaunchArguments();

    Path getExecutable(Path p);

    Path getProtonExecutable(Path p);

    String getProtonExecutableName();

    default Optional<GameVersion> getVersion(String versionString) {
        throw new UnsupportedOperationException();
    }

    default Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
        return Optional.empty();
    }

    default Path getDlcPath(Path p) {
        return p.resolve("dlc");
    }

    default List<GameDlc> loadDlcs(Path p) throws IOException {
        var dlcs = new ArrayList<GameDlc>();
        loadDlcsFromDirectory(getDlcPath(p), dlcs);
        return dlcs;
    }

    default void loadDlcsFromDirectory(Path directory, List<GameDlc> existing) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }

        try (var s = Files.list(directory)) {
            s.forEach(f -> {
                try {
                    GameDlc.fromDirectory(f)
                           // Add duplicate check, in case DLC updates were messed up by Steam
                           .filter(gameDlc -> existing.stream()
                                                  .noneMatch(other -> other.getName().equals(gameDlc.getName())))
                           .ifPresent(existing::add);
                } catch (Exception e) {
                    ErrorEventFactory.fromThrowable(e).handle();
                }
            });
        }
    }

    default String getModLauncherId(GameInstallation installation, GameMod mod) {
        var rel = installation.getUserDir().relativize(mod.getModFile());
        return FilenameUtils.separatorsToUnix(rel.toString());
    }

    default String getModSavegameId(GameInstallation installation, GameMod mod) {
        return getModLauncherId(installation, mod);
    }

    default String getDlcLauncherId(GameInstallation installation, GameDlc dlc) {
        var rel = installation.getType().getDlcPath(installation.getInstallDir()).getParent().relativize(dlc.getInfoFilePath());
        return FilenameUtils.separatorsToUnix(rel.toString());
    }

    default String getDlcSavegameId(GameInstallation installation, GameDlc dlc) {
        return dlc.getName();
    }

    void writeLaunchConfig(Path userDir, String name, Instant lastPlayed, Path path, GameVersion version) throws IOException;

    default ModInfoStorageType getModInfoStorageType() {
        return ModInfoStorageType.STORES_INFO;
    }

    default DlcInfoStorageType getDlcInfoStorageType() {
        return DlcInfoStorageType.SAVEGAME_STORES_INFO;
    }

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

    default String getCompatibleSavegameName(String name) {
        return FileSystemHelper.getFileSystemCompatibleName(name);
    }

    public List<String> getEnabledMods(Path dir, Path userDir) throws Exception;

    public List<String> getDisabledDlcs(Path dir, Path userDir) throws Exception;

    public void writeModAndDlcLoadFile(GameInstallation installation, List<GameMod> mods, List<GameDlc> dlcs) throws Exception;

    public List<GameMod> loadMods(GameInstallation installation) throws IOException;

    default Path determineUserDir(Path p, String name) throws IOException {
        var userDirFile = p.resolve("userdir.txt");
        if (Files.exists(userDirFile)) {
            var s = Files.readString(userDirFile).trim();
            if (!s.isEmpty()) {
                return Path.of(Files.readString(userDirFile));
            }
        }

        return FileSystemHelper.getUserDocumentsPath().resolve("Paradox Interactive").resolve(name);
    }

    abstract class StandardInstallType implements GameInstallType {

        private final String executableName;

        public StandardInstallType(String executableName) {
            this.executableName = executableName;
        }

        @Override
        public Path getExecutable(Path p) {
            switch (OsType.ofLocal()) {
                case OsType.Windows ignored -> {
                    return p.resolve(executableName + ".exe");
                }
                case OsType.Linux ignored -> {
                    return p.resolve(executableName);
                }
                case OsType.MacOs ignored -> {
                    return p.resolve(p.resolve(executableName + ".app")
                                             .resolve("Contents").resolve("MacOS").resolve(Path.of(executableName).getFileName()));
                }
            }
        }

        @Override
        public Path getProtonExecutable(Path p) {
            return p.resolve(executableName + ".exe");
        }

        @Override
        public String getProtonExecutableName() {
            return executableName + ".exe";
        }

        @Override
        public Optional<GameLanguage> determineLanguage(Path dir, Path userDir) throws Exception {
            var sf = userDir.resolve("settings.txt");
            if (!Files.exists(sf)) {
                return Optional.empty();
            }

            var node = TextFormatParser.text().parse(sf);
            var langId = node.getNodeForKey("language").getString();
            return Optional.ofNullable(GameLanguage.byId(langId));
        }

        public List<String> getEnabledMods(Path dir, Path userDir) throws Exception {
            var file = userDir.resolve("dlc_load.json");
            if (!Files.exists(file)) {
                return List.of();
            }

            var node = JacksonMapper.getDefault().readTree(file.toFile());
            if (node.get("enabled_mods") == null) {
                return List.of();
            }

            return StreamSupport.stream(node.required("enabled_mods").spliterator(), false)
                    .map(n -> n.textValue())
                    .collect(Collectors.toList());
        }

        public List<String> getDisabledDlcs(Path dir, Path userDir) throws Exception {
            var file = userDir.resolve("dlc_load.json");
            if (!Files.exists(file)) {
                return List.of();
            }

            var node = JacksonMapper.getDefault().readTree(file.toFile());
            if (node.get("disabled_dlcs") == null) {
                return List.of();
            }

            return StreamSupport.stream(node.required("disabled_dlcs").spliterator(), false)
                    .map(n -> n.textValue())
                    .collect(Collectors.toList());
        }

        public void writeModAndDlcLoadFile(GameInstallation installation, List<GameMod> mods, List<GameDlc> dlcs) throws Exception {
            var file = installation.getUserDir().resolve("dlc_load.json");
            ObjectNode n = JsonNodeFactory.instance.objectNode();

            var modsToUse = (getModInfoStorageType() ==
                    ModInfoStorageType.SAVEGAME_DOESNT_STORE_INFO ? installation.queryEnabledMods() : mods);
            n.putArray("enabled_mods").addAll(modsToUse.stream()
                                                      .map(d -> FilenameUtils.separatorsToUnix
                                                              (installation.getUserDir().relativize(d.getModFile()).toString()))
                                                      .map(JsonNodeFactory.instance::textNode).toList());

            var availableDlcs = installation.getDlcs();
            var currentlyDisabledDlcs = installation.queryDisabledDlcs();
            var dlcsToDisable = switch (getDlcInfoStorageType()) {
                case SAVEGAME_STORES_INFO -> availableDlcs
                        .stream()
                        .filter(gameDlc -> (gameDlc.isAffectsCompatibility() && !dlcs.contains(gameDlc)) ||
                                (!gameDlc.isAffectsCompatibility() && currentlyDisabledDlcs.contains(gameDlc)))
                        .toList();
                case SAVEGAME_DOESNT_STORE_INFO -> currentlyDisabledDlcs;
            };
            n.putArray("disabled_dlcs").addAll(dlcsToDisable.stream()
                                                       .map(d -> FilenameUtils.separatorsToUnix(
                                                               installation.getInstallDir().relativize(d.getInfoFilePath()).toString()))
                                                       .map(JsonNodeFactory.instance::textNode).toList());
            JacksonMapper.getDefault().writeValue(file.toFile(), n);
        }

        public List<GameMod> loadMods(GameInstallation installation) throws IOException {
            if (!Files.isDirectory(installation.getUserDir().resolve("mod"))) {
                return List.of();
            }

            var mods = new ArrayList<GameMod>();
            try (var list = Files.list(installation.getUserDir().resolve("mod"))) {
                list.forEach(f -> {
                    GameMod.fromFile(f).ifPresent(m -> {
                        mods.add(m);

                        var ex = m.getAbsoluteContentPath(installation.getUserDir()).map(Files::exists).orElse(null);
                        TrackEvent.debug("Found mod " + m.getName().orElse("<no name>") +
                                                                                     " at " + m.getModFile().toString() + ". Content exists: " + ex +
                                                                                     ". Legacy: " + m.isLegacyArchive());
                    });
                });
            }
            return mods;
        }
    }
}
