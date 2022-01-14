package com.crschnick.pdxu.editor.gui;


import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.model.ck3.Ck3CoatOfArms;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import static com.crschnick.pdxu.editor.gui.GuiCk3CoaDisplayType.REALM;


public class GuiCk3CoaViewerState {

    private ObjectProperty<GuiCk3CoaDisplayType> displayType;
    private EditorState state;
    private EditorRealNode editorNode;
    private ObjectProperty<Ck3CoatOfArms> parsedCoa;
    private ObjectProperty<Image> image;

    GuiCk3CoaViewerState(EditorState state, EditorRealNode editorNode) {
        this.state = state;
        this.editorNode = editorNode;
        this.displayType = new SimpleObjectProperty<>(REALM);
        this.image = new SimpleObjectProperty<>(ImageHelper.DEFAULT_IMAGE);
        this.parsedCoa = new SimpleObjectProperty<>(Ck3CoatOfArms.fromNode(editorNode.getBackingNode()));
    }

    void init(HBox box) {
        GuiCk3CoaDisplayType.init(this, box);
        refresh();
    }

    void refresh() {
        // The data might not be valid anymore
        if (!editorNode.isValid()) {
            return;
        }

        parsedCoa.set(Ck3CoatOfArms.fromNode(editorNode.getBackingNode()));
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

    public GuiCk3CoaDisplayType getDisplayType() {
        return displayType.get();
    }

    public ObjectProperty<GuiCk3CoaDisplayType> displayTypeProperty() {
        return displayType;
    }
}
