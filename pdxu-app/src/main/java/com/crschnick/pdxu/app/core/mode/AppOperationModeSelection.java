package com.crschnick.pdxu.app.core.mode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum AppOperationModeSelection {
    @JsonProperty("background")
    BACKGROUND("background"),

    @JsonProperty("tray")
    TRAY("tray"),

    @JsonProperty("gui")
    GUI("gui");

    private final String displayName;

    AppOperationModeSelection(String displayName) {
        this.displayName = displayName;
    }

    public static Optional<AppOperationModeSelection> getIfPresent(String name) {
        if (name == null) {
            return Optional.empty();
        }

        return Arrays.stream(AppOperationModeSelection.values())
                .filter(m -> m.name().equalsIgnoreCase(name))
                .findAny();
    }

    public static AppOperationModeSelection get(String name) {
        return Arrays.stream(AppOperationModeSelection.values())
                .filter(m -> m.getDisplayName().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown mode: " + name + ". Possible values: "
                        + Arrays.stream(values())
                                .map(AppOperationModeSelection::getDisplayName)
                                .collect(Collectors.joining(", "))));
    }
}
