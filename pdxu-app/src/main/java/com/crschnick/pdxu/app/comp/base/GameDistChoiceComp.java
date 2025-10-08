package com.crschnick.pdxu.app.comp.base;

import atlantafx.base.theme.Styles;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppFontSizes;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.InvalidInstallationException;
import com.crschnick.pdxu.app.installation.dist.GameDist;
import com.crschnick.pdxu.app.installation.dist.GameDists;
import com.crschnick.pdxu.app.installation.dist.SteamDist;
import com.crschnick.pdxu.app.installation.dist.WindowsStoreDist;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import javafx.beans.property.*;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import lombok.AllArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class GameDistChoiceComp extends SimpleComp {

    private final String nameKey;
    private final Game game;
    private final Property<GameDist> gameDist;

    private void showInstallErrorMessage(String msg) {
        String fullMsg = AppI18n.get("gameDirError", game.getTranslatedFullName()) + ":\n\n" +
                msg + "\n\n" + AppI18n.get("gameDirErrorMsg", game.getTranslatedFullName());
        ErrorEventFactory.fromMessage(fullMsg).expected().handle();
    }

    private boolean isValid(GameDist newValue) {
        try {
            var i = new GameInstallation(game.getInstallType(), newValue);
            GameInstallation.initTemporary(game, i);
            return true;
        } catch (InvalidInstallationException e) {
            showInstallErrorMessage(e.getLocalisedMessage());
            return false;
        } catch (Exception e) {
            showInstallErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    protected Region createSimple() {
        ObjectProperty<GameDist> setDist = new SimpleObjectProperty<>();

        var typeTooltip = new SimpleStringProperty();
        var typeIcon = new SimpleObjectProperty<>(new LabelGraphic.IconGraphic("mdi-help"));
        var typeLabel = new IconButtonComp(typeIcon);
        typeLabel.apply(struc -> struc.get().getStyleClass().remove(Styles.FLAT));
        typeLabel.tooltip(typeTooltip);

        var location = new SimpleStringProperty(Optional.ofNullable(gameDist.getValue()).map(v -> v.getInstallLocation().toString())
                .orElse(""));
        var locationLabel = new TextFieldComp(location);
        locationLabel.hgrow();
        locationLabel.apply(struc -> {
            struc.get().setEditable(false);
            AppFontSizes.sm(struc.get());
        });

        var browse = new IconButtonComp("mdi2f-folder-open-outline", () -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            if (setDist.get() != null && Files.exists(setDist.get().getInstallLocation())) {
                dirChooser.setInitialDirectory(setDist.get().getInstallLocation().toFile());
            }
            dirChooser.setTitle(AppI18n.get("selectDir", AppI18n.get(nameKey)));
            File file = dirChooser.showDialog(AppMainWindow.get().getStage());
            if (file != null && file.exists()) {
                var path = file.toPath();
                // Ugly hack for newer installations
                if (path.endsWith("binaries")) {
                    path = path.getParent();
                }


                var newDist = GameDists.detectDistFromDirectory(game, path);
                if (isValid(newDist)) {
                    setDist.set(newDist);
                }
            }
        });
        browse.tooltipKey("browseDist");
        browse.apply(struc -> struc.get().getStyleClass().remove(Styles.FLAT));

        var xbox = new IconButtonComp("mdi-xbox", () -> {
            var dist = WindowsStoreDist.getDist(game, null).orElse(null);
            if (dist == null) {
                ErrorEventFactory.fromMessage(AppI18n.get("xboxDistNotFound")).expected().handle();
                return;
            }

            if (isValid(dist)) {
                setDist.set(dist);
            }
        });
        xbox.tooltipKey("xboxDist");
        xbox.apply(struc -> struc.get().getStyleClass().remove(Styles.FLAT));
        if (game.getWindowsStoreName() == null) {
            xbox.disable(new ReadOnlyBooleanWrapper(true));
            xbox.tooltipKey("xboxDistUnavailable");
        }

        var del = new IconButtonComp("mdi2t-trash-can-outline", () -> {
            setDist.set(null);
        });
        del.tooltipKey("delete");
        del.apply(struc -> struc.get().getStyleClass().remove(Styles.FLAT));

        setDist.addListener((c, o, n) -> {
            if (n != null) {
                location.set(n.getInstallLocation().toString());

                if (n instanceof WindowsStoreDist) {
                    typeIcon.setValue(new LabelGraphic.IconGraphic("mdi-xbox"));
                    typeTooltip.set("Windows Store version");
                } else if (n instanceof SteamDist) {
                    typeIcon.setValue(new LabelGraphic.IconGraphic("mdi-steam"));
                    typeTooltip.set("Steam version");
                } else {
                    typeIcon.setValue(new LabelGraphic.IconGraphic("mdi-help"));
                    typeTooltip.set("Other version");
                }
            } else {
                location.set("");
                typeIcon.setValue(new LabelGraphic.IconGraphic("mdi-help"));
                typeTooltip.set(null);
            }
            gameDist.setValue(n);
        });
        setDist.setValue(gameDist.getValue());

        var hbox = new InputGroupComp(List.of(typeLabel, locationLabel, browse, xbox, del));
        hbox.styleClass("game-dist-choice-comp");
        hbox.setHeightReference(locationLabel);
        return hbox.createRegion();
    }
}
