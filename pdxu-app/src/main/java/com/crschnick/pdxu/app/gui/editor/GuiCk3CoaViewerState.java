package com.crschnick.pdxu.app.gui.editor;


import com.crschnick.pdxu.app.editor.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.game.ImageLoader;
import com.crschnick.pdxu.model.ck3.Ck3CoatOfArms;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import static com.crschnick.pdxu.app.gui.editor.GuiCk3CoaDisplayType.REALM;


public class GuiCk3CoaViewerState {

    private ObjectProperty<GuiCk3CoaDisplayType> displayType;
    private EditorState state;
    private EditorSimpleNode editorNode;
    private ObjectProperty<Ck3CoatOfArms> parsedCoa;
    private ObjectProperty<Image> image;

    GuiCk3CoaViewerState(EditorState state, EditorSimpleNode editorNode) {
        this.state = state;
        this.editorNode = editorNode;
        this.displayType = new SimpleObjectProperty<>(REALM);
        this.image = new SimpleObjectProperty<>(ImageLoader.DEFAULT_IMAGE);
        this.parsedCoa = new SimpleObjectProperty<>(Ck3CoatOfArms.fromNode(editorNode.getBackingNode()));

        setupListeners();
    }

    void init(HBox box) {
        GuiCk3CoaDisplayType.init(this, box);
        updateImage();
    }

    private void setupListeners() {
//        editorNode.backingNodeProperty().addListener((c,o,n) -> {
//            parsedCoa.set(Ck3CoatOfArms.fromNode(n));
//        });

        parsedCoa.addListener((c,o,n) -> {
            updateImage();
        });
    }

    void updateImage() {
        if (parsedCoa.get() == null) {
            image.set(ImageLoader.DEFAULT_IMAGE);
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

    public GuiCk3CoaDisplayType getDisplayType() {
        return displayType.get();
    }

    public ObjectProperty<GuiCk3CoaDisplayType> displayTypeProperty() {
        return displayType;
    }
}
