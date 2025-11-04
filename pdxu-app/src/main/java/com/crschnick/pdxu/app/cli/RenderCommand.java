package com.crschnick.pdxu.app.cli;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.Eu5TagRenderer;
import com.crschnick.pdxu.app.gui.game.Vic3TagRenderer;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.coa.CoatOfArms;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import picocli.CommandLine;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;

@CommandLine.Command(
        name = "render",
        header = "Renders coat of arms images for a given game and saves them into a directory.",
        sortOptions = false,
        showDefaultValues = true)
public class RenderCommand implements Runnable {

    @CommandLine.Option(
            names = {"-g", "--game"},
            description = "The game id (ck3, vic3, eu5)",
            paramLabel = "<game>",
            required = true,
            converter = GameConverter.class)
    Game game = null;

    @CommandLine.Option(
            names = {"-m", "--mod"},
            description =
                    "Additional mods to include when looking for coat of arms definitions. Note that the mod name has to be specified here, "
                            + "not its location.",
            paramLabel = "<mod>")
    List<String> mods = List.of();

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "The output directory",
            required = true,
            paramLabel = "<output>")
    Path output;

    @CommandLine.Option(
            names = {"-l", "--selector"},
            description =
                    "An optional selector. If set, only the selected coat of arms by name is rendered and outputted. Note that you can also "
                            + "specify multiple selectors.",
            paramLabel = "<selector>")
    List<String> selector;

    @CommandLine.Option(
            names = {"-h", "--height"},
            description =
                    "The output image height. The image width is calculated by multiplying the aspect ratio with this value.",
            paramLabel = "<height>")
    int size = 256;

    @SneakyThrows
    @Override
    public void run() {

        if (!game.isEnabled()) {
            throw new IllegalArgumentException("Game is not set up yet. Please launch the GUI first");
        }

        Files.createDirectories(output);
        FileUtils.cleanDirectory(output.toFile());

        FailableBiConsumer<String, BufferedImage, Exception> map = (s, bufferedImage) -> {
            var target = output.resolve(s + ".png");
            ImageIO.write(bufferedImage, "png", target.toFile());
        };

        switch (game) {
            case EU4, VIC2, CK2, HOI4, STELLARIS -> {
                throw new IllegalArgumentException("Unsupported game");
            }
            case CK3 -> {
                ck3(map);
            }
            case VIC3 -> {
                vic3(map);
            }
            case EU5 -> {
                eu5(map);
            }
        }
    }

    private void eu5(FailableBiConsumer<String, BufferedImage, Exception> consumer) throws Exception {
        var directory = Path.of("main_menu", "common").resolve("coat_of_arms").resolve("coat_of_arms");

        var mods = this.mods.stream()
                .map(s -> GameInstallation.ALL
                        .get(Game.EU5)
                        .getModForSavegameId(s)
                        .orElseThrow(() -> new IllegalArgumentException("Mod not found: " + s)))
                .toList();

        var context = GameFileContext.forGameAndMods(Game.EU5, mods);
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(directory, context, files::add);

        var all = Eu5TagRenderer.getCoatOfArmsNode(context);
        all.forEach(
                (s, node) -> {
                    if (!node.isArray()) {
                        return;
                    }

                    if (selector != null && selector.stream().noneMatch(sel -> sel.equalsIgnoreCase(s))) {
                        return;
                    }

                    try {
                        System.out.println("Rendering " + s + " ...");
                        var coa = Eu5TagRenderer.getCoatOfArms(node.getArrayNode(), all);
                        var image = Eu5TagRenderer.renderImage(coa, context, (int) (1.5 * size), size);
                        consumer.accept(s, image);
                    } catch (Exception exception) {
                        System.err.println(String.format("Error for %s:", s));
                        exception.printStackTrace();
                    }
                },
                false);
    }

    private void ck3(FailableBiConsumer<String, BufferedImage, Exception> consumer) throws Exception {
        var directory = Path.of("common").resolve("coat_of_arms").resolve("coat_of_arms");

        var mods = this.mods.stream()
                .map(s -> GameInstallation.ALL
                        .get(Game.CK3)
                        .getModForSavegameId(s)
                        .orElseThrow(() -> new IllegalArgumentException("Mod not found: " + s)))
                .toList();

        var context = GameFileContext.forGameAndMods(Game.CK3, mods);
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(directory, context, files::add);

        var all = new LinkedArrayNode(files.stream()
                .map(path -> {
                    ArrayNode content = null;
                    try {
                        content = TextFormatParser.ck3().parse(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.<ArrayNode>empty();
                    }

                    // Skip templates
                    if (content.size() == 1) {
                        return Optional.<ArrayNode>empty();
                    }

                    NodeEvaluator.evaluateArrayNode(content);
                    return Optional.of(content);
                })
                .flatMap(Optional::stream)
                .toList());

        all.forEach(
                (s, node) -> {
                    if (!node.isArray()) {
                        return;
                    }

                    if (selector != null && selector.stream().noneMatch(sel -> sel.equalsIgnoreCase(s))) {
                        return;
                    }

                    try {
                        System.out.println("Rendering " + s + " ...");
                        var coa = CoatOfArms.fromNode(node, null);
                        var image = Ck3TagRenderer.renderImage(coa, context, size, false);
                        consumer.accept(s, image);
                    } catch (Exception exception) {
                        System.err.println(String.format("Error for %s:", s));
                        exception.printStackTrace();
                    }
                },
                false);
    }

    private void vic3(FailableBiConsumer<String, BufferedImage, Exception> consumer) throws Exception {
        var directory = Path.of("common").resolve("coat_of_arms").resolve("coat_of_arms");

        var mods = this.mods.stream()
                .map(s -> GameInstallation.ALL
                        .get(Game.VIC3)
                        .getModForSavegameId(s)
                        .orElseThrow(() -> new IllegalArgumentException("Mod not found: " + s)))
                .toList();

        var context = GameFileContext.forGameAndMods(Game.VIC3, mods);
        var files = new ArrayList<Path>();
        CascadeDirectoryHelper.traverseDirectory(directory, context, files::add);

        var all = Vic3TagRenderer.getCoatOfArmsNode(context);
        all.forEach(
                (s, node) -> {
                    if (!node.isArray()) {
                        return;
                    }

                    if (selector != null && selector.stream().noneMatch(sel -> sel.equalsIgnoreCase(s))) {
                        return;
                    }

                    try {
                        System.out.println("Rendering " + s + " ...");
                        var coa = Vic3TagRenderer.getCoatOfArms(node.getArrayNode(), all);
                        var image = Vic3TagRenderer.renderImage(coa, context, (int) (1.5 * size), size);
                        consumer.accept(s, image);
                    } catch (Exception exception) {
                        System.err.println(String.format("Error for %s:", s));
                        exception.printStackTrace();
                    }
                },
                false);
    }
}
