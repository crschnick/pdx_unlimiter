package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.AppTheme;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.dist.GameDists;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.platform.GlobalBooleanProperty;
import com.crschnick.pdxu.app.platform.GlobalDoubleProperty;
import com.crschnick.pdxu.app.platform.GlobalObjectProperty;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.util.ConverterSupport;
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.app.util.IronyHelper;
import com.crschnick.pdxu.app.util.OsType;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class AppPrefs {

    private static AppPrefs INSTANCE;
    private final List<Mapping> mapping = new ArrayList<>();

    public static void initLocal() {
        INSTANCE = new AppPrefs();
        INSTANCE.loadLocal();
        INSTANCE.fixLocalValues();
    }

    public static void reset() {
        INSTANCE.save();

        // Keep instance as we might need some values on shutdown, e.g. on update with terminals
        // INSTANCE = null;
    }

    public static AppPrefs get() {
        return INSTANCE;
    }

    @Getter
    private final BooleanProperty requiresRestart = new GlobalBooleanProperty(false);

    final Property<Path> eu4Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("eu4Directory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> ck3Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("ck3Directory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> hoi4Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("hoi4Directory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> stellarisDirectory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("stellarisDirectory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> ck2Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("ck2Directory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> vic2Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("vic2Directory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> vic3Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>())
            .key("vic3Directory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());

    final BooleanProperty deleteOnImport = map(Mapping.builder()
            .property(new GlobalBooleanProperty(false))
            .key("deleteOnImport")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty playSoundOnBackgroundImport = map(Mapping.builder()
            .property(new GlobalBooleanProperty(true))
            .key("playSoundOnBackgroundImport")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty importOnNormalGameExit = map(Mapping.builder()
            .property(new GlobalBooleanProperty(false))
            .key("importOnNormalGameExit")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty launchIrony = map(Mapping.builder()
            .property(new GlobalBooleanProperty(false))
            .key("launchIrony")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final Property<Path> storageDirectory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>(AppProperties.get().getDataDir().resolve("storage")))
            .key("storageDirectory")
            .valueClass(Path.class)
            .requiresRestart(true)
            .build());
    final Property<Path> ironyDirectory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>(IronyHelper.getIronyDefaultInstallPath().orElse(null)))
            .key("ironyDirectory")
            .valueClass(Path.class)
            .requiresRestart(false)
            .build());
    final Property<Path> ck3toeu4Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>(ConverterSupport.CK3_TO_EU4.determineInstallationDirectory().orElse(null)))
            .key("ck3toeu4Directory")
            .valueClass(Path.class)
            .requiresRestart(false)
            .build());
    final Property<Path> eu4tovic3Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>(ConverterSupport.EU4_TO_VIC3.determineInstallationDirectory().orElse(null)))
            .key("eu4tovic3Directory")
            .valueClass(Path.class)
            .requiresRestart(false)
            .build());
    final Property<Path> vic3tohoi4Directory = map(Mapping.builder()
            .property(new SimpleObjectProperty<>(ConverterSupport.VIC3_TO_HOI4.determineInstallationDirectory().orElse(null)))
            .key("vic3tohoi4Directory")
            .valueClass(Path.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty enableTimedImports = map(Mapping.builder()
            .property(new GlobalBooleanProperty(false))
            .key("enableTimedImports")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final Property<Integer> timedImportsInterval = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(15))
            .key("timedImportsInterval")
            .valueClass(Integer.class)
            .requiresRestart(false)
            .build());

    final Property<EditorIndentation> editorIndentation = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(EditorIndentation.ONE_TAB))
            .key("editorIndentation")
            .valueClass(EditorIndentation.class)
            .requiresRestart(false)
            .build());
    final Property<Integer> editorPageSize = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(200))
            .key("editorPageSize")
            .valueClass(Integer.class)
            .requiresRestart(false)
            .build());
    final Property<Integer> editorMaxTooltipLines = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(10))
            .key("editorMaxTooltipLines")
            .valueClass(Integer.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty editorEnableNodeTags = map(Mapping.builder()
            .property(new GlobalBooleanProperty(true))
            .key("editorEnableNodeTags")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty editorEnableNodeJumps = map(Mapping.builder()
            .property(new GlobalBooleanProperty(true))
            .key("editorEnableNodeTags")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty editorWarnOnNodeTypeChange = map(Mapping.builder()
            .property(new GlobalBooleanProperty(true))
            .key("editorWarnOnNodeTypeChange")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final Property<String> editorExternalProgram = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(EditorProvider.get().getDefaultEditor()))
            .key("editorExternalProgram")
            .valueClass(String.class)
            .requiresRestart(false)
            .build());

    public ObservableValue<EditorIndentation> editorIndentation() {
        return editorIndentation;
    }

    public ObservableValue<Integer> editorPageSize() {
        return editorPageSize;
    }

    public ObservableValue<Integer> editorMaxTooltipLines() {
        return editorMaxTooltipLines;
    }

    public ObservableValue<Boolean> editorEnableNodeTags() {
        return editorEnableNodeTags;
    }

    public ObservableValue<Boolean> editorEnableNodeJumps() {
        return editorEnableNodeJumps;
    }

    public ObservableValue<Boolean> editorWarnOnNodeTypeChange() {
        return editorWarnOnNodeTypeChange;
    }

    public ObservableValue<String> editorExternalProgram() {
        return editorExternalProgram;
    }

    public ObservableValue<Path> ck3toeu4Directory() {
        return ck3toeu4Directory;
    }

    public ObservableValue<Path> eu4tovic3Directory() {
        return eu4tovic3Directory;
    }

    public ObservableValue<Path> vic3tohoi4Directory() {
        return vic3tohoi4Directory;
    }

    public ObservableValue<Path> eu4Directory() {
        return eu4Directory;
    }

    public ObservableValue<Path> ck3Directory() {
        return ck3Directory;
    }

    public ObservableValue<Path> hoi4Directory() {
        return hoi4Directory;
    }

    public ObservableValue<Path> stellarisDirectory() {
        return stellarisDirectory;
    }

    public ObservableValue<Path> ck2Directory() {
        return ck2Directory;
    }

    public ObservableValue<Path> vic2Directory() {
        return vic2Directory;
    }

    public ObservableValue<Path> vic3Directory() {
        return vic3Directory;
    }

    public ObservableValue<Path> ironyDirectory() {
        return ironyDirectory;
    }

    public ObservableValue<Path> storageDirectory() {
        return storageDirectory;
    }

    public ObservableValue<Boolean> launchIrony() {
        return launchIrony;
    }

    public ObservableValue<Boolean> deleteOnImport() {
        return deleteOnImport;
    }

    public ObservableValue<Boolean> importOnNormalGameExit() {
        return importOnNormalGameExit;
    }

    public ObservableValue<Boolean> enableTimedImports() {
        return enableTimedImports;
    }

    public ObservableValue<Boolean> playSoundOnBackgroundImport() {
        return playSoundOnBackgroundImport;
    }

    public ObservableValue<Integer> timedImportsInterval() {
        return timedImportsInterval;
    }

    final BooleanProperty disableHardwareAcceleration = map(Mapping.builder()
            .property(new GlobalBooleanProperty(false))
            .key("disableHardwareAcceleration")
            .valueClass(Boolean.class)
            .requiresRestart(true)
            .build());
    public final BooleanProperty performanceMode = map(Mapping.builder()
            .property(new GlobalBooleanProperty())
            .key("performanceMode")
            .valueClass(Boolean.class)
            .build());
    public final ObjectProperty<AppTheme.Theme> theme = map(Mapping.builder()
            .property(new GlobalObjectProperty<>())
            .key("theme")
            .valueClass(AppTheme.Theme.class)
            .build());
    final BooleanProperty useSystemFont = map(Mapping.builder()
            .property(new GlobalBooleanProperty(OsType.ofLocal() != OsType.MACOS))
            .key("useSystemFont")
            .valueClass(Boolean.class)
            .build());
    final Property<Integer> uiScale = map(Mapping.builder()
            .property(new GlobalObjectProperty<>())
            .key("uiScale")
            .valueClass(Integer.class)
            .requiresRestart(true)
            .build());
    final BooleanProperty saveWindowLocation = map(Mapping.builder()
            .property(new GlobalBooleanProperty(true))
            .key("saveWindowLocation")
            .valueClass(Boolean.class)
            .requiresRestart(false)
            .build());
    final DoubleProperty windowOpacity = map(Mapping.builder()
            .property(new GlobalDoubleProperty(1.0))
            .key("windowOpacity")
            .valueClass(Double.class)
            .requiresRestart(false)
            .build());
    final BooleanProperty focusWindowOnNotifications = map(Mapping.builder()
            .property(new GlobalBooleanProperty(false))
            .key("focusWindowOnNotifications")
            .valueClass(Boolean.class)
            .build());
    final ObjectProperty<StartupBehaviour> startupBehaviour = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(StartupBehaviour.GUI))
            .key("startupBehaviour")
            .valueClass(StartupBehaviour.class)
            .requiresRestart(true)
            .build());
    final ObjectProperty<CloseBehaviour> closeBehaviour = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(CloseBehaviour.QUIT))
            .key("closeBehaviour")
            .valueClass(CloseBehaviour.class)
            .build());
    final ObjectProperty<SupportedLocale> language = map(Mapping.builder()
            .property(new GlobalObjectProperty<>(SupportedLocale.ENGLISH))
            .key("language")
            .valueClass(SupportedLocale.class)
            .build());
    final BooleanProperty automaticallyCheckForUpdates = map(Mapping.builder()
            .property(new GlobalBooleanProperty(true))
            .key("automaticallyCheckForUpdates")
            .valueClass(Boolean.class)
            .build());

    @Getter
    private final List<AppPrefsCategory> categories = List.of(
            new AboutCategory(),
            new AppearanceCategory(),
            new SystemCategory(),
            new GamesCategory(),
            new StorageCategory(),
            new ImportsCategory(),
            new IronyCategory(),
            new ConvertersCategory(),
            new EditorCategory(),
            new UpdatesCategory(),
            new TroubleshootCategory(),
            new LinksCategory());

    private final AppPrefsStorageHandler globalStorageHandler = new AppPrefsStorageHandler(
            AppProperties.get().getDataDir().resolve("settings").resolve("preferences.json"));

    @Getter
    private final Property<AppPrefsCategory> selectedCategory = new GlobalObjectProperty<>(categories.getFirst());

    private AppPrefs() {}

    public ObservableBooleanValue disableHardwareAcceleration() {
        return disableHardwareAcceleration;
    }

    public ObservableBooleanValue focusWindowOnNotifications() {
        return focusWindowOnNotifications;
    }

    public ObservableValue<AppTheme.Theme> theme() {
        return theme;
    }

    public ObservableValue<SupportedLocale> language() {
        return language;
    }

    public ObservableBooleanValue performanceMode() {
        return performanceMode;
    }

    public ObservableValue<Boolean> useSystemFont() {
        return useSystemFont;
    }

    public ReadOnlyProperty<Integer> uiScale() {
        return uiScale;
    }

    public ReadOnlyProperty<CloseBehaviour> closeBehaviour() {
        return closeBehaviour;
    }

    public ReadOnlyProperty<StartupBehaviour> startupBehaviour() {
        return startupBehaviour;
    }

    public ReadOnlyBooleanProperty automaticallyUpdate() {
        return automaticallyCheckForUpdates;
    }

    public ObservableDoubleValue windowOpacity() {
        return windowOpacity;
    }

    public ObservableBooleanValue saveWindowLocation() {
        return saveWindowLocation;
    }

    @SuppressWarnings("unchecked")
    private <T> T map(Mapping m) {
        mapping.add(m);
        m.property.addListener((observable, oldValue, newValue) -> {
            var running = AppOperationMode.get() == AppOperationMode.GUI;
            if (running && m.requiresRestart) {
                AppPrefs.get().requiresRestart.set(true);
            }
        });
        return (T) m.getProperty();
    }

    public <T> void setFromExternal(ObservableValue<T> prop, T newValue) {
        var writable = (Property<T>) prop;
        PlatformThread.runLaterIfNeededBlocking(() -> {
            writable.setValue(newValue);
        });
        save();
    }

    public void initInstallations() {
        setInstallation(Game.EU4, eu4Directory);
        setInstallation(Game.CK3, ck3Directory);
        setInstallation(Game.HOI4, hoi4Directory);
        setInstallation(Game.STELLARIS, stellarisDirectory);
        setInstallation(Game.CK2, ck2Directory);
        setInstallation(Game.VIC2, vic2Directory);
        setInstallation(Game.VIC3, vic3Directory);
    }

    private void setInstallation(Game g, Property<Path> prop) {
        if (prop.getValue() == null) {
            var val = GameDists.detectDist(g, AppProperties.get().isInitialLaunch())
                    .filter(gameDist -> {
                        try {
                            return Files.exists(gameDist.determineUserDir());
                        } catch (IOException e) {
                            ErrorEventFactory.fromThrowable(e).omit().handle();
                            return false;
                        }
                    })
                    .map(gameDist -> gameDist.getInstallLocation())
                    .orElse(null);
            prop.setValue(val);
        }

        if (prop.getValue() != null) {
            var install = new GameInstallation(g.getInstallType(), GameDists.detectDistFromDirectory(g, prop.getValue()));
            try {
                install.loadData();
                install.initOptional();
                GameInstallation.ALL.put(g, install);
            } catch (Exception e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }
        }
    }

    private void fixLocalValues() {
        if (System.getProperty("os.name").toLowerCase().contains("server")) {
            performanceMode.setValue(true);
        }
    }

    private void loadLocal() {
        for (Mapping value : mapping) {
            loadValue(globalStorageHandler, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T loadValue(AppPrefsStorageHandler handler, Mapping value) {
        T def = (T) value.getProperty().getValue();
        Property<T> property = (Property<T>) value.getProperty();
        var val = handler.loadObject(value.getKey(), value.getValueType(), def, value.isLog());
        property.setValue(val);
        return val;
    }

    public synchronized void save() {
        for (Mapping m : mapping) {
            // It might be possible that we save while the handler is not initialized yet / has no file or
            // directory
            if (!globalStorageHandler.isInitialized()) {
                continue;
            }
            globalStorageHandler.updateObject(m.getKey(), m.getProperty().getValue(), m.getValueType());
        }

        if (globalStorageHandler.isInitialized()) {
            globalStorageHandler.save();
        }
    }

    public void selectCategory(String id) {
        var found = categories.stream()
                .filter(appPrefsCategory -> appPrefsCategory.getId().equals(id))
                .findFirst();
        found.ifPresent(appPrefsCategory -> {
            PlatformThread.runLaterIfNeeded(() -> {
                AppLayoutModel.get().selectSettings();

                Platform.runLater(() -> {
                    // Reset scroll in case the target category is already somewhat in focus
                    selectedCategory.setValue(null);
                    selectedCategory.setValue(appPrefsCategory);
                    Platform.runLater(() -> {
                        selectedCategory.setValue(null);
                        selectedCategory.setValue(appPrefsCategory);
                    });
                });
            });
        });
    }

    public Mapping getMapping(Object property) {
        return mapping.stream().filter(m -> m.property == property).findFirst().orElseThrow();
    }

    @Value
    @Builder
    @AllArgsConstructor
    public static class Mapping {

        String key;
        Property<?> property;
        JavaType valueType;
        boolean requiresRestart;
        boolean log;

        public static class MappingBuilder {

            MappingBuilder valueClass(Class<?> clazz) {
                this.valueType(TypeFactory.defaultInstance().constructType(clazz));
                return this;
            }
        }
    }
}
