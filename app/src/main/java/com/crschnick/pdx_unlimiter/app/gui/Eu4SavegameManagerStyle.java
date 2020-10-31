package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4SavegameImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.GameTag;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Eu4SavegameManagerStyle {

    private static Tooltip tooltip(String text) {

        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.millis(100));
        t.setStyle("-fx-font-size: 14px;");
        return t;
    }

    private static Node getImageForTagName(String tagName, int size) {
        if (GameInstallation.EU4.isPreexistingCoutry(tagName)) {
            return Eu4ImageLoader.loadFlagImage(tagName, size);
        } else {
            Label l = new Label("?");
            l.setStyle("-fx-text-fill: white; -fx-font-size: 30px;");
            l.minWidthProperty().setValue(60);
            l.prefHeightProperty().setValue(60);
            l.alignmentProperty().set(Pos.CENTER);
            return l;
        }
    }

    private static Node getTagImage(GameTag tag, int size) {
        if (!tag.isCustom()) {
            return Eu4ImageLoader.loadFlagImage(tag.getTag(), size);
        } else {
            java.awt.Color c = tag.getCountryColor();
            Label p = new Label();
            p.minWidthProperty().setValue(size);
            p.minHeightProperty().setValue(size);
            p.setBackground(new Background(new BackgroundFill(Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), 1), CornerRadii.EMPTY, Insets.EMPTY)));
            return p;
        }
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

    private static Node createSavegameInfoNode(Eu4SavegameInfo info) {
        GridPane grid = new GridPane();
        grid.alignmentProperty().set(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setMaxHeight(120);

        Label date = new Label(info.getDate().toDisplayString());
        date.setMinWidth(150);
        date.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 16px;");

        grid.add(date, 1, 1);

        if (info.isObserver()) {
            return grid;
        }

        grid.add(createRulerLabel(info.getRuler(), true), 0, 2);
        if (info.getHeir().isPresent()) {
            grid.add(createRulerLabel(info.getHeir().get(), false), 0, 3);
        }

        HBox status = new HBox();
        status.setStyle("-fx-border-width: 3px;");
        status.setSpacing(4);
        Node n = getTagImage(info.getCurrentTag(), 25);
        n.setTranslateX(3);
        n.setTranslateY(3);
        Tooltip.install(n, tooltip("Playing as " + GameInstallation.EU4.getCountryName(info.getCurrentTag())));
        status.getChildren().add(n);
        status.getChildren().add(new Pane());
        if (info.isIronman()) {
            ImageView v = Eu4ImageLoader.loadInterfaceImage("ironman_icon.dds");
            status.getChildren().add(v);
            v.setFitWidth(22);
            v.setFitHeight(32);
            Tooltip.install(v, tooltip("Ironman savegame"));
        }
        if (info.isRandomNewWorld()) {
            Predicate<Integer> s = (Integer rgb) -> {
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                boolean gold = (r > 100 && g > 87 && b < 100 && Math.max(r, Math.max(g, b) - 2) == r);
                boolean blue = Math.max(r, Math.max(g, b)) < 135;
                if (blue || gold) {
                    return false;
                }
                return true;
            };
            Image i = Eu4ImageLoader.loadImage(GameInstallation.EU4.getPath().resolve("gfx/interface/").resolve("frontend_random_world.dds"), s);
            ImageView v = new ImageView(i);
            v.setTranslateY(2);
            v.setViewport(new Rectangle2D(14, 0, 33, 30));
            Tooltip.install(v, tooltip("Random new world enabled"));
            status.getChildren().add(v);

        }
        if (info.isCustomNationInWorld()) {
            Predicate<Integer> s = (Integer rgb) -> {
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                boolean blue = Math.max(r, Math.max(g, b)) < 142;
                if (blue) {
                    return false;
                }
                return true;
            };
            Image i = Eu4ImageLoader.loadImage(GameInstallation.EU4.getPath().resolve("gfx/interface/").resolve("frontend_custom_nation.dds"), s);
            ImageView v = new ImageView(i);
            v.setViewport(new Rectangle2D(20, 5, 21, 21));
            v.setTranslateY(4);
            v.setFitWidth(22);
            v.setFitHeight(24);
            Tooltip.install(v, tooltip("A custom nation exists in the world"));
            status.getChildren().add(v);
        }
        if (info.isReleasedVassal()) {
            ImageView v = Eu4ImageLoader.loadInterfaceImage("release_nation_icon.dds");
            v.setViewport(new Rectangle2D(37, 0, 36, 30));
            v.prefWidth(32);
            v.prefHeight(32);
            Tooltip.install(v, tooltip("Is playing as a released vassal"));
            status.getChildren().add(v);
        }
        grid.add(status, 0, 1);

        Label version;
        if (GameVersion.areCompatible(GameInstallation.EU4.getVersion(), info.getVersion())) {
            version = new Label("v" + info.getVersion().toString());
            Tooltip.install(version, tooltip("Compatible version"));
        } else {
            version = new Label("v" + info.getVersion().toString(), Eu4ImageLoader.loadInterfaceImage("incompatible_warning_icon.dds"));
            Tooltip.install(version, tooltip("Incompatible savegame version"));
        }
        version.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        grid.add(version, 0, 4);
        GridPane.setHalignment(version, HPos.CENTER);
        GridPane.setValignment(version, VPos.CENTER);

        int wars = 0;
        for (Eu4SavegameInfo.War war : info.getWars()) {
            if (wars >= 2) {
                break;
            }
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", war.getEnemies(), "Fighting in the " + war.getTitle() + " against ", ""), 2 + wars, 1);
            wars++;
        }
        // save_game_dir_icon
        grid.add(createDiplomacyRow("icon_alliance.dds", info.getAllies(), "Allies: ", "None"), 1, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_royalmarriage.dds", info.getMarriages(), "Royal marriages: ", "None"), 2, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_guaranting.dds", info.getGuarantees(), "Guarantees: ", "None"), 3, 2);
        grid.add(createDiplomacyRow("icon_vassal.dds", info.getVassals(), "Vassals: ", "None"), 1, 3);
        grid.add(createDiplomacyRow("icon_vassal.dds", info.getJuniorPartners(), "Personal union junior partners: ", "none"), 2, 3);
        grid.add(createDiplomacyRow("subject_tributary_icon.dds", info.getTributaryJuniors(), "Tributaries: ", "None"), 3, 3);
        grid.add(createDiplomacyRow("icon_march.dds", info.getMarches(), "Marches: ", "None"), 1, 4);
        grid.add(createDiplomacyRow("icon_truce.dds", info.getTruces().keySet(), "Truces: ", "None"), 2, 4);
        if (info.getSeniorPartner().isPresent()) {
            grid.add(createDiplomacyRow("icon_vassal.dds.dds", Set.of(info.getSeniorPartner().get()), "Under personal union with ", "no country"), 4, 4);
        }
        return grid;
    }

    public static Node createCampaignEntryNode(Eu4Campaign.Entry e,
                                               ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry,
                                               Consumer<Eu4Campaign.Entry> onOpen,
                                               Consumer<Eu4Campaign.Entry> delete) {
        VBox main = new VBox();
        main.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");
        main.getProperties().put("entry", e);
        selectedEntry.addListener((c, o, n) -> {
            if (!n.isPresent() || !n.get().equals(e)) {
                main.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");
            } else {
                main.setStyle("-fx-background-color: #666666; -fx-border-color: #44bb44; -fx-border-width: 3px;");
            }
        });
        main.setOnMouseClicked((m) -> {
            selectedEntry.setValue(Optional.of((Eu4Campaign.Entry) main.getProperties().get("entry")));
        });

        TextField name = new TextField();
        name.setStyle("-fx-background-color: #444444; -fx-font-size: 18px; -fx-text-fill: white;");
        name.textProperty().bindBidirectional(e.nameProperty());

        Button open = new Button("\uD83D\uDCBE");
        open.setOnMouseClicked((m) -> {
            onOpen.accept(e);
        });
        open.setAlignment(Pos.CENTER_LEFT);
        open.setStyle("-fx-background-color: #aaaa66;-fx-text-fill: white; -fx-font-size: 18px;");
        Tooltip.install(open, tooltip("Open savegame location"));


        Button achievements = new Button("\uD83C\uDFC6");
        achievements.setOnMouseClicked((m) -> {
            e.getInfo().ifPresent(s -> AchievementWindow.showAchievementList(e));
        });
        achievements.setAlignment(Pos.CENTER_LEFT);
        achievements.setStyle("-fx-background-color: #88aa88;-fx-text-fill: white; -fx-font-size: 18px;");
        Tooltip.install(open, tooltip("Achievements"));


        Button del = new Button("\u2715");
        del.setOnMouseClicked((m) -> {
            if (DialogHelper.showSavegameDeleteDialog()) {
                delete.accept(e);
            }
        });
        del.setAlignment(Pos.CENTER_RIGHT);
        del.setStyle("-fx-background-color: #aa3333;-fx-text-fill: white; -fx-font-size: 16px;");
        Tooltip.install(del, tooltip("Delete savegame"));

        HBox bar = new HBox(name, achievements, open, del);
        bar.setSpacing(5);
        HBox.setHgrow(name, Priority.SOMETIMES);
        main.getChildren().add(bar);
        if (e.getInfo().isPresent()) {
            Node content = createSavegameInfoNode(e.getInfo().get());
            main.getChildren().add(content);
        } else {
            e.infoProperty().addListener(change -> {
                Platform.runLater(() -> main.getChildren().add(createSavegameInfoNode(e.getInfo().get())));
            });
        }
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

    private static Node createDiplomacyRow(String icon, Set<GameTag> tags, String tooltipStart, String none) {
        HBox box = new HBox();
        box.setSpacing(3);
        box.setAlignment(Pos.CENTER_LEFT);
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage(icon));
        for (GameTag tag : tags) {
            Node n = getTagImage(tag, 20);
            box.getChildren().add(n);
        }
        box.setStyle("-fx-background-color: #777777; -fx-border-color: #666666; -fx-border-width: 3px;");
        box.setPadding(new Insets(0, 5, 0, 0));
        box.setMaxHeight(40);
        Tooltip.install(box, tooltip(tooltipStart + (tags.size() > 0 ? getCountryTooltip(tags) : none)));
        box.minWidthProperty().setValue(120);
        return box;
    }

    private static void updateSavegames(VBox list, Eu4Campaign c,
                                        ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry,
                                        Consumer<Eu4Campaign.Entry> open,
                                        Consumer<Eu4Campaign.Entry> delete) {
        List<Node> newOrder = c.getSavegames().stream()
                .sorted(Comparator.comparingLong(e -> GameDate.toLong(e.getDate().or(() -> Optional.of(GameDate.fromLong(0))).get())))
                .map(e -> createCampaignEntryNode(e, selectedEntry, open, delete))
                .collect(Collectors.toList());
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
            e.infoProperty().addListener((change, o, n) -> {
                Platform.runLater(() -> {
                    updateSavegames(grid, c.get(), selectedEntry, open, delete);
                });
            });
        }
        c.get().getSavegames().addListener((SetChangeListener<? super Eu4Campaign.Entry>) (change) -> {
            Platform.runLater(() -> {
                updateSavegames(grid, c.get(), selectedEntry, open, delete);
            });
        });
        updateSavegames(grid, c.get(), selectedEntry, open, delete);

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

        selectedCampaign.addListener((ch, o, n) -> pane.setContent(createSavegameList(n, selectedEntry, open, delete)));

        return pane;
    }

    private static Node createCampaignButton(Eu4Campaign c, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        Button del = new Button("\u2715");
        del.setOnMouseClicked((m) -> {
            if (DialogHelper.showCampaignDeleteDialog()) {
                onDelete.accept(c);
            }
        });
        del.setAlignment(Pos.CENTER);
        del.setStyle("-fx-border-color: #993333; -fx-background-color: #aa3333;-fx-text-fill: white; -fx-font-size: 16px;");
        Tooltip.install(del, tooltip("Delete campaign"));


        TextField name = new TextField(c.getName());
        name.setStyle("-fx-background-color: #444444; -fx-font-size: 16px; -fx-text-fill: white;-fx-border-radius: 0px;");
        name.textProperty().bindBidirectional(c.nameProperty());

        Label date = new Label(c.getDate().toString());
        date.setStyle("-fx-text-fill: white;");
        c.dateProperty().addListener((change, o, n) -> {
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

        Node w = getImageForTagName(c.getTag(), 60);
        HBox btn = new HBox();
        btn.setSpacing(5);
        btn.getChildren().add(w);
        btn.getChildren().add(b);
        c.tagProperty().addListener((change, o, n) -> {
            Platform.runLater(() -> {
                btn.getChildren().set(0, getImageForTagName(n, 60));
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

    private static void sortCampaignList(VBox list, ObservableSet<Eu4Campaign> campaigns, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        List<Node> newOrder = campaigns.stream()
                .sorted(Comparator.comparing(Eu4Campaign::getLastPlayed))
                .map(c -> createCampaignButton(c, selectedCampaign, onDelete))
                .collect(Collectors.toList());
        Collections.reverse(newOrder);
        list.getChildren().setAll(newOrder);
    }

    public static Node createCampaignList(ObservableSet<Eu4Campaign> campaigns, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        for (Eu4Campaign d : campaigns) {
            d.lastPlayedProperty().addListener((change, o, n) -> {
                Platform.runLater(() -> {
                    sortCampaignList(grid, campaigns, selectedCampaign, onDelete);
                });
            });
        }
        campaigns.addListener((SetChangeListener<? super Eu4Campaign>) (change) -> {
            Platform.runLater(() -> {
                if (campaigns.size() == 0) {

                }
                sortCampaignList(grid, campaigns, selectedCampaign, onDelete);
            });
        });
        sortCampaignList(grid, campaigns, selectedCampaign, onDelete);

        ScrollPane pane = new ScrollPane(grid);
        pane.setMinViewportWidth(200);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return pane;
    }

    public static Node createNoCampaignNode() {
        StackPane p = new StackPane();
        Button b = new Button("Import EU4 savegames");
        b.setOnMouseClicked(e -> {
            if (DialogHelper.showImportSavegamesDialog()) {
                Eu4SavegameImporter.importAllSavegames();
            }
        });
        p.getChildren().add(b);
        StackPane.setAlignment(b, Pos.CENTER);
        return p;
    }

    public static Node createActiveStatusBar(PdxApp app) {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-border-width: 0; -fx-background-color: #337733;");

        ImageView icon = new ImageView(Eu4ImageLoader.loadImage(GameInstallation.EU4.getPath().resolve("launcher-assets").resolve("icon.png")));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        Label text = new Label("Europa Universalis 4", icon);
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
        Tooltip.install(b, tooltip("Kill the game. This will prevent it from saving your current game!"));

        return pane;
    }

    public static Node createInactiveStatusBar(ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, ObjectProperty<Optional<Eu4Campaign.Entry>> save, Consumer<Eu4Campaign.Entry> onLaunch) {

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-border-width: 0; -fx-background-color: #555555;");


        ImageView icon = new ImageView(Eu4ImageLoader.loadImage(GameInstallation.EU4.getPath().resolve("launcher-assets").resolve("icon.png")));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        Label text = new Label("Europa Universalis 4", icon);
        text.setAlignment(Pos.BOTTOM_CENTER);
        text.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setLeft(text);

        Label status = new Label("Status: Stopped");
        status.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setCenter(status);

        Button b = new Button("Launch");
        b.setOnMouseClicked((m) -> {
            if (save.get().isPresent() && GameVersion.areCompatible(GameInstallation.EU4.getVersion(),
                    save.get().get().getInfo().get().getVersion())) {
                onLaunch.accept(save.get().get());
            }
        });

        ChangeListener<Optional<Eu4Campaign.Entry>> l = (val, old, n) -> {
            if (n.isPresent() && GameVersion.areCompatible(GameInstallation.EU4.getVersion(), n.get().getInfo().get().getVersion())) {
                b.setStyle("-fx-opacity: 1; -fx-border-color: #339933FF; -fx-background-radius: 0; -fx-border-radius: 0; -fx-background-color: #33aa33FF;-fx-text-fill: white; -fx-font-size: 18px;");
                Tooltip.install(b, tooltip("Launch savegame " + n.get().getName()));
            } else {
                b.setStyle("-fx-opacity: 0.5; -fx-border-color: #339933FF; -fx-background-radius: 0; -fx-border-radius: 0; -fx-background-color: #33aa33FF;-fx-text-fill: white; -fx-font-size: 18px;");
                Tooltip.install(b, tooltip("Selected savegame version is not compatible to the game version."));
            }
        };
        save.addListener(l);
        l.changed(save, save.get(), save.get());
        pane.setRight(b);

        return pane;
    }

    private static MenuBar createMenuBar() {
        Menu menu = new Menu("File");

        MenuItem menuItem0 = new MenuItem("Export savegames...");
        menuItem0.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showExportDialog(false);
            path.ifPresent(SavegameCache::exportSavegameDirectory);
        });


        MenuItem l = new MenuItem("Import latest savegame (CTRL+SHIFT+I)");
        l.setOnAction((a) -> {
            Eu4SavegameImporter.importLatestSavegame();

        });
        menu.getItems().add(l);

        MenuItem i = new MenuItem("Import all savegames...");
        i.setOnAction((a) -> {
            if (DialogHelper.showImportSavegamesDialog()) {
                Eu4SavegameImporter.importAllSavegames();
            }
        });
        menu.getItems().add(i);

        menu.getItems().add(menuItem0);


        Menu settings = new Menu("Settings");
        MenuItem c = new MenuItem("Change settings");
        c.setOnAction((a) -> {
            DialogHelper.showSettings();
        });
        settings.getItems().add(c);


        Menu savegames = new Menu("Storage");

        MenuItem menuItem1 = new MenuItem("Import storage...");
        menuItem1.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showImportArchiveDialog();
            path.ifPresent(SavegameCache::importSavegameCache);
        });
        savegames.getItems().add(menuItem1);

        MenuItem menuItem2 = new MenuItem("Export storage...");
        menuItem2.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showExportDialog(true);
            path.ifPresent(SavegameCache::exportSavegameCache);
        });
        savegames.getItems().add(menuItem2);

        MenuItem u = new MenuItem("Update all savegames...");
        u.setOnAction((a) -> {
            if (DialogHelper.showUpdateAllSavegamesDialog()) {
                SavegameCache.EU4_CACHE.updateAllData();
            }
        });
        savegames.getItems().add(u);

        MenuItem sd = new MenuItem("Open storage directory");
        sd.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(SavegameCache.EU4_CACHE.getPath().toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
        savegames.getItems().add(sd);

        MenuItem backups = new MenuItem("Open backup location");
        backups.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(SavegameCache.EU4_CACHE.getBackupPath().toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
        savegames.getItems().add(backups);


        Menu about = new Menu("Help");
        MenuItem wiki = new MenuItem("EU4 Wiki");
        wiki.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://eu4.paradoxwikis.com/Europa_Universalis_4_Wiki"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem src = new MenuItem("Contribute");
        src.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/crschnick/pdx_unlimiter/"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem is = new MenuItem("Report an issue");
        is.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/crschnick/pdx_unlimiter/issues"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem lc = new MenuItem("License");
        lc.setOnAction((a) -> {
            DialogHelper.showText("License", "License", "license.txt");
        });
        MenuItem tc = new MenuItem("Third party software");
        tc.setOnAction((a) -> {
            DialogHelper.showText("Third party information", "A list of all software used to create the Pdx-Unlimiter", "third_party.txt");
        });
        about.getItems().add(wiki);
        about.getItems().add(src);
        about.getItems().add(is);
        about.getItems().add(lc);
        about.getItems().add(tc);


        Menu dev = new Menu("Developer");
        MenuItem ns = new MenuItem("Namespace creator");
        ns.setOnAction((a) -> {
            DialogHelper.createNamespaceDialog();
        });
        dev.getItems().add(ns);

        MenuItem log = new MenuItem("Show log");
        log.setOnAction((a) -> {
            DialogHelper.showLogDialog();
        });
        dev.getItems().add(log);


        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(menu);
        menuBar.getMenus().add(settings);
        menuBar.getMenus().add(savegames);
        menuBar.getMenus().add(about);
        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            menuBar.getMenus().add(dev);
        }
        return menuBar;
    }

    private static Node createStatusIndicator() {
        MenuBar rightBar = new MenuBar();
        Menu m = new Menu("Idle");
        rightBar.getMenus().addAll(m);
        SavegameCache.EU4_CACHE.statusProperty().addListener((ch, o, n) -> {
            Platform.runLater(() -> {
                if (!n.isPresent()) {
                    m.setText("Idle");
                    return;
                }

                SavegameCache.Status s = n.get();
                if (s.getType() == SavegameCache.Status.Type.UPDATING) {
                    m.setText("Updating campaign entry " + s.getPath());
                }
                if (s.getType() == SavegameCache.Status.Type.LOADING) {
                    m.setText("Loading campaign entry " + s.getPath());
                }
                if (s.getType() == SavegameCache.Status.Type.IMPORTING) {
                    m.setText("Importing savegame " + s.getPath());
                }
                if (s.getType() == SavegameCache.Status.Type.DELETING) {
                    m.setText("Deleting campaign entry " + s.getPath());
                }
            });
        });
        return rightBar;
    }

    public static Node createMenu() {
        MenuBar leftBar = createMenuBar();

        Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        HBox menubars = new HBox(leftBar, spacer, createStatusIndicator());
        return menubars;
    }
}
