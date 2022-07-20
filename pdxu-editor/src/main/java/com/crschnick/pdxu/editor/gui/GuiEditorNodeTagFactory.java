package com.crschnick.pdxu.editor.gui;


import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

public abstract class GuiEditorNodeTagFactory {

    public abstract boolean checkIfApplicable(EditorState state, EditorRealNode node);

    public abstract Node create(EditorState state, EditorRealNode node, Region valueDisplay);

    public static abstract class ImagePreviewNodeTagFactory extends GuiEditorNodeTagFactory {

        private final String icon;
        private final Function<EditorRealNode, Path> fileFunction;

        public ImagePreviewNodeTagFactory(String icon, Function<EditorRealNode, Path> fileFunction) {
            this.icon = icon;
            this.fileFunction = fileFunction;
        }

        @Override
        public Node create(EditorState state, EditorRealNode node, Region valueDisplay) {
            var b = new JFXButton(null, new FontIcon());
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon(icon));
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
                    var img = ImageHelper.loadImage(found);
                    var imgView = new ImageView(img);
                    var tt = GuiTooltips.createTooltip(imgView);
                    tt.setShowDelay(Duration.ZERO);
                    b.setTooltip(tt);
                });
            });
            return b;
        }
    }


    public static class CacheTagFactory extends GuiEditorNodeTagFactory {
        private final Set<String> keyNames;

        public CacheTagFactory(Set<String> keyNames) {
            this.keyNames = keyNames;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            return node.getKeyName().map(k -> keyNames.contains(k)).orElse(false);
        }

        @Override
        public Node create(EditorState state, EditorRealNode node, Region valueDisplay) {
            var b = new JFXButton(null, new FontIcon());
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon("mdi-information-outline"));

            var tt = GuiTooltips.createTooltip("The contents of this value are recalculated every time you launch your game. " +
                    "Therefore, any changes made to this value will not apply to your game.");
            tt.setShowDelay(Duration.ZERO);
            Tooltip.install(b, tt);
            return b;
        }
    }

    public static class InfoNodeTagFactory extends GuiEditorNodeTagFactory {

        private final Set<String> keyNames;
        private final Function<EditorRealNode, String> descFunction;

        public InfoNodeTagFactory(Set<String> keyNames, String desc) {
            this.keyNames = keyNames;
            this.descFunction = e -> desc;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            return node.getKeyName().map(k -> keyNames.contains(k)).orElse(false);
        }

        @Override
        public Node create(EditorState state, EditorRealNode node, Region valueDisplay) {
            var b = new JFXButton(null, new FontIcon());
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
            if (valueDisplay != null) {
                valueDisplay.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    GuiDialogHelper.showBlockingAlert(alert -> {
                        alert.getDialogPane().setMaxWidth(Settings.getInstance().maxTooltipWidth.getValue());
                        alert.setAlertType(Alert.AlertType.WARNING);
                        alert.setTitle("Node information");
                        alert.setHeaderText(descFunction.apply(node));
                    });
                });
            }
            return b;
        }
    }
}
