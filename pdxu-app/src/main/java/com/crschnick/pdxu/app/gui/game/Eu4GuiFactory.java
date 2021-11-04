package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu4.Eu4RulerComp;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.app.lang.GameLocalisation;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Eu4GuiFactory extends GameGuiFactory<Eu4Tag, Eu4SavegameInfo> {


    private static Region createPowersNode(int admP, int dipP, int milP) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Label adm = new Label(admP + "  ", imageNode(EU4_ICON_ADM, CLASS_POWER_ICON));
        box.getChildren().add(adm);

        Label dip = new Label(dipP + "  ", imageNode(EU4_ICON_DIP, CLASS_POWER_ICON));
        box.getChildren().add(dip);

        Label mil = new Label(String.valueOf(milP), imageNode(EU4_ICON_MIL, CLASS_POWER_ICON));
        box.getChildren().add(mil);
        return box;
    }

    private void createDiplomacyRow(
            JFXMasonryPane pane,
            SavegameInfo<Eu4Tag> info,
            Region icon,
            List<Eu4Tag> tags,
            String tooltip,
            String style) {
        if (tags.size() == 0) {
            return;
        }

        var row = TagRows.createTagRow(icon, tooltip, tags,
                t -> GameLocalisation.getLocalisedValue(t.getTag(), info),
                t -> GameImage.imageNode(Eu4TagRenderer.smallShieldImage(info, t), CLASS_TAG_ICON));
        row.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        row.getStyleClass().add(style);
        addNode(pane, row);
    }

    private void addManpowerEntry(
            JFXMasonryPane pane,
            int value, int max) {
        var label = new Label(value + "k / " + max + "k",
                GameImage.imageNode(EU4_ICON_MANPOWER, CLASS_IMAGE_ICON));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.getStyleClass().add("number");
        stack.setMinWidth(label.getPrefWidth());
        GuiTooltips.install(stack, PdxuI18n.get("MANPOWER") + " / " + PdxuI18n.get("MAX_MANPOWER"));
        addNode(pane, stack);
    }

    private void addDucatsEntry(
            JFXMasonryPane pane,
            int value, int loans) {
        var label = new Label(value + (loans != 0 ? " / -" + loans : ""),
                GameImage.imageNode(EU4_ICON_DUCATS, CLASS_IMAGE_ICON));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(label.getPrefWidth());
        stack.getStyleClass().add("number");
        GuiTooltips.install(stack, PdxuI18n.get("TREASURY") + (loans != 0 ? " / " + PdxuI18n.get("TREASURY_OWED") : ""));
        addNode(pane, stack);
    }

    private void addDevelopmentEntry(
            JFXMasonryPane pane,
            int totalDev, int autonomyDev) {
        var label = new Label(autonomyDev + " / " + totalDev,
                GameImage.imageNode(EU4_ICON_DEV, CLASS_IMAGE_ICON));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(label.getPrefWidth());
        stack.getStyleClass().add("number");
        GuiTooltips.install(stack, PdxuI18n.get("AUTONOMY_DEV") + " / " + PdxuI18n.get("TOTAL_DEV"));
        addNode(pane, stack);
    }

    private void addPowersEntry(
            JFXMasonryPane pane, int adm, int dip, int mil) {
        var label = createPowersNode(adm, dip, mil);
        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(label.getPrefWidth());
        stack.getStyleClass().add("number");
        addNode(pane, stack);
    }

    @Override
    public Image tagImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        return Eu4TagRenderer.shieldImage(info, tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(EU4_BACKGROUND);
        bg.setOpacity(0.9);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Eu4Tag> info) {
        return new Background(new BackgroundFill(
                ColorHelper.withAlpha(ColorHelper.fromGameColor(info.getData().getTag().getMapColor()), 0.33),
                CornerRadii.EMPTY, Insets.EMPTY));
    }
}
