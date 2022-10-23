package com.crschnick.pdxu.app.cli;

import com.crschnick.pdxu.app.core.CacheManager;
import com.crschnick.pdxu.app.core.ComponentManager;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.Vic3TagRenderer;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.CoatOfArms;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "render",
        header = "Renders coat of arms images for a given game and saves them into a directory.",
        sortOptions = false,
        showDefaultValues = true
)
public class RenderCommand implements Runnable {

    @CommandLine.Option(
            names = {
                    "-g",
                    "--game"
            },
            description = "The game id (ck3 or vic3)",
            paramLabel = "<game>",
            required = true,
            converter = GameConverter.class
    )
    Game game = null;

    @CommandLine.Option(
            names = {
                    "-o",
                    "--output"
            },
            description = "The output directory",
            paramLabel = "<output>"
    )
    Path output;

    @CommandLine.Option(
            names = {
                    "-l",
                    "--selector"
            },
            description = "An optional selector. If set, only the selected coat of arms by name is rendered and outputted.",
            paramLabel = "<selector>"
    )
    String selector;

    @CommandLine.Option(
            names = {
                    "-h",
                    "--height"
            },
            description = "The output image height. The image width is calculated by multiplying the aspect ratio with this value.",
            paramLabel = "<height>"
    )
    int size = 256;

    @SneakyThrows
    @Override
    public void run() {
        ComponentManager.initialSetup(List.of());
        LanguageManager.init();
        Settings.init();
        CacheManager.init();
        PdxuI18n.init();
        GameInstallation.init();
        SavegameStorage.init();

        if (!game.isEnabled()) {
            throw new IllegalArgumentException("Game is not set up yet");
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
        }
    }

    private void ck3(FailableBiConsumer<String, BufferedImage, Exception> consumer) throws Exception {
        var directory = GameInstallation.ALL.get(Game.CK3)
                .getInstallDir()
                .resolve("game")
                .resolve("common")
                .resolve("coat_of_arms")
                .resolve("coat_of_arms");

        try (Stream<Path> list = Files.list(directory)) {
            var all = new LinkedArrayNode(list.map(path -> {
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
            }).flatMap(Optional::stream).toList());

            all.forEach((s, node) -> {
                if (!node.isArray()) {
                    return;
                }

                if (selector != null && !s.equalsIgnoreCase(selector)) {
                    return;
                }

                try {
                    var coa = CoatOfArms.fromNode(node, null);
                    var image = Ck3TagRenderer.renderImage(coa, GameFileContext.forGame(Game.CK3), size, false);
                    consumer.accept(s, image);
                } catch (Exception exception) {
                    System.err.println(String.format("Error for %s:", s));
                    exception.printStackTrace();
                }
            }, false);
        }
    }

    private void vic3(FailableBiConsumer<String, BufferedImage, Exception> consumer) throws Exception {
        var directory = GameInstallation.ALL.get(Game.VIC3)
                .getInstallDir()
                .resolve("game")
                .resolve("common")
                .resolve("coat_of_arms")
                .resolve("coat_of_arms");
        try (Stream<Path> list = Files.list(directory)) {
            var all = new LinkedArrayNode(list.map(path -> {
                ArrayNode content = null;
                try {
                    content = TextFormatParser.vic3().parse(path);
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
            }).flatMap(Optional::stream).toList());

            all.forEach((s, node) -> {
                if (!node.isArray()) {
                    return;
                }

                if (selector != null && !s.equalsIgnoreCase(selector)) {
                    return;
                }

                try {
                    var coa = CoatOfArms.fromNode(node, parent -> all.getNodeForKeyIfExistent(parent).orElse(null));
                    var image = Vic3TagRenderer.renderImage(coa, GameFileContext.forGame(Game.VIC3), (int) (1.5 * size), size);
                    consumer.accept(s, image);
                } catch (Exception exception) {
                    System.err.println(String.format("Error for %s:", s));
                    exception.printStackTrace();
                }
            }, false);
        }
    }
}
