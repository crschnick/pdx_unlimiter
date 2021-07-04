package com.crschnick.pdxu.app.gui.editor;


import com.crschnick.pdxu.app.editor.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.ImageLoader;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.function.Function;

public abstract class GuiEditorNodeTagFactory {

    public abstract boolean checkIfApplicable(EditorState state, EditorSimpleNode node);

    public abstract Node create(EditorState state, EditorSimpleNode node);

    public static abstract class ImagePreviewNodeTagFactory extends GuiEditorNodeTagFactory {

        private final Function<EditorSimpleNode, Path> fileFunction;

        public ImagePreviewNodeTagFactory(Function<EditorSimpleNode, Path> fileFunction) {
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

    public static class InfoNodeTagFactory extends GuiEditorNodeTagFactory {

        private final String keyName;
        private final Function<EditorSimpleNode, String> descFunction;

        public InfoNodeTagFactory(String keyName, Function<EditorSimpleNode, String> descFunction) {
            this.keyName = keyName;
            this.descFunction = descFunction;
        }

        public InfoNodeTagFactory(String keyName, String desc) {
            this.keyName = keyName;
            this.descFunction = e -> desc;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorSimpleNode node) {
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
}
