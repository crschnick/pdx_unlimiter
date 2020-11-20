package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class GameImage {

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

    private static Map<Image, Rectangle2D> VIEWPORTS = new HashMap<>();
    private static Map<String, Image> COUNTRY_IMAGES = new HashMap<>();

    public static Pane tagNode(String tag, String styleClass) {
        if (!COUNTRY_IMAGES.containsKey(tag)) {
            COUNTRY_IMAGES.put(tag, ImageLoader.loadImage(
                    GameInstallation.EU4.getPath().resolve("gfx/flags/" + tag + ".tga")));
        }

        ImageView v = new ImageView(COUNTRY_IMAGES.get(tag));
        Pane pane = new Pane(v);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
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

    public static void loadImages() {
        Path p = GameInstallation.EU4.getPath();
        Path i = p.resolve("gfx").resolve("interface");
        String s = "image-icon";

        EU4_ICON = ImageLoader.loadImage(
                GameInstallation.EU4.getPath().resolve("launcher-assets").resolve("icon.png"));

        EU4_ICON_VASSAL = ImageLoader.loadImage(i.resolve("icon_vassal.dds"));
        EU4_ICON_ALLIANCE= ImageLoader.loadImage(i.resolve("icon_alliance.dds"));
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
    }
}
