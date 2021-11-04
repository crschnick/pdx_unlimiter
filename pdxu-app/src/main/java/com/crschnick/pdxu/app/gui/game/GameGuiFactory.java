package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.util.ImageHelper;
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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public abstract class GameGuiFactory<T, I extends SavegameInfo<T>> {

    public static final BidiMap<Game, GameGuiFactory<?, ?>> ALL = new DualHashBidiMap<>();

    static {
        ALL.put(Game.EU4, new Eu4GuiFactory());
        ALL.put(Game.HOI4, new Hoi4GuiFactory());
        ALL.put(Game.CK3, new Ck3GuiFactory());
        ALL.put(Game.STELLARIS, new StellarisGuiFactory());
        ALL.put(Game.CK2, new Ck2GuiFactory());
        ALL.put(Game.VIC2, new Vic2GuiFactory());
    }

    @SuppressWarnings("unchecked")
    public static <T, I extends SavegameInfo<T>> GameGuiFactory<T, I> get(Game g) {
        return (GameGuiFactory<T, I>) ALL.get(g);
    }

    protected void addNode(JFXMasonryPane pane, Region content) {
        content.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTENT);
        StackPane p = new StackPane(content);
        p.setAlignment(Pos.CENTER);
        p.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE);
        content.setPadding(new Insets(5, 10, 5, 10));
        p.setPrefWidth(Region.USE_COMPUTED_SIZE);
        pane.getChildren().add(p);

        // Magic! Using any other properties breaks the layout
        content.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> p.getWidth() - p.getPadding().getLeft() - p.getPadding().getRight(), p.widthProperty()));
        content.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> p.getHeight() - p.getPadding().getTop() - p.getPadding().getBottom(), p.heightProperty()));
    }


    protected void addIntegerEntry(JFXMasonryPane pane,
                                   Image icon, int value, String tooltip, boolean showPlus) {
        var text = (showPlus && value > 0 ? "+" + value : String.valueOf(value));
        var ironman = new StackPane(new Label(text, GameImage.imageNode(icon, CLASS_IMAGE_ICON)));
        ironman.setAlignment(Pos.CENTER);
        GuiTooltips.install(ironman, tooltip);
        ironman.getStyleClass().add("number");
        addNode(pane, ironman);
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
                    GameImage.imageNode(tagImage(entry.getInfo(), entry.getInfo().getData().getTag()), CLASS_TAG_ICON));
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
        return GameImage.imageNode(tagImage(info, info.getData().getTag()), CLASS_TAG_ICON);
    }

    public abstract Image tagImage(SavegameInfo<T> info, T tag);

    public abstract Pane background();

    public Pane createIcon() {
        var img = GameImage.getGameIcon(ALL.inverseBidiMap().get(this));
        if (img.equals(ImageHelper.DEFAULT_IMAGE)) {
            var label = new Label("(" + ALL.inverseBidiMap().get(this).getTranslatedAbbreviation() + ")");
            label.setAlignment(Pos.CENTER);
            var pane = new StackPane(label);
            pane.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            pane.setAlignment(Pos.CENTER);
            pane.getStyleClass().add(CLASS_IMAGE_ICON);
            return pane;
        } else {
            return GameImage.imageNode(img, CLASS_IMAGE_ICON);
        }
    }

    public abstract Background createEntryInfoBackground(SavegameInfo<T> info);

    public ObservableValue<String> createInfoString(SavegameCampaign<T, I> campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }
}
