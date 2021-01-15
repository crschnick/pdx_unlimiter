package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.Settings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class GuiTooltips {

    public static Tooltip createTooltip(String text) {
        var tt = new Tooltip(text);
        tt.styleProperty().setValue("-fx-font-size: 1em; -fx-background-color: #333333FF;");
        return tt;
    }

    public static Node helpNode(String text) {
        Label q = new Label(" ? ");
        q.setStyle("-fx-border-color: #333333FF;");
        var tt = GuiTooltips.createTooltip(text);
        tt.setShowDelay(Duration.ZERO);
        q.setTooltip(tt);
        return q;
    }

    public static void install(Node node, String text) {
        var tt = GuiTooltips.createTooltip(text);
        tt.setShowDelay(Duration.millis(350));
        Tooltip.install(node, tt);
    }
}
