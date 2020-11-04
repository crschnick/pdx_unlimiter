package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameTag;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.*;

public class GuiEu4CampaignEntry {

    private static String CLASS_CAMPAIGN_ENTRY = "campaign-entry";
    private static String CLASS_DIPLOMACY_ROW = "diplomacy-row";
    private static String CLASS_CAMPAIGN_ENTRY_NODE = "node";
    private static String CLASS_DATE = "date";
    private static String CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER = "node-container";
    private static String CLASS_VERSION_OK = "version-ok";
    private static String CLASS_VERSION_INCOMPATIBLE = "version-incompatible";
    private static String CLASS_RULER = "ruler";
    private static String CLASS_WAR = "war";
    private static String CLASS_ALLIANCE = "alliance";
    private static String CLASS_MARRIAGE = "marriage";
    private static String CLASS_GUARANTEE = "guarantee";
    private static String CLASS_VASSAL = "vassal";
    private static String CLASS_TRUCE = "vassal";
    private static String CLASS_IMAGE_ICON = "image-icon";
    private static String CLASS_POWER_ICON = "power-icon";
    private static String CLASS_RULER_ICON = "ruler-icon";
    private static String CLASS_TAG_ICON = "tag-icon";
    private static String CLASS_ENTRY_BAR = "entry-bar";
    private static String CLASS_ENTRY_LOADING = "entry-loading";

    private static Tooltip tooltip(String text) {

        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.millis(100));
        t.setStyle("-fx-font-size: 14px;");
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

    private static Node getImageForTag(GameTag tag, String styleClass) {
        Node n = getImageForTagName(tag.getTag(), styleClass);
        if (tag.isCustom()) {
            java.awt.Color c = tag.getCountryColor();
            ((Label)n).setBackground(new Background(
                    new BackgroundFill(Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), 1), CornerRadii.EMPTY, Insets.EMPTY)));
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

    private static Node createSavegameInfoNode(Eu4Campaign.Entry entry) {
        StackPane stack = new StackPane();
        JFXMasonryPane grid =
                new JFXMasonryPane();
        grid.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER);
        grid.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);
        grid.setHSpacing(10);
        grid.setVSpacing(10);
        grid.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> 3 * grid.getCellHeight() + 2 * grid.getVSpacing() + grid.getPadding().getBottom() + grid.getPadding().getTop(), grid.paddingProperty()));
        grid.setLimitRow(3);

        JFXSpinner loading = new JFXSpinner();
        loading.getStyleClass().add(CLASS_ENTRY_LOADING);
        stack.getChildren().add(grid);
        if (entry.getInfo().isPresent()) {
            fillNodeContainer(entry.getInfo().get(), grid);
        } else {
            stack.getChildren().add(loading);
        }

        AtomicBoolean load = new AtomicBoolean(false);
        stack.layoutBoundsProperty().addListener((c,o,n) -> {
            if (stack.localToScreen(0, 0) == null) {
                return;
            }

            if (stack.localToScreen(0, 0).getY() < PdxuApp.getApp().getScene().getWindow().getHeight() && !load.get()) {
                load.set(true);
                System.out.println("load");
            }
        });

        entry.infoProperty().addListener((change) -> {
            Platform.runLater(() -> {
                stack.getChildren().remove(loading);
                fillNodeContainer(entry.getInfo().get(), grid);
            });
        });

        return stack;
    }

    private static void fillNodeContainer(Eu4SavegameInfo info, JFXMasonryPane grid) {
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
        if (GameVersion.areCompatible(GameInstallation.EU4.getVersion(), info.getVersion())) {
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

    public static Node createCampaignEntryNode(Eu4Campaign.Entry e, ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry) {
        VBox main = new VBox();
        main.setAlignment(Pos.CENTER);
        main.setFillWidth(true);
        main.getProperties().put("entry", e);
        Node n = getImageForTagName(e.getCampaign().getTag(), CLASS_TAG_ICON);
        Label l = new Label(e.getName());
        l.getStyleClass().add(CLASS_DATE);

        TextField name = new TextField();
        name.getStyleClass().add("text-field");
        name.textProperty().bindBidirectional(e.nameProperty());

        Button open = new JFXButton();
        open.setOnMouseClicked((m) -> {
            try {
                Desktop.getDesktop().open(SavegameCache.EU4_CACHE.getPath(e).toFile());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        Tooltip.install(open, tooltip("Open savegame location"));
        open.setGraphic(new FontIcon());
        open.getStyleClass().add("open-button");


        Button achievements = new JFXButton();
        achievements.setGraphic(new FontIcon());
        achievements.setOnMouseClicked((m) -> {
            e.getInfo().ifPresent(s -> AchievementWindow.showAchievementList(e));
        });
        Tooltip.install(open, tooltip("Achievements"));
        achievements.getStyleClass().add("achievement-button");


        Button del = new JFXButton();
        del.setGraphic(new FontIcon());
        del.setOnMouseClicked((m) -> {
            if (DialogHelper.showSavegameDeleteDialog()) {
                SavegameCache.EU4_CACHE.delete(e);
            }
        });
        Tooltip.install(del, tooltip("Delete savegame"));
        del.getStyleClass().add("delete-button");

        HBox bar = new HBox(n, l, name, achievements, open, del);
        bar.setAlignment(Pos.CENTER);
        bar.getStyleClass().add(CLASS_ENTRY_BAR);
        main.getChildren().add(bar);
        Node content = createSavegameInfoNode(e);
        main.getChildren().add(content);
        main.getStyleClass().add(CLASS_CAMPAIGN_ENTRY);
        return main;
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
