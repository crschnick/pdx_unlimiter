package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.model.CoatOfArms;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Vic3TagRenderer {


    public static BufferedImage renderImage(CoatOfArms coa, GameFileContext ctx, int size) {
        if (coa == null) {
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        for (var sub : coa.getSubs()) {
            var rawPatternImg = CoatOfArmsRenderer.VIC3.pattern(g, sub, ctx, size);
            for (var emblem : sub.getEmblems()) {
                CoatOfArmsRenderer.VIC3.emblem(i, rawPatternImg, sub, emblem, ctx, size);
            }
        }

        return i;
    }
}
