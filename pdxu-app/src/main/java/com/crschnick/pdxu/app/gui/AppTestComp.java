package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppStyle;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AppTestComp extends SimpleComp {

    @Override
    protected Region createSimple() {
        var box = new VBox(
                button("Throw error", () -> {
                    ErrorEventFactory.fromThrowable(new IllegalStateException("Test error"))
                            .handle();
                }),
                button("Show confirmation dialog", () -> {
                    AppDialog.confirm("deleteSavegame");
                }),
                button("Run blocking task for 5 seconds", () -> {
                    TaskExecutor.getInstance().submitTask(() -> ThreadHelper.sleep(5000), true);
                }),
                button("Clear game caches", () -> {
                    GameCacheManager.getInstance().onGameChange();
                }),
                button("Test all savegame infos", () -> {
                    ThreadHelper.runAsync(SavegameInfoTest::run);
                }),
                button("Reload stylesheets", () -> {
                    AppStyle.reloadStylesheets(AppMainWindow.get().getStage().getScene());
                }));
        box.setSpacing(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_LEFT);
        return box;
    }

    private static Button button(String text, Runnable action) {
        var button = new Button(text);
        button.setOnAction(e -> {
            action.run();
            e.consume();
        });
        return button;
    }
}
