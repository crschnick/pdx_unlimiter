package com.crschnick.pdx_unlimiter.app.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class PdxuProperties {

    private Path customDataDir;
    private boolean simulateProduction;
    private Path customRakalyDir;
    private boolean developerMode;
    private boolean nativeHookEnabled;

    PdxuProperties(boolean production, Path settingsDir) {
        Properties props = new Properties();
        if (production) {
            Path propsFile = settingsDir.resolve("pdxu.properties");
            if (Files.exists(propsFile)) {
                try (var in = Files.newInputStream(propsFile)){
                    props.load(in);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }
        } else {
            try (var in = Files.newInputStream(Path.of("app", "pdxu.properties"))) {
                props.load(in);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            var customDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(Path::isAbsolute);
            customDir.ifPresent(path -> customDataDir = path);

            simulateProduction = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);

            Optional.ofNullable(props.get("rakalyDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(val -> val.isAbsolute() && Files.exists(val))
                    .ifPresent(path -> customRakalyDir = path);
        }

        developerMode = Optional.ofNullable(props.get("developerMode"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(false);
        nativeHookEnabled = Optional.ofNullable(props.get("enableJNativeHook"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(true);
    }

    public Optional<Path> getCustomDataDir() {
        return Optional.ofNullable(customDataDir);
    }

    public boolean isSimulateProduction() {
        return simulateProduction;
    }

    public Optional<Path> getCustomRakalyDir() {
        return Optional.ofNullable(customRakalyDir);
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public boolean isNativeHookEnabled() {
        return nativeHookEnabled;
    }
}
