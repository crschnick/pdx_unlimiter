package com.crschnick.pdxu.app.gui.editor;


import com.crschnick.pdxu.app.editor.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.ImageLoader;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class GuiEditorNodeTagFactory {

    private final Game game;

    public GuiEditorNodeTagFactory(Game game) {
        this.game = game;
    }

    public final boolean checkIfApplicable(EditorState state, EditorSimpleNode node) {
        if (!state.getFileContext().getGame().equals(game)) {
            return false;
        }

        return checkIfNodeIsApplicable(state, node);
    }

    protected abstract boolean checkIfNodeIsApplicable(EditorState state, EditorSimpleNode node);

    public abstract Node create(EditorState state, EditorSimpleNode node);

    static abstract class ImagePreviewNodeTagFactory extends GuiEditorNodeTagFactory {

        private final Function<EditorSimpleNode, Path> fileFunction;

        public ImagePreviewNodeTagFactory(Game game, Function<EditorSimpleNode, Path> fileFunction) {
            super(game);
            this.fileFunction = fileFunction;
        }

        @Override
        public Node create(EditorState state, EditorSimpleNode node) {
            var b = new JFXButton();
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            b.setOnAction(e -> {
                CascadeDirectoryHelper.openFile(fileFunction.apply(node), state.getFileContext()).ifPresent(found -> {
                    ThreadHelper.browseDirectory(found);
                });
            });
            b.setOnMouseEntered(e -> {
                if (b.getTooltip() != null) {
                    return;
                }

                CascadeDirectoryHelper.openFile(fileFunction.apply(node), state.getFileContext()).ifPresent(found -> {
                    var img = ImageLoader.loadImage(found);
                    var imgView = new ImageView(img);
                    var tt = GuiTooltips.createTooltip(imgView);
                    tt.setShowDelay(Duration.ZERO);
                    b.setTooltip(tt);
                });
            });
            return b;
        }
    }

    static class InfoNodeTagFactory extends GuiEditorNodeTagFactory {

        private final String keyName;
        private final Function<EditorSimpleNode, String> descFunction;

        public InfoNodeTagFactory(Game game, String keyName, Function<EditorSimpleNode, String> descFunction) {
            super(game);
            this.keyName = keyName;
            this.descFunction = descFunction;
        }

        public InfoNodeTagFactory(Game game, String keyName, String desc) {
            super(game);
            this.keyName = keyName;
            this.descFunction = e -> desc;
        }

        @Override
        protected boolean checkIfNodeIsApplicable(EditorState state, EditorSimpleNode node) {
            return node.getKeyName().map(k -> k.equals(keyName)).orElse(false);
        }

        @Override
        public Node create(EditorState state, EditorSimpleNode node) {
            var b = new Label();
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon("mdi-information-outline"));
            b.setOnMouseEntered(e -> {
                if (b.getTooltip() != null) {
                    return;
                }

                var tt = GuiTooltips.createTooltip(descFunction.apply(node));
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(b, tt);
            });
            return b;
        }
    }

    static final class Ck3ImagePreviewNodeTagFactory extends ImagePreviewNodeTagFactory {

        private final String nodeName;

        public Ck3ImagePreviewNodeTagFactory(Path base, String nodeName) {
            super(Game.CK3, node -> GameInstallation.ALL.get(Game.CK3).getInstallDir().resolve("game")
                    .resolve(base).resolve(node.getBackingNode().getString()));
            this.nodeName = nodeName;
        }

        @Override
        public boolean checkIfNodeIsApplicable(EditorState state, EditorSimpleNode node) {
            return node.getKeyName().map(k -> k.equals(nodeName)).orElse(false);
        }
    }

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(new GuiEditorNodeTagFactory(Game.CK3) {
        @Override
        public boolean checkIfNodeIsApplicable(EditorState state, EditorSimpleNode node) {
            if (node.getBackingNode().isArray()) {
                ArrayNode ar = (ArrayNode) node.getBackingNode();
                return ar.hasKey("pattern");
            }
            return false;
        }

        @Override
        public Node create(EditorState state, EditorSimpleNode node) {
            var b = new JFXButton();
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            GuiTooltips.install(b, "Open in coat of arms preview window");
            b.setOnAction(e -> {
                var viewer = new GuiCk3CoaViewer(state, node);
                viewer.createStage();
            });
            return b;
        }
    }, new Ck3ImagePreviewNodeTagFactory(Path.of("gfx").resolve("coat_of_arms").resolve("patterns"), "pattern"),
            new Ck3ImagePreviewNodeTagFactory(Path.of("gfx").resolve("coat_of_arms").resolve("colored_emblems"), "texture"),
            new InfoNodeTagFactory(Game.CK3, "meta_data", "The meta data of this savegame that is shown in the main menu. Editing anything inside of it only changes the main menu display, not the actual data in-game."));

    public static Optional<Node> createTag(EditorState state, EditorSimpleNode node) {
        for (var fac : FACTORIES) {
            if (fac.checkIfApplicable(state, node)) {
                return Optional.of(fac.create(state, node));
            }
        }
        return Optional.empty();
    }
}
