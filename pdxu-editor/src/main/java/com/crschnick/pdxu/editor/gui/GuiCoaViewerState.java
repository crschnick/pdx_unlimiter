package com.crschnick.pdxu.editor.gui;


import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.Vic3TagRenderer;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeEnvironment;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.util.Map;


public abstract class GuiCoaViewerState<T extends GuiCoaDisplayType> {

    public static class Ck3GuiCoaViewerState extends GuiCoaViewerState<GuiCk3CoaDisplayType> {

        public Ck3GuiCoaViewerState(EditorState state, EditorRealNode editorNode) {
            super(state, editorNode, GuiCk3CoaDisplayType.REALM);
        }

        @Override
        protected void setup(HBox box) {
            GuiCk3CoaDisplayType.init(this, box);
        }

        @Override
        protected CoatOfArms createCoatOfArms() {
            var additional = state.isSavegame() ?
                    new ArrayNode[0] :
                    state.getRootNodes()
                            .values()
                            .stream()
                            .map(editorRootNode -> editorRootNode.getBackingNode().copy().getArrayNode())
                            .toArray(ArrayNode[]::new);
            var all = Ck3TagRenderer.getCoatOfArmsNode(GameFileContext.forGame(Game.CK3), additional);
            return Ck3TagRenderer.getCoatOfArms(editorNode.getBackingNode().getArrayNode(), all);
        }
    }

    public static class Vic3GuiCoaViewerState extends GuiCoaViewerState<GuiVic3CoaDisplayType> {

        public Vic3GuiCoaViewerState(EditorState state, EditorRealNode editorNode) {
            super(state, editorNode, GuiVic3CoaDisplayType.NONE);
        }

        @Override
        protected void setup(HBox box) {
            GuiVic3CoaDisplayType.init(this, box);
        }

        @Override
        protected CoatOfArms createCoatOfArms() {
            var additional = state.isSavegame() ?
                    new ArrayNode[0] :
                    state.getRootNodes()
                            .values()
                            .stream()
                            .map(editorRootNode -> editorRootNode.getBackingNode().copy().getArrayNode())
                            .toArray(ArrayNode[]::new);

            var all = Vic3TagRenderer.getCoatOfArmsNode(GameFileContext.forGame(Game.VIC3), additional);
            var allCopy = all.copy().getArrayNode();
            var env = new NodeEnvironment(Map.of());
            NodeEvaluator.evaluateArrayNode(allCopy, env);

            var coaNode = editorNode.getBackingNode().copy().getArrayNode();
            NodeEvaluator.evaluateArrayNode(coaNode, env);

            return Vic3TagRenderer.getCoatOfArms(coaNode, allCopy);
        }
    }

    private ObjectProperty<T> displayType;
    protected EditorState state;
    protected EditorRealNode editorNode;
    private ObjectProperty<CoatOfArms> parsedCoa;
    private ObjectProperty<Image> image;

    GuiCoaViewerState(EditorState state, EditorRealNode editorNode, T initial) {
        this.state = state;
        this.editorNode = editorNode;
        this.displayType = new SimpleObjectProperty<>(initial);
        this.image = new SimpleObjectProperty<>(ImageHelper.DEFAULT_IMAGE);
        this.parsedCoa = new SimpleObjectProperty<>(createCoatOfArms());
    }

    void init(HBox box) {
        setup(box);
        refresh();
        createExportButton(box);
    }

    private void createExportButton(HBox box) {
        Button refresh = new Button();
        refresh.setOnAction(e -> {
            FileChooser dirChooser = new FileChooser();
            dirChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            dirChooser.setTitle(AppI18n.get("selectFile"));
            File file = dirChooser.showSaveDialog(AppMainWindow.get().getStage());
            if (file == null) {
                return;
            }

            try {
                ImageHelper.writePng(image.get(), file.toPath());
            } catch (IOException ex) {
                ErrorEventFactory.fromThrowable(ex).handle();
            }
        });
        refresh.setGraphic(new FontIcon("mdi-export"));
        box.getChildren().add(refresh);
    }

    protected abstract void setup(HBox box);

    protected abstract CoatOfArms createCoatOfArms();

    void refresh() {
        // The data might not be valid anymore
        if (!editorNode.isValid()) {
            return;
        }

        parsedCoa.set(createCoatOfArms());
        updateImage();
    }

    void updateImage() {
        if (parsedCoa.get() == null) {
            image.set(ImageHelper.DEFAULT_IMAGE);
        } else {
            image.set(displayType.get().render(parsedCoa.get(), state.getFileContext()));
        }
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public T getDisplayType() {
        return displayType.get();
    }

    public ObjectProperty<T> displayTypeProperty() {
        return displayType;
    }
}
