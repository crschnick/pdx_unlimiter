package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Ck3TagRenderer {

    public static CoatOfArms getCoatOfArms(ArrayNode node, ArrayNode all) {
        var eval = node.copy();
        NodeEvaluator.evaluateArrayNode(node);

        var coa = CoatOfArms.fromNode(eval, parent -> {
            var found = all.getNodesForKey(parent);
            return found.size() > 0 ? found.get(found.size() - 1) : null;
        });
        return coa;
    }

    public static ArrayNode getCoatOfArmsNode(GameFileContext context, ArrayNode... otherNodes) {
        var directory = Path.of("common")
                .resolve("coat_of_arms")
                .resolve("coat_of_arms");
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(directory, context, files::add);

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

    public static BufferedImage renderImage(CoatOfArms coa, GameFileContext ctx, int size, boolean cloth) {
        if (coa == null) {
            return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        for (var sub : coa.getSubs()) {
            var rawPatternImg = CoatOfArmsRenderer.CK3.pattern(g, sub, ctx, size, size);

            for (var emblem : sub.getEmblems()) {
                CoatOfArmsRenderer.CK3.emblem(i, rawPatternImg, sub, emblem, ctx, size, size);
            }
        }
        if (cloth) {
            CoatOfArmsRenderer.CK3.applyMask(i, GameImage.CK3_COA_OVERLAY);
            CoatOfArmsRenderer.CK3.brighten(i);
        }

        ThreadHelper.sleep(30);

        return i;
    }

    public static Image renderRealmImage(
            CoatOfArms coa, String governmentShape, GameFileContext ctx, int size, boolean cloth) {
        var realmImg = renderImage(coa, ctx, size, false);

        var masks = Map.of(
                "clan_government", GameImage.CK3_REALM_CLAN_MASK,
                "republic_government", GameImage.CK3_REALM_REPUBLIC_MASK,
                "theocracy_government", GameImage.CK3_REALM_THEOCRACY_MASK,
                "tribal_government", GameImage.CK3_REALM_TRIBAL_MASK);
        var useMask = masks.getOrDefault(governmentShape, GameImage.CK3_REALM_MASK);
        CoatOfArmsRenderer.CK3.applyMask(realmImg, useMask);
        CoatOfArmsRenderer.CK3.brighten(realmImg);

        double scaleFactor = (double) size / CoatOfArmsRenderer.CK3.REF_IMG_SIZE;
        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        CoatOfArmsRenderer.CK3.renderImage(
                g,
                realmImg,
                scaleFactor,
                4 * scaleFactor,
                realmImg.getWidth() - scaleFactor,
                realmImg.getHeight() - (4 * scaleFactor));

        var frames = Map.of(
                "clan_government", GameImage.CK3_REALM_CLAN_FRAME,
                "republic_government", GameImage.CK3_REALM_REPUBLIC_FRAME,
                "theocracy_government", GameImage.CK3_REALM_THEOCRACY_FRAME,
                "tribal_government", GameImage.CK3_REALM_TRIBAL_FRAME);
        var useFrame = frames.getOrDefault(governmentShape, GameImage.CK3_REALM_FRAME);
        CoatOfArmsRenderer.CK3.renderImage(
                g,
                ImageHelper.fromFXImage(useFrame),
                3 * scaleFactor,
                -8 * scaleFactor,
                realmImg.getWidth() - (6 * scaleFactor),
                realmImg.getHeight() + (20 * scaleFactor));

        return ImageHelper.toFXImage(i);
    }

    public static Image renderHouseImage(CoatOfArms coa, GameFileContext ctx, int size, boolean cloth) {
        var houseImg = renderImage(coa, ctx, size, cloth);
        CoatOfArmsRenderer.CK3.applyMask(houseImg, GameImage.CK3_HOUSE_MASK);

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scaleFactor = (double) size / CoatOfArmsRenderer.CK3.REF_IMG_SIZE;
        CoatOfArmsRenderer.CK3.renderImage(
                g,
                houseImg,
                20 * scaleFactor,
                20 * scaleFactor,
                i.getWidth() - (40 * scaleFactor),
                i.getHeight() - (40 * scaleFactor));

        CoatOfArmsRenderer.CK3.renderImage(
                g,
                ImageHelper.fromFXImage(GameImage.CK3_HOUSE_FRAME),
                -25 * scaleFactor,
                -15 * scaleFactor,
                houseImg.getWidth() + (33 * scaleFactor),
                houseImg.getHeight() + (30 * scaleFactor));

        return ImageHelper.toFXImage(i);
    }

    public static Image renderTitleImage(CoatOfArms coa, GameFileContext ctx, int size, boolean cloth) {
        var titleImg = renderImage(coa, ctx, size, cloth);
        CoatOfArmsRenderer.CK3.applyMask(titleImg, GameImage.CK3_TITLE_MASK);

        BufferedImage i = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scaleFactor = (double) size / CoatOfArmsRenderer.CK3.REF_IMG_SIZE;
        CoatOfArmsRenderer.CK3.renderImage(
                g,
                titleImg,
                13 * scaleFactor,
                13 * scaleFactor,
                i.getWidth() - (28 * scaleFactor),
                i.getHeight() - (28 * scaleFactor));

        CoatOfArmsRenderer.CK3.renderImage(
                g,
                ImageHelper.fromFXImage(GameImage.CK3_TITLE_FRAME),
                -6 * scaleFactor,
                -4 * scaleFactor,
                titleImg.getWidth() + (11 * scaleFactor),
                titleImg.getHeight() + (11 * scaleFactor));

        return ImageHelper.toFXImage(i);
    }

}
