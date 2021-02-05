package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.install.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.core.SavedState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.HBox;

public class GuiGameSwitcher {

    public static void showGameSwitchDialog() {
        Alert alert = DialogHelper.createEmptyAlert();
        alert.setTitle("Select game");

        HBox games = new HBox();
        for (var integ : GameIntegration.ALL) {
            var icon = integ.getGuiFactory().createIcon();
            ColorAdjust desaturate = new ColorAdjust();
            desaturate.setSaturation(-1);
            icon.getStyleClass().add(GuiStyle.CLASS_GAME_ICON);

            GuiTooltips.install(icon, integ.getName());
            icon.setOnMouseClicked(e -> {
                SavegameManagerState.get().selectIntegration(integ);
                SavedState.getInstance().setActiveGame(integ.getInstallation());
                alert.setResult(ButtonType.CLOSE);
            });

            icon.setOnMouseEntered(e -> {
                icon.setEffect(null);
            });
            icon.setOnMouseExited(e -> {
                icon.setEffect(desaturate);
            });
            games.getChildren().add(icon);
            icon.setEffect(desaturate);
        }
        games.setFillHeight(true);
        games.getStyleClass().add(GuiStyle.CLASS_GAME_ICON_BAR);

        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));
        alert.getDialogPane().setContent(games);
        alert.showAndWait();
    }
}
