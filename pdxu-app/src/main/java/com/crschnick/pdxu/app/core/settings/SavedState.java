package com.crschnick.pdxu.app.core.settings;

import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.ConfigHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SavedState {

    public static final int INVALID = Integer.MIN_VALUE;
    private static SavedState INSTANCE;
    private boolean maximized;
    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private Game activeGame;
    private Path previousExportLocation;

    public static void init() {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("state.json");
        INSTANCE = loadConfig(file);
    }

    public static SavedState getInstance() {
        return INSTANCE;
    }

    private static SavedState loadConfig(Path file) {
        JsonNode sNode;
        if (Files.exists(file)) {
            JsonNode node = ConfigHelper.readConfig(file);
            sNode = Optional.ofNullable(node.get("state")).orElse(JsonNodeFactory.instance.objectNode());
        } else {
            sNode = JsonNodeFactory.instance.objectNode();
        }

        SavedState s = new SavedState();
        s.maximized = Optional.ofNullable(sNode.get("maximized")).map(JsonNode::booleanValue).orElse(false);
        s.windowX = Optional.ofNullable(sNode.get("windowX")).map(JsonNode::intValue).orElse(INVALID);
        s.windowY = Optional.ofNullable(sNode.get("windowY")).map(JsonNode::intValue).orElse(INVALID);
        s.windowWidth = Optional.ofNullable(sNode.get("windowWidth")).map(JsonNode::intValue).orElse(INVALID);
        s.windowHeight = Optional.ofNullable(sNode.get("windowHeight")).map(JsonNode::intValue).orElse(INVALID);
        s.previousExportLocation = Optional.ofNullable(sNode.get("previousExportLocation"))
                .map(JsonNode::textValue)
                .map(Path::of)
                .filter(Files::exists)
                .orElse(null);

        var active = Optional.ofNullable(sNode.get("activeGame"));
        active.map(JsonNode::textValue).ifPresent(n -> {
            s.activeGame = Game.byId(n);
        });

        return s;
    }

    public void saveConfig() {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("state.json");

        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ObjectNode i = n.putObject("state");
        SavedState s = INSTANCE;
        if (s.activeGame != null) {
            i.put("activeGame", s.activeGame.getId());
        }
        i.put("maximized", s.maximized);
        i.put("windowX", s.windowX);
        i.put("windowY", s.windowY);
        i.put("windowWidth", s.windowWidth);
        i.put("windowHeight", s.windowHeight);
        if (s.previousExportLocation != null) {
            i.put("previousExportLocation", s.previousExportLocation.toString());
        }

        ConfigHelper.writeConfig(file, n);
    }

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    public int getWindowX() {
        return windowX;
    }

    public void setWindowX(int windowX) {
        this.windowX = windowX;
    }

    public int getWindowY() {
        return windowY;
    }

    public void setWindowY(int windowY) {
        this.windowY = windowY;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public Game getActiveGame() {
        return activeGame;
    }

    public void setActiveGame(Game activeGame) {
        this.activeGame = activeGame;
    }

    public Path getPreviousExportLocation() {
        return previousExportLocation;
    }

    public void setPreviousExportLocation(Path previousExportLocation) {
        this.previousExportLocation = previousExportLocation;
    }
}
