package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.InstallLocationHelper;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class SavedState {

    private static SavedState INSTANCE;

    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private GameInstallation activeGame;

    public static final int INVALID = Integer.MIN_VALUE;

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
            try {
                JsonNode node = new ObjectMapper().readTree(Files.readAllBytes(file));
                sNode = node.required("state");
            } catch (Exception e) {
                ErrorHandler.handleException(e);
                sNode = JsonNodeFactory.instance.objectNode();
            }
        } else {
            sNode = JsonNodeFactory.instance.objectNode();
        }

        SavedState s = new SavedState();
        s.windowX = Optional.ofNullable(sNode.get("windowX")).map(JsonNode::intValue).orElse(INVALID);
        s.windowY = Optional.ofNullable(sNode.get("windowY")).map(JsonNode::intValue).orElse(INVALID);
        s.windowWidth = Optional.ofNullable(sNode.get("windowWidth")).map(JsonNode::intValue).orElse(INVALID);
        s.windowHeight = Optional.ofNullable(sNode.get("windowHeight")).map(JsonNode::intValue).orElse(INVALID);

        var active = Optional.ofNullable(sNode.get("activeGame"));
        active.map(JsonNode::textValue).ifPresent(n -> {
            GameInstallation.ALL.forEach(i -> {
                if (i.getId().equals(n)) {
                    s.activeGame = i;
                }
            });
        });
        if (s.activeGame == null) {
            s.activeGame = GameInstallation.ALL.stream().findFirst().orElse(null);
        }

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
        i.put("windowX", s.windowX);
        i.put("windowY", s.windowY);
        i.put("windowWidth", s.windowWidth);
        i.put("windowHeight", s.windowHeight);

        try {
            FileUtils.forceMkdirParent(file.toFile());
            JsonHelper.write(n, Files.newOutputStream(file));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public void setWindowX(int windowX) {
        this.windowX = windowX;
    }

    public void setWindowY(int windowY) {
        this.windowY = windowY;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public void setActiveGame(GameInstallation activeGame) {
        this.activeGame = activeGame;
    }

    public int getWindowX() {
        return windowX;
    }

    public int getWindowY() {
        return windowY;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public GameInstallation getActiveGame() {
        return activeGame;
    }
}
