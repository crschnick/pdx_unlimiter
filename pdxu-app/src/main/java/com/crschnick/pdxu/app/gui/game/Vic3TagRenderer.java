package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.model.CoatOfArms;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Vic3TagRenderer {


    public static BufferedImage renderImage(CoatOfArms coa, GameFileContext ctx, int width, int height) {
        if (coa == null) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
        g.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );

        for (var sub : coa.getSubs()) {
            var rawPatternImg = CoatOfArmsRenderer.VIC3.pattern(g, sub, ctx, width, height);
            for (var emblem : sub.getEmblems()) {
                CoatOfArmsRenderer.VIC3.emblem(i, rawPatternImg, sub, emblem, ctx, width, height);
            }
        }

        return i;
    }
}
