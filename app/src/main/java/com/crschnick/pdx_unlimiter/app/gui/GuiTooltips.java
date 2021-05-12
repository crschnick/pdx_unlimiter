package com.crschnick.pdx_unlimiter.app.gui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import javafx.util.Duration;

public class GuiTooltips {

    public static Tooltip createTooltip(String text) {
        var tt = new FocusTooltip(text);
        tt.styleProperty().setValue("-fx-font-size: 1em; -fx-background-color: #333333FF;");
        return tt;
    }

    public static Node helpNode(String text) {
        Label q = new Label(" ? ");
        q.setStyle("-fx-border-color: #333333FF;");
        var tt = GuiTooltips.createTooltip(text);
        tt.setShowDelay(Duration.ZERO);
        tt.setShowDuration(Duration.INDEFINITE);
        q.setTooltip(tt);
        return q;
    }

    public static void install(Node node, String text) {
        // TODO
        // Attempt to fix weird tooltip errors by forcing the platform thread to do the installation
        Platform.runLater(() -> {
            var tt = GuiTooltips.createTooltip(text);
            tt.setShowDelay(Duration.millis(350));
            tt.setShowDuration(Duration.INDEFINITE);
            Tooltip.install(node, tt);
        });
    }

    private static class FocusTooltip extends Tooltip {

        public FocusTooltip(String string) {
            super(string);
        }

        @Override
        protected void show() {
            Window owner = getOwnerWindow();
            if (owner.isFocused())
                super.show();
        }
    }
}
