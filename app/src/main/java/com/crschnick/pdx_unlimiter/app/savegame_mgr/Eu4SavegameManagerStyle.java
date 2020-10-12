package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Eu4SavegameManagerStyle {

    private static Tooltip tooltip(String text) {

        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.ZERO);
        return t;
    }

    public static HBox createRulerLabel(Eu4SavegameInfo.Ruler ruler, boolean isRuler) {
        HBox box = new HBox();
        box.setSpacing(0);
        box.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(box, tooltip((isRuler ? "Ruler: " : "Heir: ") + ruler.getName()));
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        if (isRuler) {
            var i = Eu4ImageLoader.loadInterfaceImage("tab_domestic_court.dds");
            i.setViewport(new Rectangle2D(8, 10, 30, 30));
            box.getChildren().add(i);
        } else {
            box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("monarch_heir_crown_icon.dds"));
        }

        Label adm = new Label(" " + ruler.getAdm());
        adm.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        box.getChildren().add(adm);
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_administrative_in_text.dds"));


        Label dip = new Label("/ " + ruler.getDip());
        dip.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        box.getChildren().add(dip);
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_diplomatic_in_text.dds"));

        Label mil = new Label("/ " + ruler.getMil());
        mil.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        box.getChildren().add(mil);
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_military_in_text.dds"));
        box.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        return box;
    }

    public static GridPane createCampaignEntryNode(Eu4Campaign.Entry e,
                                                   ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry,
                                                   Consumer<Eu4Campaign.Entry> onOpen,
                                                   Consumer<Eu4Campaign.Entry> delete) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setMaxHeight(120);
        grid.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");

        TextField name = new TextField();
        name.setStyle("-fx-background-color: #444444; -fx-font-size: 18px; -fx-text-fill: white;");
        name.textProperty().bindBidirectional(e.nameProperty());
        grid.add(name, 0, 0, 3, 1);


        Button open = new Button("\uD83D\uDCBE");
        open.setOnMouseClicked((m) -> {
            onOpen.accept(e);
        });
        open.setAlignment(Pos.CENTER_LEFT);
        open.setStyle("-fx-background-color: #aaaa66;-fx-text-fill: white; -fx-font-size: 18px;");

        Button del = new Button("\u2715");
        del.setOnMouseClicked((m) -> {
            delete.accept(e);
        });
        del.setAlignment(Pos.CENTER_RIGHT);
        del.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;-fx-text-fill: white; -fx-font-size: 16px;");

        HBox h = new HBox(open, del);
        grid.add(h, 3, 0);

        Label date = new Label(e.getInfo().getDate().toDisplayString());
        date.setMinWidth(150);
        date.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 16px;");

        grid.add(date, 0, 1);
        grid.add(createRulerLabel(e.getInfo().getRuler(), true), 0, 2);
        if (e.getInfo().getHeir().isPresent()) {
            grid.add(createRulerLabel(e.getInfo().getHeir().get(), false), 0, 3);
        }


        Label version = new Label("v" + e.getInfo().getVersion().toString());
        version.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        grid.add(version, 0, 4);

        int wars = 0;
        for (Eu4SavegameInfo.War war : e.getInfo().getWars()) {
            if (wars >= 3) {
                break;
            }
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", war.getEnemies(), "Fighting in the " + war.getTitle() + " against ", ""), 1 + wars, 1);
            wars++;
        }
        grid.add(createDiplomacyRow("icon_alliance.dds", e.getInfo().getAllies(), "Allies: ", "None"), 1, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_royalmarriage.dds", e.getInfo().getMarriages(), "Royal marriages: ", "None"), 2, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_guaranting.dds", e.getInfo().getGuarantees(), "Guarantees: ", "None"), 3, 2);
        grid.add(createDiplomacyRow("icon_vassal.dds", e.getInfo().getVassals(), "Vassals: ", "None"), 1, 3);
        grid.add(createDiplomacyRow("icon_vassal.dds", e.getInfo().getJuniorPartners(), "Personal union junior partners: ", "none"), 2, 3);
        grid.add(createDiplomacyRow("subject_tributary_icon.dds", e.getInfo().getTributaryJuniors(), "Tributaries: ", "None"), 3, 3);
        grid.add(createDiplomacyRow("icon_march.dds", e.getInfo().getMarches(), "Marches: ", "None"), 1, 4);
        grid.add(createDiplomacyRow("icon_truce.dds", e.getInfo().getTruces().keySet(), "Truces: ", "None"), 2, 4);
        if (e.getInfo().getSeniorPartner().isPresent()) {
            grid.add(createDiplomacyRow("icon_alliance.dds", Set.of(e.getInfo().getSeniorPartner().get()), "Under personal union with ", "no country"), 4, 4);
        }

        grid.getProperties().put("entry", e);
        selectedEntry.addListener((c, o, n) -> {
            if (!n.isPresent() || !n.get().equals(e)) {
                grid.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");
            } else {
                grid.setStyle("-fx-background-color: #666666; -fx-border-color: #44bb44; -fx-border-width: 3px;");
            }
        });
        grid.setOnMouseClicked((m) -> {
            selectedEntry.setValue(Optional.of((Eu4Campaign.Entry) grid.getProperties().get("entry")));
        });
        return grid;
    }

    private static String getCountryTooltip(Set<String> tags) {
        StringBuilder b = new StringBuilder();
        for (String s : tags) {
            b.append(Installation.EU4.get().getCountryName(s));
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private static Node createDiplomacyRow(String icon, Set<String> tags, String tooltipStart, String none) {
        HBox box = new HBox();
        box.setSpacing(3);
        box.setAlignment(Pos.CENTER_LEFT);
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage(icon));
        for (String tag : tags) {
            ImageView n = Eu4ImageLoader.loadFlagImage(tag, 20);
            box.getChildren().add(n);
        }
        box.setStyle("-fx-background-color: #777777; -fx-border-color: #666666; -fx-border-width: 3px;");
        box.setPadding(new Insets(0, 5, 0, 0));
        box.setMaxHeight(40);
        Tooltip.install(box, tooltip(tooltipStart + (tags.size() > 0 ? getCountryTooltip(tags) : none)));
        box.minWidthProperty().setValue(120);
        return box;
    }

    private static void sortSavegames(VBox list, Eu4Campaign c) {
        List<Node> newOrder = c.getSavegames().stream().map(e -> (list.getChildren().stream()
                .filter(x -> x.getProperties().get("entry").equals(e))).findAny().get()).collect(Collectors.toList());
        Collections.reverse(newOrder);
        list.getChildren().setAll(newOrder);
    }

    private static VBox createSavegameList(Optional<Eu4Campaign> c,
                                          ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry,
                                           Consumer<Eu4Campaign.Entry> open,
                                          Consumer<Eu4Campaign.Entry> delete) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        ScrollPane pane = new ScrollPane(grid);
        pane.setFitToWidth(true);
        pane.setStyle("-fx-focus-color: transparent;");

        if (!c.isPresent()) {
            return grid;
        }

        for (Eu4Campaign.Entry e : c.get().getSavegames()) {
            grid.getChildren().add(createCampaignEntryNode(e, selectedEntry, open, delete));
        }
        sortSavegames(grid, c.get());

        c.get().getSavegames().addListener((SetChangeListener<? super Eu4Campaign.Entry>) (change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                        grid.getChildren().add(createCampaignEntryNode(change.getElementAdded(), selectedEntry, open, delete));
                } else {
                    grid.getChildren().removeIf((x) -> change.getElementRemoved().equals(x.getProperties().get("entry")));
                }

               sortSavegames(grid, c.get());
            });
        });

        return grid;
    }

    public static ScrollPane createSavegameScrollPane(ObjectProperty<Optional<Eu4Campaign>> selectedCampaign,
                                                      ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry,
                                                      Consumer<Eu4Campaign.Entry> open,
                                                      Consumer<Eu4Campaign.Entry> delete) {
        ScrollPane pane = new ScrollPane();
        pane.setFitToWidth(true);
        pane.setStyle("-fx-focus-color: transparent;");
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        selectedCampaign.addListener((ch,o,n) -> pane.setContent(createSavegameList(n, selectedEntry, open, delete)));

        return pane;
    }

    private static Node createCampaignButton(Eu4Campaign c, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        Button del = new Button("\u2715");
        del.setOnMouseClicked((m) -> {
            onDelete.accept(c);
        });
        del.setAlignment(Pos.CENTER);
        del.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;-fx-text-fill: white; -fx-font-size: 16px;");


        TextField name = new TextField(SavegameCache.EU4_CACHE.getNames().getOrDefault(c.getCampaignId(), Installation.EU4.get().getCountryName(c.getTag())));
        name.setStyle("-fx-background-color: #444444; -fx-font-size: 16px; -fx-text-fill: white;-fx-border-radius: 0px;");
        name.textProperty().bindBidirectional(c.nameProperty());

        Label date = new Label(c.getDate().toString());
        date.setStyle("-fx-text-fill: white;");
        c.dateProperty().addListener((change,o,n) -> {
            Platform.runLater(() -> {
                date.setText(n.toString());
            });
        });

        HBox top = new HBox();
        top.setSpacing(3);
        top.getChildren().add(name);
        top.getChildren().add(del);

        VBox b = new VBox();
        b.setSpacing(3);
        b.getChildren().add(top);
        b.getChildren().add(date);

        ImageView w = Eu4ImageLoader.loadFlagImage(c.getTag(), 60);
        HBox btn = new HBox();
        btn.setSpacing(5);
        btn.getChildren().add(w);
        btn.getChildren().add(b);
        c.tagProperty().addListener((change, o, n) -> {
            Platform.runLater(() -> {
                btn.getChildren().set(0, Eu4ImageLoader.loadFlagImage(n, 60));
            });
        });

        btn.setOnMouseClicked((m) -> selectedCampaign.setValue(Optional.of(c)));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(5, 5, 5, 5));
        btn.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        btn.setBorder(Border.EMPTY);
        btn.getProperties().put("campaign", c);


        return btn;
    }

    private static void sortCampaignList(VBox list, ObservableSet<Eu4Campaign> campaigns) {
        List<Node> newOrder = campaigns.stream().map(c -> (list.getChildren().stream()
                .filter(x -> x.getProperties().get("campaign").equals(c))).findAny().get()).collect(Collectors.toList());
        Collections.reverse(newOrder);
        list.getChildren().setAll(newOrder);
    }

    public static Node createCampaignList(ObservableSet<Eu4Campaign> campaigns, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        for (Eu4Campaign d : campaigns) {
            grid.getChildren().add(createCampaignButton(d, selectedCampaign, onDelete));
            d.lastPlayedProperty().addListener((change, o, n) -> {
                Platform.runLater(() -> {
                    sortCampaignList(grid, campaigns);
                });
            });
        }
        sortCampaignList(grid, campaigns);

        campaigns.addListener((SetChangeListener<? super Eu4Campaign>) (change) -> {
            Platform.runLater(() -> {
                if (change.wasAdded()) {
                    grid.getChildren().add(createCampaignButton(change.getElementAdded(), selectedCampaign, onDelete));
                } else {
                    grid.getChildren().removeIf((x) -> change.getElementRemoved().equals(x.getProperties().get("campaign")));
                }
                sortCampaignList(grid, campaigns);
            });
        });

        ScrollPane pane = new ScrollPane(grid);
        pane.setMinViewportWidth(200);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return pane;
    }

    public static Node createActiveStatusBar(PdxApp app) {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-border-color: #339933; -fx-background-color: #33aa33;");

        ImageView icon = new ImageView(Eu4ImageLoader.loadImage(Installation.EU4.get().getPath().resolve("launcher-assets").resolve("icon.png")));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        Label text = new Label("Eu4", icon);
        text.setAlignment(Pos.BOTTOM_CENTER);
        text.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setLeft(text);

        Label status = new Label("Status: Running");
        status.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setCenter(status);

        Button b = new Button("Kill");
        b.setOnMouseClicked((m) -> {
            app.kill();
        });
        b.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setRight(b);
        return pane;
    }

    public static Node createInactiveStatusBar(ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, ObjectProperty<Optional<Eu4Campaign.Entry>> save, Consumer<Eu4Campaign.Entry> onLaunch) {

        BorderPane pane = new BorderPane();
            pane.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;");


        ImageView icon = new ImageView(Eu4ImageLoader.loadImage(Installation.EU4.get().getPath().resolve("launcher-assets").resolve("icon.png")));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        Label text = new Label("eu4", icon);
        text.setAlignment(Pos.BOTTOM_CENTER);
        text.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setLeft(text);

        Label status = new Label("Status: Stopped");
        status.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setCenter(status);

        Button b = new Button("Launch");
        b.disableProperty().setValue(true);
        b.setOnMouseClicked((m) -> {
            if (!b.disableProperty().get()) {
                onLaunch.accept(save.get().get());
            }
        });
        save.addListener((val, old , n) -> {
            if (n.isPresent() && GameVersion.areCompatible(Installation.EU4.get().getVersion(), n.get().getInfo().getVersion())) {
                b.disableProperty().setValue(false);
                b.setStyle("-fx-opacity: 1; -fx-border-color: #339933FF; -fx-background-radius: 0; -fx-border-radius: 0; -fx-background-color: #33aa33FF;-fx-text-fill: white; -fx-font-size: 18px;");
            } else {
                b.disableProperty().setValue(true);
                b.setStyle("-fx-opacity: 0.5; -fx-border-color: #339933FF; -fx-background-radius: 0; -fx-border-radius: 0; -fx-background-color: #33aa33FF;-fx-text-fill: white; -fx-font-size: 18px;");
            }
        });
        b.setStyle("-fx-opacity: 0.5; -fx-border-color: #339933FF; -fx-background-radius: 0; -fx-border-radius: 0; -fx-background-color: #33aa33FF;-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setRight(b);

        return pane;
    }
}
