package com.crschnick.pdxu.app.core.window;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.platform.NativeWinWindowControl;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.OsType;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemUtils;

public class AppModifiedStage extends Stage {

    public static boolean mergeFrame() {
        return SystemUtils.IS_OS_WINDOWS_10 || SystemUtils.IS_OS_WINDOWS_11;
    }

    public static void init() {
        ObservableList<Window> list = Window.getWindows();
        list.addListener((ListChangeListener<Window>) c -> {
            if (c.next() && c.wasAdded()) {
                var added = c.getAddedSubList().getFirst();
                if (added instanceof Stage stage) {
                    hookUpStage(stage);
                }
            }
        });
    }

    public static void prepareStage(Stage stage) {
        if (mergeFrame()) {
            stage.initStyle(StageStyle.UNIFIED);
        }
    }

    private static void hookUpStage(Stage stage) {
        applyModes(stage);
        if (AppPrefs.get() != null) {
            AppPrefs.get().theme().addListener((observable, oldValue, newValue) -> {
                updateStage(stage);
            });
            AppPrefs.get().performanceMode().addListener((observable, oldValue, newValue) -> {
                updateStage(stage);
            });
        }
        if (stage.getScene() != null) {
            stage.getScene().rootProperty().addListener((observable, oldValue, newValue) -> {
                applyModes(stage);
            });
        }
    }

    @SneakyThrows
    private static void applyModes(Stage stage) {
        if (stage.getScene() == null) {
            return;
        }

        if (!stage.isShowing()) {
            return;
        }

        var applyToStage = OsType.ofLocal() == OsType.WINDOWS;
        if (!applyToStage || AppPrefs.get() == null || AppPrefs.get().theme().getValue() == null) {
            stage.getScene().getRoot().pseudoClassStateChanged(PseudoClass.getPseudoClass("seamless-frame"), false);
            stage.getScene().getRoot().pseudoClassStateChanged(PseudoClass.getPseudoClass("separate-frame"), true);
            return;
        }

        try {
            switch (OsType.ofLocal()) {
                case OsType.Linux ignored -> {}
                case OsType.MacOs ignored -> {}
                case OsType.Windows ignored -> {
                    var ctrl = new NativeWinWindowControl(stage);
                    ctrl.setWindowAttribute(
                            NativeWinWindowControl.DmwaWindowAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE.get(),
                            AppPrefs.get().theme().getValue().isDark());
                    boolean seamlessFrame;
                    if (AppPrefs.get().performanceMode().get()
                            || !mergeFrame()
                            || AppMainWindow.get() == null
                            || stage != AppMainWindow.get().getStage()) {
                        seamlessFrame = false;
                    } else {
                        // This is not available on Windows 10
                        seamlessFrame = ctrl.setWindowBackdrop(NativeWinWindowControl.DwmSystemBackDropType.MICA_ALT)
                                || SystemUtils.IS_OS_WINDOWS_10;
                    }
                    stage.getScene()
                            .getRoot()
                            .pseudoClassStateChanged(PseudoClass.getPseudoClass("seamless-frame"), seamlessFrame);
                    stage.getScene()
                            .getRoot()
                            .pseudoClassStateChanged(PseudoClass.getPseudoClass("separate-frame"), !seamlessFrame);
                }
            }
        } catch (Throwable t) {
            ErrorEventFactory.fromThrowable(t).omit().handle();
        }
    }

    private static void updateStage(Stage stage) {
        if (!stage.isShowing()) {
            return;
        }

        PlatformThread.runLaterIfNeeded(() -> {
            var transition = new PauseTransition(Duration.millis(300));
            transition.setOnFinished(e -> {
                applyModes(stage);
                // We only need to update the frame by resizing on Windows
                if (OsType.ofLocal() == OsType.WINDOWS) {
                    stage.setWidth(stage.getWidth() - 1);
                    Platform.runLater(() -> {
                        stage.setWidth(stage.getWidth() + 1);
                    });
                }
            });
            transition.play();
        });
    }
}
