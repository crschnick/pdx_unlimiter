package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.crschnick.pdx_unlimiter.app.util.ColorHelper.*;
import static com.crschnick.pdx_unlimiter.app.util.ColorHelper.getBlue;

public class Eu4CustomFlagCache extends CacheManager.Cache {

    private static final int PATTERN_SIZE = 128;

    private static final int PATTERN_COLOR_1 = 0xFFFF0000;
    private static final int PATTERN_COLOR_2 = 0xFF00FF00;
    private static final int PATTERN_COLOR_3 = 0xFF0000FF;

    record Texture(BufferedImage img, int x, int y, int frames, int colors) {

    }

    private List<Integer> colors = new ArrayList<>();
    private List<Integer> flagColors = new ArrayList<>();
    private List<Texture> textures = new ArrayList<>();

    public Eu4CustomFlagCache() {
        super(CacheManager.Scope.GAME_SPECIFIC);

        try {
            var colorsFile = GameInstallation.ALL.get(Game.EU4).getPath().resolve("common")
                    .resolve("custom_country_colors").resolve("00_custom_country_colors.txt");
            var content = TextFormatParser.textFileParser().parse(colorsFile);
            content.forEach((k, v) -> {
                if (k.equals("color")) {
                    var mc = v.getNodeArray();
                    int color =
                            (mc.get(0).getInteger() << 24) +
                                    (mc.get(1).getInteger() << 16) +
                                    (mc.get(2).getInteger() << 8) + 0xFF;
                    colors.add(color);
                }

                if (k.equals("flag_color")) {
                    var mc = v.getNodeArray();
                    int color =
                            (mc.get(0).getInteger() << 24) +
                                    (mc.get(1).getInteger() << 16) +
                                    (mc.get(2).getInteger() << 8) + 0xFF;
                    flagColors.add(color);
                }
            });
            content.getNodeForKey("textures").forEach((k, v) -> {
                var tex = new Texture(
                        ImageLoader.loadAwtImage(GameInstallation.ALL.get(Game.EU4).getPath().resolve(
                                Path.of(v.getNodeForKey("file").getString())), null),
                        v.getNodeForKey("size").getNodeForKey("x").getInteger(),
                        v.getNodeForKey("size").getNodeForKey("y").getInteger(),
                        v.getNodeForKey("noOfFrames").getInteger(),
                        v.getNodeForKey("color").getInteger());
                textures.add(tex);
            });
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    public Color getColor(int index) {
        if (index >= colors.size()) {
            return new Color(0, 0, 0, 0);
        }

        return ColorHelper.awtColorFromInt(colors.get(index), 0xFF);
    }

    public Color getFlagColor(int index) {
        if (index >= flagColors.size()) {
            return new Color(0, 0, 0, 0);
        }

        return ColorHelper.awtColorFromInt(flagColors.get(index), 0xFF);
    }

    public void renderTexture(BufferedImage img, int flagIndex, List<Integer> flagColors) {
        Function<Integer, Color> patternFunction = (Integer rgb) -> {
            if (rgb == PATTERN_COLOR_1) {
                return getColor(flagColors.get(0));
            }

            if (rgb == PATTERN_COLOR_2) {
                return getColor(flagColors.get(1));
            }

            if (rgb == PATTERN_COLOR_3) {
                return getColor(flagColors.get(2));
            }

            return new Color(0, 0, 0, 0);
        };

        int current = 0;
        for (var tex : textures) {
            if (flagIndex < current + tex.frames) {
                int rel = flagIndex - current;
                int xOff = rel % tex.x;
                int yOff = rel / tex.x;

                img.getGraphics().drawImage(
                        tex.img,
                        -xOff * img.getWidth(),
                        -yOff * img.getWidth(),
                        (int) (((double) img.getWidth() / PATTERN_SIZE) * tex.img.getWidth()),
                        (int) (((double) img.getHeight() / PATTERN_SIZE) * tex.img.getHeight()),
                        new Color(0, 0, 0, 0),
                        null);

                for (int x = 0; x < img.getWidth(); x++) {
                    for (int y = 0; y < img.getHeight(); y++) {
                        int argb = img.getRGB(x, y);
                        var replace = patternFunction.apply(argb);
                        img.setRGB(x, y, replace.getRGB());
                    }
                }

                return;
            }
            current += tex.frames;
        }
    }
}
