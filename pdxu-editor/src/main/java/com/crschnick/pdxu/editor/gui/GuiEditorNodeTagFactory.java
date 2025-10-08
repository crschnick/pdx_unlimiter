package com.crschnick.pdxu.editor.gui;


import com.crschnick.pdxu.app.comp.base.TooltipHelper;
import com.crschnick.pdxu.app.core.window.AppSideWindow;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.DesktopHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
            var b = new Button(null, new FontIcon());
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon(icon));
            b.setOnAction(e -> {
                CascadeDirectoryHelper.openFile(fileFunction.apply(node), state.getFileContext()).ifPresent(found -> {
                    DesktopHelper.browseFileInDirectory(found);
                });
            });
            b.setOnMouseEntered(e -> {
                if (b.getTooltip() != null) {
                    return;
                }

                CascadeDirectoryHelper.openFile(fileFunction.apply(node), state.getFileContext()).ifPresent(found -> {
                    var img = ImageHelper.loadImage(found);
                    var imgView = new ImageView(img);
                    var tt = new Tooltip();
                    tt.setGraphic(imgView);
                    tt.getStyleClass().add("fancy-tooltip");
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
            var b = new Button(null, new FontIcon());
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon("mdi-information-outline"));

            var tt = TooltipHelper.create(new ReadOnlyStringWrapper("The contents of this value are recalculated every time you launch your game. " +
                    "Therefore, any changes made to this value will not apply to your game."), null);
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
            var b = new Button(null, new FontIcon());
            b.setAlignment(Pos.CENTER);
            b.setGraphic(new FontIcon("mdi-information-outline"));
            b.setOnMouseEntered(e -> {
                if (b.getTooltip() != null) {
                    return;
                }

                var tt = TooltipHelper.create(new ReadOnlyStringWrapper(descFunction.apply(node)), null);
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(b, tt);
            });
            if (valueDisplay != null) {
                valueDisplay.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                    AppSideWindow.showBlockingAlert(alert -> {
                        alert.getDialogPane().setMaxWidth(500);
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
