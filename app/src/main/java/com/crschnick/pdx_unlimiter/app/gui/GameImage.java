package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.eu4.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.eu4.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.eu4.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.data.StellarisTag;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.savegame.StellarisSavegameInfo;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class GameImage {

    public static Image CK3_ICON;
    public static Image CK3_ICON_IRONMAN;
    public static Image CK3_BACKGROUND;

    public static Image STELLARIS_ICON;
    public static Image STELLARIS_ICON_IRONMAN;
    public static Image STELLARIS_BACKGROUND;

    public static Image HOI4_ICON;
    public static Image HOI4_ICON_IRONMAN;
    public static Image HOI4_ICON_VERSION_WARNING;
    public static Image HOI4_ICON_DIFF_RECRUIT;
    public static Image HOI4_ICON_DIFF_REGULAR;
    public static Image HOI4_ICON_DIFF_VETERAN;
    public static Image HOI4_ICON_DIFF_ELITE;
    public static Image HOI4_ICON_DIFF_CIVILIAN;
    public static Image HOI4_BACKGROUND;

    public static Image EU4_ICON;
    public static Image EU4_ICON_VASSAL;
    public static Image EU4_ICON_ALLIANCE;
    public static Image EU4_ICON_TRIBUTARY;
    public static Image EU4_ICON_MARCH;
    public static Image EU4_ICON_TRUCE;
    public static Image EU4_ICON_ROYAL_MARRIAGE;
    public static Image EU4_ICON_GUARANTEE;
    public static Image EU4_ICON_WAR;
    public static Image EU4_ICON_VERSION_WARNING;
    public static Image EU4_ICON_IRONMAN;
    public static Image EU4_ICON_RANDOM_NEW_WORLD;
    public static Image EU4_ICON_CUSTOM_NATION;
    public static Image EU4_ICON_RELEASED_VASSAL;
    public static Image EU4_ICON_RULER;
    public static Image EU4_ICON_HEIR;
    public static Image EU4_ICON_ADM;
    public static Image EU4_ICON_DIP;
    public static Image EU4_ICON_MIL;
    public static Image EU4_BACKGROUND;

    private static Map<Image, Rectangle2D> VIEWPORTS = new HashMap<>();

    private static Pane unknownTag(String styleClass) {
        Label l = new Label("?");
        l.getStyleClass().add(styleClass);
        l.getStyleClass().add(GuiStyle.CLASS_UNKNOWN_TAG);
        l.alignmentProperty().set(Pos.CENTER);
        var sp = new StackPane(l);
        StackPane.setAlignment(l, Pos.CENTER);
        return sp;
    }

    private static Path getEu4TagPath(String tag) {
        return Path.of("gfx/flags/" + tag + ".tga");
    }

    private static Path getHoi4TagPath(Hoi4Tag tag) {
        return Path.of("gfx/flags/" + tag.getTag() + "_" + tag.getIdeology() + ".tga");
    }

    public static Pane ck3TagNode(Ck3Tag tag, String styleClass) {
        return unknownTag(styleClass);
    }

    public static Pane hoi4TagNode(Hoi4Tag tag, String styleClass) {
        return tagNode(getHoi4TagPath(tag), null, styleClass);
    }

    public static Pane eu4TagNode(String tag, String styleClass) {
        return tagNode(getEu4TagPath(tag), null, styleClass);
    }

    public static Pane eu4TagNode(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, String styleClass) {
        return tagNode(getEu4TagPath(entry.getTag().getTag()), entry, styleClass);
    }

    private static Pane tagNode(Path path, GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry, String styleClass) {
        Image img = null;
        try {
            var in = CascadeDirectoryHelper.openFile(
                    path, entry, GameInstallation.EU4);
            img = in.map(inputStream -> ImageLoader.loadImage(inputStream, null)).orElse(null);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

        if (img == null) {
            return unknownTag(styleClass);
        }

        ImageView v = new ImageView(img);
        Pane pane = new Pane(v);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        v.preserveRatioProperty().setValue(true);
        pane.getStyleClass().add(styleClass);
        return pane;
    }

    public static Pane stellarisTagNode(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry, String styleClass) {
        return stellarisTagNode(Path.of(entry.getTag().getBackgroundFile()), entry, styleClass);
        //var bg = stellarisTagNode(GameInstallation.STELLARIS.getPath().resolve("flags"))
    }

    public static Pane stellarisTagNode(StellarisTag tag, String styleClass) {
        return stellarisTagNode(Path.of(tag.getBackgroundFile()), null, styleClass);
        //var bg = stellarisTagNode(GameInstallation.STELLARIS.getPath().resolve("flags"))
    }


    private static Pane stellarisTagNode(
            Path path, GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry, String styleClass) {
        Image img = null;
        try {
            var in = CascadeDirectoryHelper.openFile(
                    path, entry, GameInstallation.STELLARIS);
            img = in.map(inputStream -> ImageLoader.loadImage(inputStream, null)).orElse(null);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

        if (img == null) {
            return unknownTag(styleClass);
        }

        ImageView v = new ImageView(img);
        Pane pane = new Pane(v);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        v.preserveRatioProperty().setValue(true);
        pane.getStyleClass().add(styleClass);
        return pane;
    }

    public static Pane imageNode(Image i) {
        return imageNode(i, null, null);
    }

    public static Pane imageNode(Image i, String styleClass) {
        return imageNode(i, styleClass, null);
    }

    public static Pane imageNode(Image i, String styleClass, String tt) {
        if (i == null) {
            throw new NullPointerException();
        }
        ImageView v = new ImageView(i);
        Pane pane = new Pane(v);
        Rectangle2D viewport = VIEWPORTS.get(i);
        if (viewport != null) v.setViewport(viewport);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        v.preserveRatioProperty().setValue(true);
        if (styleClass != null) pane.getStyleClass().add(styleClass);

        var t = new Tooltip(tt);
        t.getStyleClass().add("tooltip");
        Tooltip.install(pane, t);

        return pane;
    }

    public static Pane backgroundNode(Image i) {
        ImageView v = new ImageView(i);
        Pane pane = new Pane(v);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        double imageAspect = i.getWidth() / i.getHeight();
        ChangeListener<? extends Number> cl = (c, o, n) -> {
            double w = (double) n;
            double h = w / imageAspect;
            double paneAspect = pane.getWidth() / pane.getHeight();

            double relViewportWidth = 0;
            double relViewportHeight = 0;

            // Pane width too big for image
            if (paneAspect > imageAspect) {
                relViewportWidth = 1;
                double newImageHeight = pane.getWidth() / imageAspect;
                relViewportHeight = Math.min(1, pane.getHeight() / newImageHeight);
            }

            // Height too big
            else {
                relViewportHeight = 1;
                double newImageWidth = pane.getHeight() * imageAspect;
                relViewportWidth = Math.min(1, pane.getWidth() / newImageWidth);
            }

            v.setViewport(new Rectangle2D(
                    ((1 - relViewportWidth) / 2.0) * i.getWidth(),
                    ((1 - relViewportHeight) / 2.0) * i.getHeight(),
                    i.getWidth() * relViewportWidth,
                    i.getHeight() * relViewportHeight));
        };
        pane.widthProperty().addListener((ChangeListener<? super Number>) cl);
        pane.heightProperty().addListener((ChangeListener<? super Number>) cl);
        return pane;
    }

    public static void loadImages() {
        loadEu4Images();
        loadHoi4Images();
        loadStellarisImages();
        loadCk3Images();
    }

    public static void loadCk3Images() {
        if (GameInstallation.CK3 == null) {
            return;
        }

        Path p = GameInstallation.CK3.getPath().resolve("game");
        Path i = p.resolve("gfx").resolve("interface").resolve("icons");

        CK3_ICON = ImageLoader.loadImage(
                GameInstallation.CK3.getPath().resolve("game").resolve("gfx").resolve("exe_icon.bmp"));

        CK3_ICON_IRONMAN = ImageLoader.loadImage(i.resolve("ironman_icon.dds"));

        CK3_BACKGROUND = ImageLoader.loadImage(GameInstallation.CK3.getPath()
                .resolve("launcher").resolve("launcher-assets").resolve("app-background.png"));

    }

    public static void loadStellarisImages() {
        if (GameInstallation.STELLARIS == null) {
            return;
        }

        Path p = GameInstallation.STELLARIS.getPath();
        Path i = p.resolve("gfx").resolve("interface").resolve("icons");

        STELLARIS_ICON = ImageLoader.loadImage(
                GameInstallation.STELLARIS.getPath().resolve("gfx").resolve("exe_icon.bmp"));

        STELLARIS_ICON_IRONMAN = ImageLoader.loadImage(i.resolve("ironman_icon.dds"));
        STELLARIS_BACKGROUND = ImageLoader.loadImage(
                GameInstallation.STELLARIS.getPath().resolve("launcher-assets").resolve("app-background.png"));

    }

    public static void loadHoi4Images() {
        if (GameInstallation.HOI4 == null) {
            return;
        }

        Path p = GameInstallation.HOI4.getPath();
        Path i = p.resolve("gfx").resolve("interface");

        HOI4_ICON = ImageLoader.loadImage(
                GameInstallation.HOI4.getPath().resolve("launcher-assets").resolve("game-icon.png"));

        HOI4_ICON_VERSION_WARNING = ImageLoader.loadImage(i.resolve("warning_icon.dds"));
        HOI4_ICON_IRONMAN = ImageLoader.loadImage(i.resolve("ironman_icon.dds"));
        HOI4_ICON_DIFF_RECRUIT = ImageLoader.loadImage(i.resolve("difficulty_button_recruit.dds"));
        HOI4_ICON_DIFF_REGULAR = ImageLoader.loadImage(i.resolve("difficulty_button_regular.dds"));
        HOI4_ICON_DIFF_VETERAN = ImageLoader.loadImage(i.resolve("difficulty_button_veteran.dds"));
        HOI4_ICON_DIFF_ELITE = ImageLoader.loadImage(i.resolve("difficulty_button_elite.dds"));
        HOI4_ICON_DIFF_CIVILIAN = ImageLoader.loadImage(i.resolve("difficulty_button_civilian.dds"));
        HOI4_BACKGROUND = ImageLoader.loadImage(
                GameInstallation.HOI4.getPath().resolve("launcher-assets").resolve("app-background.png"));

    }

    public static void loadEu4Images() {
        if (GameInstallation.EU4 == null) {
            return;
        }

        Path p = GameInstallation.EU4.getPath();
        Path i = p.resolve("gfx").resolve("interface");

        EU4_ICON = ImageLoader.loadImage(
                GameInstallation.EU4.getPath().resolve("launcher-assets").resolve("icon.png"));

        EU4_ICON_VASSAL = ImageLoader.loadImage(i.resolve("icon_vassal.dds"));
        EU4_ICON_ALLIANCE = ImageLoader.loadImage(i.resolve("icon_alliance.dds"));
        EU4_ICON_TRIBUTARY = ImageLoader.loadImage(i.resolve("subject_tributary_icon.dds"));
        EU4_ICON_MARCH = ImageLoader.loadImage(i.resolve("icon_march.dds"));
        EU4_ICON_TRUCE = ImageLoader.loadImage(i.resolve("icon_truce.dds"));
        EU4_ICON_ROYAL_MARRIAGE = ImageLoader.loadImage(i.resolve("icon_diplomacy_royalmarriage.dds"));
        EU4_ICON_GUARANTEE = ImageLoader.loadImage(i.resolve("icon_diplomacy_guaranting.dds"));
        EU4_ICON_WAR = ImageLoader.loadImage(i.resolve("icon_diplomacy_war.dds"));
        EU4_ICON_VERSION_WARNING = ImageLoader.loadImage(i.resolve("incompatible_warning_icon.dds"));
        EU4_ICON_IRONMAN = ImageLoader.loadImage(i.resolve("ironman_icon.dds"));
        EU4_ICON_RULER = ImageLoader.loadImage(
                i.resolve("tab_domestic_court.dds"));
        VIEWPORTS.put(EU4_ICON_RULER, new Rectangle2D(8, 10, 30, 30));
        EU4_ICON_HEIR = ImageLoader.loadImage(i.resolve("monarch_heir_crown_icon.dds"));
        EU4_ICON_ADM = ImageLoader.loadImage(i.resolve("icon_powers_administrative_in_text.dds"));
        EU4_ICON_DIP = ImageLoader.loadImage(i.resolve("icon_powers_diplomatic_in_text.dds"));
        EU4_ICON_MIL = ImageLoader.loadImage(i.resolve("icon_powers_military_in_text.dds"));

        Predicate<Integer> rnwFilter = (Integer rgb) -> {
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
        EU4_ICON_RANDOM_NEW_WORLD = ImageLoader.loadImage(
                i.resolve("frontend_random_world.dds"),
                rnwFilter);
        VIEWPORTS.put(EU4_ICON_RANDOM_NEW_WORLD, new Rectangle2D(14, 0, 33, 30));

        Predicate<Integer> customFilter = (Integer rgb) -> {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            boolean blue = Math.max(r, Math.max(g, b)) < 142;
            if (blue) {
                return false;
            }
            return true;
        };
        EU4_ICON_CUSTOM_NATION = ImageLoader.loadImage(
                i.resolve("frontend_custom_nation.dds"),
                customFilter);
        VIEWPORTS.put(EU4_ICON_CUSTOM_NATION, new Rectangle2D(20, 5, 21, 21));

        EU4_ICON_RELEASED_VASSAL = ImageLoader.loadImage(
                i.resolve("release_nation_icon.dds"));
        VIEWPORTS.put(EU4_ICON_RELEASED_VASSAL, new Rectangle2D(37, 0, 36, 30));

        EU4_BACKGROUND = ImageLoader.loadImage(
                GameInstallation.EU4.getPath().resolve("launcher-assets").resolve("app-background.png"));
    }
}
