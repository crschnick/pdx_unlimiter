package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public final class GameAppManager {

    private static final Logger logger = LoggerFactory.getLogger(GameAppManager.class);
    private static final GameAppManager INSTANCE = new GameAppManager();

    private final ObjectProperty<GameApp> activeGame = new SimpleObjectProperty<>(null);
    private Instant lastImport;
    private boolean active = false;
    private Thread thread;

    private GameAppManager() {
    }

    public static void init() {
        INSTANCE.start();
    }

    public static void reset() {
        INSTANCE.stop();
    }

    private static boolean isInstanceOfGame(String cmd, Game game) {
        var install = GameInstallation.ALL.get(game);
        return install != null && install.getDist().isGameInstance(cmd);
    }

    public static GameAppManager getInstance() {
        return INSTANCE;
    }

    private void start() {
        active = true;
        thread = ThreadHelper.create("game watcher", true, () -> {
            while (active) {
                try {
                    update();
                    ThreadHelper.sleep(1000);
                } catch (Exception ex) {
                    // Catch all exceptions to not terminate this thread if an error occurs!
                    ErrorHandler.handleException(ex);
                }
            }
        });
        thread.start();
    }

    private void stop() {
        active = false;
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        thread = null;
        activeGame.set(null);
    }

    private void update() {
        if ((activeGame.get() == null)) {
            var process = Arrays.stream(Game.values())
                    .map(g -> new GameApp(ProcessHandle.allProcesses()
                            .filter(p -> p.info().command()
                                    .map(cs -> isInstanceOfGame(cs, g)).orElse(false))
                            .findAny().orElse(null), g)
                    )
                    .filter(ga -> ga.getProcess() != null)
                    .findAny();
            if (process.isPresent()) {
                logger.info("Detected new running game instance of " + process.get().getGame().getId());
                activeGame.set(process.get());
                activeGame.get().onStart();
            }
        } else {
            updateImportTimer();

            if (!activeGame.get().isAlive()) {
                logger.info("Game instance of " + activeGame.get().getGame().getId() + " is dead");
                activeGame.get().onShutdown();
                activeGame.set(null);
            }
        }
    }

    private void updateImportTimer() {
        if (!Settings.getInstance().enabledTimedImports.getValue()) {
            return;
        }

        if (lastImport == null) {
            lastImport = Instant.now();
        }

        if (Duration.between(lastImport, Instant.now()).compareTo(
                Duration.of(Settings.getInstance().timedImportsInterval.getValue(), ChronoUnit.MINUTES)) > 0) {
            logger.info("Importing latest savegame because timed imports is enabled");
            if (Settings.getInstance().playSoundOnBackgroundImport.getValue()) {
                Toolkit.getDefaultToolkit().beep();
            }
            importLatest();
            lastImport = Instant.now();
        }
    }

    public void playImportSound() {
        if (Settings.getInstance().playSoundOnBackgroundImport.getValue()) {
            var clip = new AudioClip(PdxuInstallation.getInstance().getResourceDir().resolve("sound")
                    .resolve("import.wav").toUri().toString());
            clip.play(0.2);
        }
    }

    public void importLatest() {
        var g = getActiveGame();
        if (g != null && g.getGame().isEnabled()) {
            logger.info("Importing latest savegame");
            SavegameActions.importLatestSavegame(g.getGame());
        }
    }

    public void loadLatestCheckpoint() {
        var g = getActiveGame();
        if (g != null && g.getGame().isEnabled()) {
            logger.info("Loading latest checkpoint");
            g.kill();
            SavegameActions.loadLatestSavegameCheckpoint(g.getGame());
        }
    }

    public void kill() {
        var g = getActiveGame();
        if (g != null && g.getGame().isEnabled()) {
            logger.info("Killing active game");
            g.kill();
        }
    }

    public GameApp getActiveGame() {
        return activeGame.get();
    }

    public ObjectProperty<GameApp> activeGameProperty() {
        return activeGame;
    }
}