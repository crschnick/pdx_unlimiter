package com.crschnick.pdxu.app.cli;

import com.crschnick.pdxu.app.core.CacheManager;
import com.crschnick.pdxu.app.core.ComponentManager;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.ck3.Ck3CoatOfArms;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "render",
        sortOptions = false
)
public class RenderCommand implements Runnable {

    @CommandLine.Option(
            names = {
                    "-g",
                    "--game"
            },
            description = "The game id",
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

            case EU4 -> {
            }
            case HOI4 -> {
            }
            case CK3 -> {
                ck3(map);
            }
            case STELLARIS -> {
            }
            case CK2 -> {
            }
            case VIC2 -> {
            }
            case VIC3 -> {
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
            for (Path path : list.toList()) {
                var content = TextFormatParser.ck3().parse(path);
                content.forEach((s, node) -> {
                    if (!node.isArray()) {
                        return;
                    }

                    try {
                        var coa = Ck3CoatOfArms.fromNode(node);
                        var image = Ck3TagRenderer.renderImage(coa, GameFileContext.forGame(Game.CK3), 512, false);
                        consumer.accept(s, image);
                    } catch (Exception exception) {
                        System.err.println(String.format("Error for %s@%s:", s, directory.relativize(path)));
                        exception.printStackTrace();
                    }
                }, false);
            }
        }
    }
}
