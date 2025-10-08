package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.base.TooltipHelper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class GuiTooltips {

    public static void install(Node node, String text) {
        Platform.runLater(() -> {
            var tt = TooltipHelper.create(new ReadOnlyStringWrapper(text), null);
            tt.setShowDelay(Duration.millis(350));
            tt.setShowDuration(Duration.INDEFINITE);
            Tooltip.install(node, tt);
        });
    }
}
