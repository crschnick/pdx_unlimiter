package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GameImage {

    private static final Map<Image, Rectangle2D> VIEWPORTS = new HashMap<>();
    public static Image CK3_ICON_IRONMAN;
    public static Image CK3_ICON_RULER;
    public static Image CK3_ICON_HEIR;
    public static Image CK3_ICON_TITLES;
    public static Image CK3_ICON_CLAIMS;
    public static Image CK3_ICON_WAR;
    public static Image CK3_ICON_ALLY;
    public static Image CK3_BACKGROUND;

    public static Image CK3_REALM_MASK;
    public static Image CK3_REALM_CLAN_MASK;
    public static Image CK3_REALM_REPUBLIC_MASK;
    public static Image CK3_REALM_THEOCRACY_MASK;
    public static Image CK3_REALM_TRIBAL_MASK;

    public static Image CK3_REALM_FRAME;
    public static Image CK3_REALM_CLAN_FRAME;
    public static Image CK3_REALM_REPUBLIC_FRAME;
    public static Image CK3_REALM_THEOCRACY_FRAME;
    public static Image CK3_REALM_TRIBAL_FRAME;

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

    public static Image CK3_ICON_GOLD;
    public static Image CK3_ICON_PRESTIGE;
    public static Image CK3_ICON_SOLDIERS;
    public static Image CK3_ICON_PIETY;
    public static Image CK3_ICON_RENOWN;
    public static Image CK3_ICON_HEALTH;

    public static Image STELLARIS_ICON_IRONMAN;
    public static Image STELLARIS_ICON_WAR;
    public static Image STELLARIS_ICON_ALLIANCE;
    public static Image STELLARIS_FLAG_MASK;
    public static Image STELLARIS_FLAG_FRAME;
    public static Image STELLARIS_BACKGROUND;
    public static Image STELLARIS_ICON_MINERALS;
    public static Image STELLARIS_ICON_ENERGY;
    public static Image STELLARIS_ICON_FOOD;
    public static Image STELLARIS_ICON_ALLOYS;
    public static Image STELLARIS_ICON_INFLUENCE;
    public static Image STELLARIS_ICON_UNITY;
    public static Image STELLARIS_ICON_RESEARCH;
    public static Image STELLARIS_ICON_CONSUMER_GOODS;
    public static Image STELLARIS_ICON_FLEETS;
    public static Image STELLARIS_ICON_EMPIRE_SIZE;
    public static Image STELLARIS_ICON_PLANETS;

    public static Image HOI4_ICON_IRONMAN;
    public static Image HOI4_ICON_VERSION_WARNING;
    public static Image HOI4_ICON_DIFF_RECRUIT;
    public static Image HOI4_ICON_DIFF_REGULAR;
    public static Image HOI4_ICON_DIFF_VETERAN;
    public static Image HOI4_ICON_DIFF_ELITE;
    public static Image HOI4_ICON_DIFF_CIVILIAN;
    public static Image HOI4_BACKGROUND;
    public static Image HOI4_FLAG_OVERLAY;

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
    public static Image EU4_ICON_UNION_SENIOR;
    public static Image EU4_ICON_UNION_JUNIOR;
    public static Image EU4_ICON_DUCATS;
    public static Image EU4_ICON_DEV;
    public static Image EU4_ICON_MANPOWER;
    public static Image EU4_ICON_PRESTIGE;
    public static Image EU4_ICON_STABILITY;
    public static Image EU4_ICON_ACHIEVEMENT;
    public static Image EU4_BACKGROUND;
    public static Image EU4_SHIELD_MASK;
    public static Image EU4_SHIELD_FRAME;
    public static Image EU4_SMALL_SHIELD_MASK;
    public static Image EU4_SMALL_SHIELD_FRAME;


    public static Image CK2_BACKGROUND;


    public static Image VIC2_BACKGROUND;

    public static Image VIC3_BACKGROUND;
    public static Image VIC3_ICON_IRONMAN;
    public static Image VIC3_ICON_ACHIEVEMENT_ELIGIBLE;
    public static Image VIC3_ICON_ACHIEVEMENT_INELIGIBLE;
    public static Image VIC3_ICON_GDP;
    public static Image VIC3_ICON_PRESTIGE;
    public static Image VIC3_ICON_SOL;
    public static Image VIC3_ICON_RADICALS;
    public static Image VIC3_ICON_LOYALISTS;

    public static void loadGameImages(Game g) {
        Map<Game, Runnable> loadFuncs = Map.of(
                Game.EU4, GameImage::loadEu4Images,
                Game.CK3, GameImage::loadCk3Images,
                Game.HOI4, GameImage::loadHoi4Images,
                Game.STELLARIS, GameImage::loadStellarisImages,
                Game.CK2, GameImage::loadCk2Images,
                Game.VIC2, GameImage::loadVic2Images,
                Game.VIC3, GameImage::loadVic3Images
        );

        if (g != null) {
            loadFuncs.get(g).run();
        }
    }

    public static Image getGameIcon(Game g) {
        var iconFile = GameInstallation.ALL.get(g).getDist().getIcon();
        var img = ImageHelper.loadImage(iconFile);
        if (g == Game.CK3) {
            img = ImageHelper.cut(img, new Rectangle2D(25, 25, 512 - 50, 512 - 50));
        }
        return img;
    }

    public static void loadVic3Images() {
        var installPath = GameInstallation.ALL.get(Game.VIC3).getInstallDir();
        Path p = installPath.resolve("game");
        Path fi = p.resolve("gfx").resolve("frontend").resolve("interface").resolve("icons");
        Path i = p.resolve("gfx").resolve("interface").resolve("icons");

        VIC3_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.VIC3)
                                                        .getType().chooseBackgroundImage(installPath));
        VIC3_ICON_IRONMAN = ImageHelper.loadImage(fi.resolve("meta").resolve("icon_ironman.dds"));
        VIC3_ICON_ACHIEVEMENT_ELIGIBLE = ImageHelper.loadImage(fi.resolve("meta").resolve("icon_achievements_enabled.dds"));
        VIC3_ICON_ACHIEVEMENT_INELIGIBLE = ImageHelper.loadImage(fi.resolve("meta").resolve("icon_achievements_disabled.dds"));

        VIC3_ICON_GDP = ImageHelper.loadImage(i.resolve("generic_icons").resolve("gdp.dds"));
        VIC3_ICON_PRESTIGE = ImageHelper.loadImage(p.resolve("gfx").resolve("interface").resolve("flag").resolve("power_rank_04.dds"));
        VIC3_ICON_SOL = ImageHelper.loadImage(i.resolve("generic_icons").resolve("sol_icon.dds"));
        VIC3_ICON_LOYALISTS = ImageHelper.loadImage(i.resolve("generic_icons").resolve("population_loyalist.dds"));
        VIC3_ICON_RADICALS = ImageHelper.loadImage(i.resolve("generic_icons").resolve("population_radical.dds"));
    }

    public static void loadCk3Images() {
        var installPath = GameInstallation.ALL.get(Game.CK3).getInstallDir();
        Path p = installPath.resolve("game");
        Path i = p.resolve("gfx").resolve("interface").resolve("icons");

        CK3_ICON_IRONMAN = ImageHelper.loadImage(i.resolve("meta").resolve("icon_ironman.dds"));

        CK3_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.CK3)
                                                       .getType().chooseBackgroundImage(installPath));
        CK3_TITLE_MASK = ImageHelper.loadImage(
                installPath.resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("title_mask.dds"));
        CK3_TITLE_FRAME = ImageHelper.loadImage(
                installPath.resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("title_86.dds"));

        CK3_HOUSE_MASK = ImageHelper.loadImage(
                installPath.resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("house_mask.dds"));
        CK3_HOUSE_FRAME = ImageHelper.cut(ImageHelper.loadImage(
                installPath.resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("house_115.dds")), new Rectangle2D(150, 0, 150, 150));
        VIEWPORTS.put(CK3_HOUSE_FRAME, new Rectangle2D(150, 0, 150, 150));


        CK3_SKILL_DIPLOMACY = ImageHelper.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_DIPLOMACY, new Rectangle2D(0, 0, 60, 60));

        CK3_SKILL_MARTIAL = ImageHelper.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_MARTIAL, new Rectangle2D(60, 0, 60, 60));

        CK3_SKILL_STEWARDSHIP = ImageHelper.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_STEWARDSHIP, new Rectangle2D(120, 0, 60, 60));

        CK3_SKILL_INTRIGUE = ImageHelper.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_INTRIGUE, new Rectangle2D(180, 0, 60, 60));

        CK3_SKILL_LEARNING = ImageHelper.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_LEARNING, new Rectangle2D(240, 0, 60, 60));

        CK3_SKILL_PROWESS = ImageHelper.loadImage(i.resolve("icon_skills.dds"));
        VIEWPORTS.put(CK3_SKILL_PROWESS, new Rectangle2D(300, 0, 60, 60));

        CK3_ICON_RULER = ImageHelper.loadImage(i.resolve("flat_icons").resolve("map_modes").resolve("dejure_kingdoms.dds"));
        CK3_ICON_HEIR = ImageHelper.loadImage(i.resolve("flat_icons").resolve("heir.dds"));
        CK3_ICON_TITLES = ImageHelper.loadImage(i.resolve("message_feed").resolve("titles.dds"));
        CK3_ICON_CLAIMS = ImageHelper.loadImage(i.resolve("casus_bellis").resolve("claim_cb.dds"));

        CK3_REALM_MASK = ImageHelper.loadImage(i.resolve("realm_masks").resolve("_default.dds"));
        CK3_REALM_CLAN_MASK = ImageHelper.loadImage(i.resolve("realm_masks").resolve("clan_government.dds"));
        CK3_REALM_REPUBLIC_MASK = ImageHelper.loadImage(i.resolve("realm_masks").resolve("republic_government.dds"));
        CK3_REALM_THEOCRACY_MASK = ImageHelper.loadImage(i.resolve("realm_masks").resolve("theocracy_government.dds"));
        CK3_REALM_TRIBAL_MASK = ImageHelper.loadImage(i.resolve("realm_masks").resolve("tribal_government.dds"));

        CK3_REALM_FRAME = ImageHelper.loadImage(i.resolve("realm_frames").resolve("_default_115.dds"));
        CK3_REALM_CLAN_FRAME = ImageHelper.loadImage(i.resolve("realm_frames").resolve("clan_government_115.dds"));
        CK3_REALM_REPUBLIC_FRAME = ImageHelper.loadImage(i.resolve("realm_frames").resolve("republic_government_115.dds"));
        CK3_REALM_THEOCRACY_FRAME = ImageHelper.loadImage(i.resolve("realm_frames").resolve("theocracy_government_115.dds"));
        CK3_REALM_TRIBAL_FRAME = ImageHelper.loadImage(i.resolve("realm_frames").resolve("tribal_government_115.dds"));

        CK3_COA_OVERLAY = ImageHelper.loadImage(
                installPath.resolve("game").resolve("gfx").resolve("interface")
                        .resolve("coat_of_arms").resolve("coa_overlay.dds"));

        CK3_ICON_WAR = ImageHelper.loadImage(i.resolve("map_coa").resolve("icon_at_war_big.dds"));
        CK3_ICON_ALLY = ImageHelper.loadImage(i.resolve("message_feed").resolve("alliance.dds"));
        CK3_ICON_GOLD = ImageHelper.loadImage(i.resolve("icon_gold.dds"));
        CK3_ICON_PRESTIGE = ImageHelper.loadImage(i.resolve("currencies").resolve("icon_prestige_01.dds"));
        CK3_ICON_PIETY = ImageHelper.loadImage(i.resolve("currencies").resolve("icon_piety_christian_01.dds"));
        CK3_ICON_SOLDIERS = ImageHelper.loadImage(i.resolve("icon_soldier.dds"));
        CK3_ICON_RENOWN = ImageHelper.loadImage(i.resolve("currencies").resolve("icon_dynasty_prestige_01.dds"));
        CK3_ICON_HEALTH = ImageHelper.loadImage(i.resolve("traits").resolve("_frame_health.dds"));
    }

    private static void loadStellarisImages() {
        var installPath = GameInstallation.ALL.get(Game.STELLARIS).getInstallDir();
        Path it = installPath.resolve("gfx").resolve("interface");
        Path i = installPath.resolve("gfx").resolve("interface").resolve("icons");
        Path r = installPath.resolve("gfx").resolve("interface").resolve("icons").resolve("resources");
        Path f = installPath.resolve("gfx").resolve("interface").resolve("flags");

        STELLARIS_ICON_IRONMAN = ImageHelper.loadImage(i.resolve("ironman_icon.dds"));
        STELLARIS_FLAG_MASK = ImageHelper.loadImage(f.resolve("empire_flag_200_mask.dds"));
        STELLARIS_FLAG_FRAME = ImageHelper.loadImage(f.resolve("empire_flag_200_frame.dds"));
        STELLARIS_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.STELLARIS)
                                                             .getType().chooseBackgroundImage(installPath));
        STELLARIS_ICON_WAR = ImageHelper.loadImage(it.resolve("waroverview").resolve("at_war_with_bg.dds"));
        STELLARIS_ICON_ALLIANCE = ImageHelper.loadImage(i.resolve("diplomacy").resolve("diplomacy_alliance.dds"));
        STELLARIS_ICON_ALLOYS = ImageHelper.loadImage(r.resolve("alloys.dds"));
        STELLARIS_ICON_ALLOYS = ImageHelper.loadImage(r.resolve("alloys.dds"));
        STELLARIS_ICON_ENERGY = ImageHelper.loadImage(r.resolve("energy.dds"));
        STELLARIS_ICON_FOOD = ImageHelper.loadImage(r.resolve("food.dds"));
        STELLARIS_ICON_UNITY = ImageHelper.loadImage(r.resolve("unity.dds"));
        STELLARIS_ICON_INFLUENCE = ImageHelper.loadImage(r.resolve("influence.dds"));
        STELLARIS_ICON_RESEARCH = ImageHelper.loadImage(i.resolve("research_icon.dds"));
        STELLARIS_ICON_MINERALS = ImageHelper.loadImage(r.resolve("minerals.dds"));
        STELLARIS_ICON_CONSUMER_GOODS = ImageHelper.loadImage(r.resolve("consumer_goods.dds"));
        STELLARIS_ICON_FLEETS = ImageHelper.loadImage(i.resolve("fleet_size_icon.dds"));
        STELLARIS_ICON_EMPIRE_SIZE = ImageHelper.loadImage(i.resolve("empire_sprawl_icon.dds"));
        STELLARIS_ICON_PLANETS = ImageHelper.loadImage(i.resolve("planet.dds"));

    }

    private static void loadCk2Images() {
        var installPath = GameInstallation.ALL.get(Game.CK2).getInstallDir();
        CK2_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.CK2)
                                                       .getType().chooseBackgroundImage(installPath));
    }

    private static void loadVic2Images() {
        var installPath = GameInstallation.ALL.get(Game.VIC2).getInstallDir();
        VIC2_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.VIC2)
                                                        .getType().chooseBackgroundImage(installPath));
        VIC2_BACKGROUND = ImageHelper.cut(VIC2_BACKGROUND, new Rectangle2D(100, 100,
                                                                           VIC2_BACKGROUND.getWidth() - 200, VIC2_BACKGROUND.getHeight() - 200
        ));
    }

    private static void loadHoi4Images() {
        var installPath = GameInstallation.ALL.get(Game.HOI4).getInstallDir();
        Path i = installPath.resolve("gfx").resolve("interface");

        HOI4_ICON_VERSION_WARNING = ImageHelper.loadImage(i.resolve("warning_icon.dds"));
        HOI4_ICON_IRONMAN = ImageHelper.loadImage(i.resolve("ironman_icon.dds"));
        HOI4_ICON_DIFF_RECRUIT = ImageHelper.loadImage(i.resolve("difficulty_button_recruit.dds"));
        HOI4_ICON_DIFF_REGULAR = ImageHelper.loadImage(i.resolve("difficulty_button_regular.dds"));
        HOI4_ICON_DIFF_VETERAN = ImageHelper.loadImage(i.resolve("difficulty_button_veteran.dds"));
        HOI4_ICON_DIFF_ELITE = ImageHelper.loadImage(i.resolve("difficulty_button_elite.dds"));
        HOI4_ICON_DIFF_CIVILIAN = ImageHelper.loadImage(i.resolve("difficulty_button_civilian.dds"));
        HOI4_FLAG_OVERLAY = ImageHelper.loadImage(i.resolve("flag_overlay.dds"));
        HOI4_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.HOI4)
                                                        .getType().chooseBackgroundImage(installPath));

    }

    private static void loadEu4Images() {
        var installPath = GameInstallation.ALL.get(Game.EU4).getInstallDir();
        Path i = installPath.resolve("gfx").resolve("interface");

        EU4_ICON_VASSAL = ImageHelper.loadImage(i.resolve("icon_vassal.dds"));
        EU4_ICON_ALLIANCE = ImageHelper.loadImage(i.resolve("icon_alliance.dds"));
        EU4_ICON_TRIBUTARY = ImageHelper.loadImage(i.resolve("subject_tributary_icon.dds"));
        EU4_ICON_MARCH = ImageHelper.loadImage(i.resolve("icon_march.dds"));
        EU4_ICON_TRUCE = ImageHelper.loadImage(i.resolve("icon_truce.dds"));
        EU4_ICON_ROYAL_MARRIAGE = ImageHelper.loadImage(i.resolve("icon_diplomacy_royalmarriage.dds"));
        EU4_ICON_GUARANTEE = ImageHelper.loadImage(i.resolve("icon_diplomacy_guaranting.dds"));
        EU4_ICON_WAR = ImageHelper.loadImage(i.resolve("icon_diplomacy_war.dds"));
        EU4_ICON_VERSION_WARNING = ImageHelper.loadImage(i.resolve("incompatible_warning_icon.dds"));
        EU4_ICON_IRONMAN = ImageHelper.loadImage(i.resolve("ironman_icon.dds"));
        EU4_ICON_RULER = ImageHelper.loadImage(
                i.resolve("tab_domestic_court.dds"));
        VIEWPORTS.put(EU4_ICON_RULER, new Rectangle2D(8, 10, 30, 30));
        EU4_ICON_HEIR = ImageHelper.loadImage(i.resolve("monarch_heir_crown_icon.dds"));
        EU4_ICON_ADM = ImageHelper.loadImage(i.resolve("icon_powers_administrative_in_text.dds"));
        EU4_ICON_DIP = ImageHelper.loadImage(i.resolve("icon_powers_diplomatic_in_text.dds"));
        EU4_ICON_MIL = ImageHelper.loadImage(i.resolve("icon_powers_military_in_text.dds"));


        EU4_ICON_UNION_SENIOR = ImageHelper.loadImage(i.resolve("icon_diplomacy_leadunions.dds"));
        EU4_ICON_UNION_JUNIOR = ImageHelper.loadImage(i.resolve("icon_diplomacy_inunion.dds"));
        EU4_ICON_MANPOWER = ImageHelper.loadImage(i.resolve("icon_manpower2.dds"));
        EU4_ICON_DEV = ImageHelper.loadImage(i.resolve("development_icon.dds"));
        EU4_ICON_DUCATS = ImageHelper.loadImage(i.resolve("icon_gold.dds"));
        EU4_ICON_PRESTIGE = ImageHelper.loadImage(i.resolve("icon_prestige.dds"));
        EU4_ICON_STABILITY = ImageHelper.loadImage(i.resolve("icon_stability.dds"));
        EU4_ICON_ACHIEVEMENT = ImageHelper.loadImage(i.resolve("button_achievements.dds"));

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
        EU4_ICON_RANDOM_NEW_WORLD = ImageHelper.loadImage(
                i.resolve("frontend_random_world.dds"),
                rnwFilter
        );
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
        EU4_ICON_CUSTOM_NATION = ImageHelper.loadImage(
                i.resolve("frontend_custom_nation.dds"),
                customFilter
        );
        VIEWPORTS.put(EU4_ICON_CUSTOM_NATION, new Rectangle2D(20, 5, 21, 21));

        EU4_ICON_RELEASED_VASSAL = ImageHelper.loadImage(
                i.resolve("release_nation_icon.dds"));
        VIEWPORTS.put(EU4_ICON_RELEASED_VASSAL, new Rectangle2D(37, 0, 36, 30));

        EU4_SHIELD_MASK = ImageHelper.loadImage(i.resolve("shield_mask.tga"));
        EU4_SHIELD_FRAME = ImageHelper.loadImage(i.resolve("shield_frame.dds"));
        EU4_SMALL_SHIELD_MASK = ImageHelper.loadImage(i.resolve("small_shield_mask.tga"));
        EU4_SMALL_SHIELD_FRAME = ImageHelper.loadImage(i.resolve("small_shield_overlay.dds"));

        EU4_BACKGROUND = ImageHelper.loadImage(GameInstallation.ALL.get(Game.EU4)
                                                       .getType().chooseBackgroundImage(installPath));
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
                    i.getHeight() * relViewportHeight
            ));
        };
        pane.widthProperty().addListener(cl);
        pane.heightProperty().addListener(cl);
        return pane;
    }
}
