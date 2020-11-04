package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
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
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static com.crschnick.pdx_unlimiter.app.gui.GameImage.*;
import static com.crschnick.pdx_unlimiter.app.gui.GuiEu4CampaignEntry.createCampaignEntryNode;

public class Eu4SavegameManagerStyle {

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
    private static String CLASS_CAMPAIGN = "campaign";
    private static String CLASS_IMAGE_ICON = "image-icon";
    private static String CLASS_POWER_ICON = "power-icon";
    private static String CLASS_RULER_ICON = "ruler-icon";
    private static String CLASS_TAG_ICON = "tag-icon";
    private static String CLASS_TEXT = "text";;
    private static String CLASS_ENTRY_BAR = "entry-bar";
    private static String CLASS_ENTRY_LIST = "entry-list";
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

    private static void updateSavegames(JFXListView<Node> list, Eu4Campaign c,
                                        ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry) {
        List<Node> newOrder = c.getSavegames().stream()
                .sorted(Comparator.comparingLong(e -> GameDate.toLong(e.getDate().or(() -> Optional.of(GameDate.fromLong(0))).get())))
                .map(e -> createCampaignEntryNode(e, selectedEntry))
                .collect(Collectors.toList());
        Collections.reverse(newOrder);
        list.getItems().setAll(newOrder);
    }

    public static Node createSavegameList(ObjectProperty<Optional<Eu4Campaign>> selectedCampaign,
                                           ObjectProperty<Optional<Eu4Campaign.Entry>> selectedEntry) {
        JFXListView<Node> grid = new JFXListView<>();
        grid.getStyleClass().add(CLASS_ENTRY_LIST);

        selectedCampaign.addListener((c, o, n) -> {
            if (n.isPresent()) {
                Platform.runLater(() -> {
                    updateSavegames(grid, n.get(), selectedEntry);
                });
            } else {
                Platform.runLater(() -> {
                    grid.setItems(FXCollections.observableArrayList());
                });
            }
        });

        return grid;
    }

    private static Node createCampaignButton(Eu4Campaign c, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        Button del = new JFXButton();
        del.setGraphic(new FontIcon());
        del.getStyleClass().add("delete-button");
        del.setOnMouseClicked((m) -> {
            if (DialogHelper.showCampaignDeleteDialog()) {
                onDelete.accept(c);
            }
        });
        del.setAlignment(Pos.CENTER);
        Tooltip.install(del, tooltip("Delete campaign"));


        TextField name = new TextField(c.getName());
        name.setMinWidth(0);
        name.textProperty().bindBidirectional(c.nameProperty());

        Label date = new Label(c.getDate().toString());
        c.dateProperty().addListener((change, o, n) -> {
            Platform.runLater(() -> {
                date.setText(n.toString());
            });
        });
        date.getStyleClass().add(CLASS_DATE);

        HBox top = new HBox();
        top.setSpacing(3);
        top.getChildren().add(name);
        top.getChildren().add(del);

        VBox b = new VBox();
        b.setSpacing(3);
        b.getChildren().add(top);
        b.getChildren().add(date);

        Node w = getImageForTagName(c.getTag(), CLASS_TAG_ICON);
        HBox btn = new HBox();
        btn.getChildren().add(w);
        btn.getChildren().add(b);
        c.tagProperty().addListener((change, o, n) -> {
            Platform.runLater(() -> {
                btn.getChildren().set(0, getImageForTagName(n, CLASS_TAG_ICON));
            });
        });

        btn.setOnMouseClicked((m) -> selectedCampaign.setValue(Optional.of(c)));
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add(CLASS_CAMPAIGN);

        HBox.setHgrow(name, Priority.NEVER);

        return btn;
    }

    private static void sortCampaignList(JFXListView<Node> list, ObservableSet<Eu4Campaign> campaigns, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        List<Node> newOrder = campaigns.stream()
                .sorted(Comparator.comparing(Eu4Campaign::getLastPlayed))
                .map(c -> createCampaignButton(c, selectedCampaign, onDelete))
                .collect(Collectors.toList());
        Collections.reverse(newOrder);
        list.getItems().setAll(newOrder);
        list.prefWidthProperty().setValue(400);
    }

    public static Node createCampaignList(ObservableSet<Eu4Campaign> campaigns, ObjectProperty<Optional<Eu4Campaign>> selectedCampaign, Consumer<Eu4Campaign> onDelete) {
        JFXListView<Node> grid = new JFXListView<Node>();
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
        return grid;
    }

    public static Node createNoCampaignNode() {
        Label text = new Label("Welcome to the Pdx-Unlimiter!" +
                " To get started, import your latest EU4 savegame.");
        StackPane textPane = new StackPane(text);
        StackPane.setAlignment(textPane, Pos.CENTER);

        Button b = new Button("Import latest EU4 savegame");
        b.setOnMouseClicked(e -> {
            Eu4SavegameImporter.importLatestSavegame();
        });
        StackPane p = new StackPane();
        p.getChildren().add(b);
        StackPane.setAlignment(b, Pos.CENTER);
        VBox v = new VBox(textPane, new Label(), p);
        v.setAlignment(Pos.CENTER);
        return v;
    }

    public static Node createActiveStatusBar(PdxApp app) {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-border-width: 0; -fx-background-color: #337733;");

        Node icon = imageNode(EU4_ICON, CLASS_IMAGE_ICON);
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

    public static Node createInactiveStatusBar(
            ObjectProperty<Optional<Eu4Campaign>> selectedCampaign,
            ObjectProperty<Optional<Eu4Campaign.Entry>> save,
            Consumer<Eu4Campaign.Entry> onExport,
            Consumer<Eu4Campaign.Entry> onLaunch) {

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-border-width: 0; -fx-background-color: #555555;");

        Label text = new Label("Europa Universalis 4", imageNode(EU4_ICON, CLASS_IMAGE_ICON));
        text.setAlignment(Pos.BOTTOM_CENTER);
        text.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setLeft(text);

        Label status = new Label("Status: Stopped");
        status.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        pane.setCenter(status);

        Button e = new Button("Export");
        e.setOnMouseClicked((m) -> {
            onExport.accept(save.get().get());
        });

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
        HBox buttons = new HBox(e, b);
        buttons.setSpacing(10);
        pane.setRight(buttons);

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
