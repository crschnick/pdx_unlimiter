package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCampaign;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;
import static com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper.createAlert;

public abstract class GameGuiFactory<T, I extends SavegameInfo<T>> {

    private final String styleClass;
    private final GameInstallation installation;

    public GameGuiFactory(String styleClass, GameInstallation installation) {
        this.styleClass = styleClass;
        this.installation = installation;
    }

    protected static void addNode(JFXMasonryPane pane, Region content) {
        content.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTENT);
        StackPane p = new StackPane(content);
        p.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE);
        content.setPadding(new Insets(5, 10, 5, 10));
        pane.getChildren().add(p);
        content.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> p.getWidth() - p.getPadding().getLeft() - p.getPadding().getRight(), p.widthProperty()));
        content.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> p.getHeight() - p.getPadding().getTop() - p.getPadding().getBottom(), p.heightProperty()));
        p.setAlignment(Pos.CENTER);
    }

    public boolean displayIncompatibleWarning(SavegameEntry<T, I> entry) {
        var launch = new ButtonType("Launch anyway");
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.WARNING);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.getButtonTypes().add(launch);
        alert.setTitle("Incompatible savegame");

        StringBuilder builder = new StringBuilder("Selected savegame is incompatible. Launching it anyway, can cause problems.\n\n");
        if (!SavegameActions.isVersionCompatible(entry.getInfo())) {
            builder.append("Incompatible versions:\n")
                    .append("- Game version: " + installation.getVersion().toString()).append("\n")
                    .append("- Savegame version: " + entry.getInfo().getVersion().toString());
        }

        boolean missingMods = entry.getInfo().getMods().stream()
                .map(m -> installation.getModForName(m))
                .anyMatch(Optional::isEmpty);
        if (missingMods) {
            builder.append("\nThe following Mods are missing:\n").append(entry.getInfo().getMods().stream()
                    .map(s -> {
                        var m = installation.getModForName(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        boolean missingDlc = entry.getInfo().getDlcs().stream()
                .map(m -> installation.getDlcForName(m))
                .anyMatch(Optional::isEmpty);
        if (missingDlc) {
            builder.append("\nThe following DLCs are missing:\n").append(entry.getInfo().getDlcs().stream()
                    .map(s -> {
                        var m = installation.getDlcForName(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        alert.setHeaderText(builder.toString());
        return alert.showAndWait().orElse(ButtonType.CLOSE).equals(launch);
    }


    public ObservableValue<Node> createImage(SavegameEntry<T, I> entry) {
        SimpleObjectProperty<Node> prop;
        if (entry.getInfo() == null) {
            prop = new SimpleObjectProperty<>(new Region());
            entry.infoProperty().addListener((c, o, n) -> {
                prop.set(n != null ? tagNode(entry.getInfo()) : new Region());
            });
        } else {
            prop = new SimpleObjectProperty<>(
                    GameImage.imageNode(tagImage(entry.getInfo(), entry.getInfo().getTag()), CLASS_TAG_ICON));
        }
        return prop;
    }

    public ObservableValue<Node> createImage(SavegameCampaign<T, I> campaign) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(
                GameImage.imageNode(campaign.getImage(), CLASS_TAG_ICON));
        campaign.imageProperty().addListener((ChangeListener<? super Image>) (c, o, n) -> {
            prop.set(GameImage.imageNode(n, CLASS_TAG_ICON));
        });
        return prop;
    }

    public Node tagNode(SavegameInfo<T> info) {
        return GameImage.imageNode(tagImage(info, info.getTag()), CLASS_TAG_ICON);
    }

    public abstract Image tagImage(SavegameInfo<T> info, T tag);

    public abstract Font font() throws IOException;

    public abstract Pane background();

    public abstract Pane createIcon();

    public abstract Background createEntryInfoBackground(SavegameInfo<T> info);

    public ObservableValue<String> createInfoString(SavegameCampaign<T, I> campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }

    protected Label createVersionInfo(SavegameInfo<T> info) {
        Label version;
        if (SavegameActions.isVersionCompatible(info)) {
            version = new Label(info.getVersion().toString());
            GuiTooltips.install(version, "Compatible version");
            version.getStyleClass().add(CLASS_COMPATIBLE);
        } else {
            version = new Label(info.getVersion().toString());
            GuiTooltips.install(version, "Incompatible savegame version");
            version.getStyleClass().add(CLASS_INCOMPATIBLE);
        }
        return version;
    }

    public void fillNodeContainer(SavegameInfo<T> info, JFXMasonryPane grid) {
        grid.getStyleClass().add(styleClass);

        Label version = createVersionInfo(info);
        version.setAlignment(Pos.CENTER);
        addNode(grid, version);

        if (info.getMods().size() > 0) {
            Label mods = new Label("Mods");
            mods.setGraphic(new FontIcon());
            mods.getStyleClass().add(CLASS_CONTENT);
            GuiTooltips.install(mods,
                    "Requires the following " + info.getMods().size() + " mods:\n" +
                            info.getMods().stream()
                                    .map(s -> {
                                        var m = installation.getModForName(s);
                                        return "- " + (m.isPresent() ? m.get().getName() : s + " (Missing)");
                                    })
                                    .collect(Collectors.joining("\n")));

            boolean missing = info.getMods().stream()
                    .map(m -> installation.getModForName(m))
                    .anyMatch(Optional::isEmpty);
            mods.getStyleClass().add(missing ? CLASS_INCOMPATIBLE : CLASS_COMPATIBLE);
            mods.setAlignment(Pos.CENTER);
            addNode(grid, mods);
        }

        if (info.getDlcs().size() > 0) {
            Label dlcs = new Label("DLCs");
            dlcs.setGraphic(new FontIcon());
            dlcs.getStyleClass().add(CLASS_CONTENT);
            GuiTooltips.install(dlcs,
                    "Requires the following " + info.getDlcs().size() + " DLCs:\n" +
                            info.getDlcs().stream()
                                    .map(s -> {
                                        var m = installation.getDlcForName(s);
                                        return "- " + (m.isPresent() ? m.get().getName() : s + " (Missing)");
                                    })
                                    .collect(Collectors.joining("\n")));
            boolean missing = info.getDlcs().stream()
                    .map(m -> installation.getDlcForName(m))
                    .anyMatch(Optional::isEmpty);
            dlcs.getStyleClass().add(missing ? CLASS_INCOMPATIBLE : CLASS_COMPATIBLE);
            dlcs.setAlignment(Pos.CENTER);

            addNode(grid, dlcs);
        }
    }
}
