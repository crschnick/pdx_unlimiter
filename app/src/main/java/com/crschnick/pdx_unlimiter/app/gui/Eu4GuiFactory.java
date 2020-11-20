package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.game.Eu4CampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameTag;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Set;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.*;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class Eu4GuiFactory extends GameGuiFactory<Eu4CampaignEntry, Eu4Campaign> {

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(EU4_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(Eu4CampaignEntry entry) {
        return new Background(new BackgroundFill(
                colorFromInt(entry.getInfo().getCurrentTag().getMapColor(), 100),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public Pane createGameImage(Eu4Campaign campaign) {
        return GameImage.imageNode(EU4_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Pane createImage(Eu4CampaignEntry entry) {
        return GameImage.tagNode(entry.getTag(), CLASS_TAG_ICON);
    }

    @Override
    public ObservableValue<Pane> createImage(Eu4Campaign campaign) {
        var b = Bindings.createObjectBinding(
                () -> GameImage.tagNode(campaign.getTag(), CLASS_TAG_ICON), campaign.tagProperty());
        return b;
    }

    @Override
    public String createInfoString(Eu4CampaignEntry entry) {
        return entry.getDate().toDisplayString();
    }

    @Override
    public ObservableValue<String> createInfoString(Eu4Campaign campaign) {
        var b = Bindings.createObjectBinding(
                () -> campaign.getDate().toDisplayString(), campaign.dateProperty());
        return b;
    }

    @Override
    public void fillNodeContainer(Eu4CampaignEntry entry, JFXMasonryPane grid) {
        Eu4SavegameInfo info = entry.getInfo();
        if (info.isObserver()) {
            return;
        }

        grid.getChildren().add(createRulerLabel(info.getRuler(), true));
        if (info.getHeir().isPresent()) {
            grid.getChildren().add(createRulerLabel(info.getHeir().get(), false));
        }

        HBox status = new HBox();
        status.setStyle("-fx-border-width: 3px;");
        status.setSpacing(4);


        status.getChildren().add(imageNode(EU4_ICON_IRONMAN, CLASS_IMAGE_ICON, "Ironman savegame"));
        status.getChildren().add(imageNode(EU4_ICON_RANDOM_NEW_WORLD, CLASS_IMAGE_ICON, "Random new world enabled"));
        status.getChildren().add(imageNode(EU4_ICON_CUSTOM_NATION, CLASS_IMAGE_ICON, "A custom nation exists in the world"));
        status.getChildren().add(imageNode(EU4_ICON_RELEASED_VASSAL, CLASS_IMAGE_ICON, "Is playing as a released vassal"));
        //grid.add(status, 0, 1);

        for (Eu4SavegameInfo.War war : info.getWars()) {
            createDiplomacyRow(grid, imageNode(EU4_ICON_WAR, CLASS_IMAGE_ICON), war.getEnemies(),
                    "Fighting in the " + war.getTitle() + " against ", "", CLASS_WAR);
        }

        createDiplomacyRow(grid, imageNode(EU4_ICON_ALLIANCE, CLASS_IMAGE_ICON), info.getAllies(),
                "Allies: ", "None", CLASS_ALLIANCE);
        createDiplomacyRow(grid, imageNode(EU4_ICON_ROYAL_MARRIAGE, CLASS_IMAGE_ICON), info.getMarriages(),
                "Royal marriages: ", "None", CLASS_MARRIAGE);
        createDiplomacyRow(grid, imageNode(EU4_ICON_GUARANTEE, CLASS_IMAGE_ICON), info.getGuarantees(),
                "Guarantees: ", "None", CLASS_GUARANTEE);
        createDiplomacyRow(grid, imageNode(EU4_ICON_VASSAL, CLASS_IMAGE_ICON), info.getVassals(),
                "Vassals: ", "None", CLASS_VASSAL);
        createDiplomacyRow(grid, imageNode(EU4_ICON_VASSAL, CLASS_IMAGE_ICON), info.getJuniorPartners(),
                "Personal union junior partners: ", "none", CLASS_VASSAL);
        createDiplomacyRow(grid, imageNode(EU4_ICON_TRIBUTARY, CLASS_IMAGE_ICON), info.getTributaryJuniors(),
                "Tributaries: ", "None", CLASS_VASSAL);
        createDiplomacyRow(grid, imageNode(EU4_ICON_MARCH, CLASS_IMAGE_ICON), info.getMarches(),
                "Marches: ", "None", CLASS_VASSAL);
        createDiplomacyRow(grid, imageNode(EU4_ICON_TRUCE, CLASS_IMAGE_ICON),
                info.getTruces().keySet(), "Truces: ", "None", CLASS_TRUCE);
        createDiplomacyRow(grid, imageNode(EU4_ICON_VASSAL, CLASS_IMAGE_ICON),
                info.getSeniorPartner().map(Set::of).orElse(Set.of()),
                "Under personal union with ", "no country", CLASS_VASSAL);


        Label version;
        if (GameIntegration.current().isVersionCompatibe(entry)) {
            version = new Label("v" + info.getVersion().toString());
            Tooltip.install(version, tooltip("Compatible version"));
            version.getStyleClass().add(CLASS_VERSION_OK);
        } else {
            version = new Label("v" + info.getVersion().toString(), imageNode(EU4_ICON_VERSION_WARNING, CLASS_IMAGE_ICON));
            Tooltip.install(version, tooltip("Incompatible savegame version"));
            version.getStyleClass().add(CLASS_VERSION_INCOMPATIBLE);
        }
        version.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE);
        grid.getChildren().add(version);
    }

    private static Tooltip tooltip(String text) {

        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.millis(100));
        return t;
    }

    private static Node getImageForTagName(String tag, String styleClass) {
        if (GameInstallation.EU4.isPreexistingCoutry(tag)) {
            return GameImage.tagNode(tag, styleClass);
        } else {
            Label l = new Label("?");
            l.getStyleClass().add(styleClass);
            l.alignmentProperty().set(Pos.CENTER);
            return l;
        }
    }

    private static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    private static Node getImageForTag(GameTag tag, String styleClass) {
        Node n = getImageForTagName(tag.getTag(), styleClass);
        if (tag.isCustom()) {
            int c = tag.getCountryColor();
            ((Label)n).setBackground(new Background(
                    new BackgroundFill(colorFromInt(c, 255), CornerRadii.EMPTY, Insets.EMPTY)));
        }
        return n;
    }

    private static Node createRulerLabel(Eu4SavegameInfo.Ruler ruler, boolean isRuler) {
        VBox box = new VBox();
        if (isRuler) {
            box.getChildren().add(new Label(ruler.getName(), imageNode(EU4_ICON_RULER, CLASS_RULER_ICON)));
        } else {
            box.getChildren().add(imageNode(EU4_ICON_HEIR, CLASS_RULER_ICON));
        }

        box.alignmentProperty().set(Pos.CENTER);
        box.getChildren().add(createRulerStatsNode(ruler));
        box.getStyleClass().add(CLASS_RULER);
        box.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE);
        return box;
    }

    private static Node createRulerStatsNode(Eu4SavegameInfo.Ruler ruler) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Label adm = new Label(ruler.getAdm() + "  ", imageNode(EU4_ICON_ADM, CLASS_POWER_ICON));
        box.getChildren().add(adm);

        Label dip = new Label(ruler.getDip() + "  ", imageNode(EU4_ICON_DIP, CLASS_POWER_ICON));
        box.getChildren().add(dip);

        Label mil = new Label(String.valueOf(ruler.getMil()), imageNode(EU4_ICON_MIL, CLASS_POWER_ICON));
        box.getChildren().add(mil);
        return box;
    }

    private static String getCountryTooltip(Set<GameTag> tags) {
        StringBuilder b = new StringBuilder();
        for (GameTag t : tags) {
            b.append(GameInstallation.EU4.getCountryName(t));
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private static void createDiplomacyRow(JFXMasonryPane pane, Node icon, Set<GameTag> tags, String tooltipStart, String none, String style) {
        if (tags.size() == 0) {
            return;
        }

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(icon);
        for (GameTag tag : tags) {
            Node n = getImageForTag(tag, CLASS_TAG_ICON);
            box.getChildren().add(n);
        }
        box.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE);
        box.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        box.getStyleClass().add(style);
        Tooltip.install(box, tooltip(tooltipStart + (tags.size() > 0 ? getCountryTooltip(tags) : none)));
        pane.getChildren().add(box);
    }
}
