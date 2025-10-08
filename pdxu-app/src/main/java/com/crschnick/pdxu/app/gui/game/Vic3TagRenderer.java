package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.coa.CoatOfArms;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class Vic3TagRenderer {

    public static CoatOfArms getCoatOfArms(ArrayNode node, ArrayNode all) {
        var eval = node.copy().getArrayNode();
        NodeEvaluator.evaluateArrayNode(eval);

        var coa = CoatOfArms.fromNode(eval, parent -> {
            var found = all.getNodesForKey(parent);
            return found.size() > 0 ? found.getLast() : null;
        });
        return coa;
    }

    public static ArrayNode getCoatOfArmsNode(GameFileContext context, ArrayNode... otherNodes) {
        var dir = Path.of("common")
                .resolve("coat_of_arms")
                .resolve("coat_of_arms");
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(dir, context, files::add);

        var all = new LinkedArrayNode(Stream.concat(files.stream().map(path -> {
                    ArrayNode content = null;
                    try {
                        content = TextFormatParser.vic3().parse(path);
                    } catch (Exception e) {
                        return Optional.<ArrayNode>empty();
                    }

                    return Optional.of(content);
                }), Arrays.stream(otherNodes).map(Optional::of))
                                              .flatMap(Optional::stream)
                                              .peek(arrayNode -> NodeEvaluator.evaluateArrayNode(arrayNode.getArrayNode()))
                                              .toList());
        return all;
    }


    public static BufferedImage renderImage(CoatOfArms coa, GameFileContext ctx, int width, int height) {
        if (coa == null) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();

        for (var sub : coa.getSubs()) {
            var rawPatternImg = CoatOfArmsRenderer.VIC3.pattern(g, sub, ctx, width, height);

            for (var emblem : sub.getEmblems()) {
                CoatOfArmsRenderer.VIC3.emblem(i, rawPatternImg, sub, emblem, ctx, width, height);
            }
        }

        return i;
    }
}
