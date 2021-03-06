package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4Tag;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GameImage {

    private static final Map<Image, Rectangle2D> VIEWPORTS = new HashMap<>();
    public static Image CK3_ICON;
    public static Image CK3_ICON_IRONMAN;
    public static Image CK3_ICON_RULER;
    public static Image CK3_ICON_HEIR;
    public static Image CK3_ICON_TITLES;
    public static Image CK3_ICON_CLAIMS;
    public static Image CK3_ICON_WAR;
    public static Image CK3_ICON_ALLY;
    public static Image CK3_BACKGROUND;
    public static Image CK3_REALM_MASK;
    public static Image CK3_REALM_FRAME;
    public static Image CK3_TITLE_MASK;
    public static Image CK3_TITLE_FRAME;
    public static Image CK3_HOUSE_MASK;
    public static Image CK3_HOUSE_FRAME;
    public static Image CK3_COA_OVERLAY;
    public static Image CK3_SKILL_DIPLOMACY;
    public static Image CK3_SKILL_MARTIAL;
    public static Image CK3_SKILL_STEWARDSHIP;
    public static Image CK3_SKILL_INTRIGUE;
    public static Image CK3_SKILL_LEARNING;
    public static Image CK3_SKILL_PROWESS;

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
    public static Image HOI4_FLAG_OVERLAY;

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

    public static void init() throws IOException {
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

        CK3_ICON_IRONMAN = ImageLoader.loadImage(i.resolve("meta").resolve("icon_ironman.dds"));

        CK3_BACKGROUND = ImageLoader.loadImage(GameInstallation.CK3.getPath()
                .resolve("launcher").resolve("assets").resolve("app-background.png"));
        CK3_TITLE_MASK = ImageLoader.loadImage(
                GameInstallation.CK3.getPath().resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("title_mask.dds"));
        CK3_TITLE_FRAME = ImageLoader.loadImage(
                GameInstallation.CK3.getPath().resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("title_86.dds"));

        CK3_HOUSE_MASK = ImageLoader.loadImage(
                GameInstallation.CK3.getPath().resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("house_mask.dds"));
        CK3_HOUSE_FRAME = ImageLoader.cut(ImageLoader.loadImage(
                GameInstallation.CK3.getPath().resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("house_115.dds")), new Rectangle2D(150, 0, 150, 150));
        VIEWPORTS.put(CK3_HOUSE_FRAME, new Rectangle2D(150, 0, 150, 150));


        CK3_SKILL_DIPLOMACY = ImageLoader.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_DIPLOMACY, new Rectangle2D(0, 0, 60, 60));

        CK3_SKILL_MARTIAL = ImageLoader.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_MARTIAL, new Rectangle2D(60, 0, 60, 60));

        CK3_SKILL_STEWARDSHIP = ImageLoader.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_STEWARDSHIP, new Rectangle2D(120, 0, 60, 60));

        CK3_SKILL_INTRIGUE = ImageLoader.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_INTRIGUE, new Rectangle2D(180, 0, 60, 60));

        CK3_SKILL_LEARNING = ImageLoader.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_LEARNING, new Rectangle2D(240, 0, 60, 60));

        CK3_SKILL_PROWESS = ImageLoader.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_PROWESS, new Rectangle2D(300, 0, 60, 60));

        CK3_ICON_RULER = ImageLoader.loadImage(i.resolve("flat_icons").resolve("mapmode_kingdom.dds"));
        CK3_ICON_HEIR = ImageLoader.loadImage(i.resolve("flat_icons").resolve("heir.dds"));
        CK3_ICON_TITLES = ImageLoader.loadImage(i.resolve("message_feed").resolve("titles.dds"));
        CK3_ICON_CLAIMS = ImageLoader.loadImage(i.resolve("casus_bellis").resolve("claim_cb.dds"));

        CK3_REALM_MASK = ImageLoader.loadImage(i.resolve("realm_masks").resolve("_default.dds"));
        CK3_REALM_FRAME = ImageLoader.loadImage(i.resolve("realm_frames").resolve("_default_115.dds"));

        CK3_COA_OVERLAY = ImageLoader.loadImage(
                GameInstallation.CK3.getPath().resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("coa_overlay.dds"));

        CK3_ICON_WAR = ImageLoader.loadImage(i.resolve("map_coa").resolve("icon_at_war_big.dds"));
        CK3_ICON_ALLY = ImageLoader.loadImage(i.resolve("message_feed").resolve("alliance.dds"));
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
        HOI4_FLAG_OVERLAY = ImageLoader.loadImage(i.resolve("flag_overlay.dds"));
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

        Function<Integer, Integer> rnwFilter = (Integer rgb) -> {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            boolean gold = (r > 100 && g > 87 && b < 100 && Math.max(r, Math.max(g, b) - 2) == r);
            boolean blue = Math.max(r, Math.max(g, b)) < 135;
            if (blue || gold) {
                return 0x00000001;
            }
            return rgb;
        };
        EU4_ICON_RANDOM_NEW_WORLD = ImageLoader.loadImage(
                i.resolve("frontend_random_world.dds"),
                rnwFilter);
        VIEWPORTS.put(EU4_ICON_RANDOM_NEW_WORLD, new Rectangle2D(14, 0, 33, 30));

        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            boolean blue = Math.max(r, Math.max(g, b)) < 142;
            if (blue) {
                return 0x00000001;
            }
            return rgb;
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

    public static Path getEu4TagPath(String tag) {
        return Path.of("gfx/flags/" + tag + ".tga");
    }

    public static Path getHoi4TagPath(Hoi4Tag tag) {
        return Path.of("gfx/flags/" + tag.getTag() + "_" + tag.getIdeology() + ".tga");
    }

    public static Pane imageNode(Image i, String styleClass) {
        return imageNode(i, styleClass, null);
    }

    public static Pane imageNode(Image i, String styleClass, String tt) {
        if (i == null) {
            throw new NullPointerException();
        }

        ImageView v = new ImageView(i);
        StackPane pane = new StackPane(v);
        pane.setAlignment(Pos.CENTER);

        Rectangle2D viewport = VIEWPORTS.get(i);
        if (viewport != null) {
            v.setViewport(viewport);
        }

        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        v.preserveRatioProperty().setValue(true);

        if (styleClass != null) {
            pane.getStyleClass().add(styleClass);
        }

        if (tt != null) {
            GuiTooltips.install(pane, tt);
        }
        return pane;
    }

    public static Pane backgroundNode(Image i) {
        ImageView v = new ImageView(i);
        Pane pane = new Pane(v);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        if (i == null) {
            return pane;
        }

        double imageAspect = i.getWidth() / i.getHeight();
        ChangeListener<? super Number> cl = (c, o, n) -> {
            double paneAspect = pane.getWidth() / pane.getHeight();

            double relViewportWidth;
            double relViewportHeight;

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
        pane.widthProperty().addListener(cl);
        pane.heightProperty().addListener(cl);
        return pane;
    }
}
