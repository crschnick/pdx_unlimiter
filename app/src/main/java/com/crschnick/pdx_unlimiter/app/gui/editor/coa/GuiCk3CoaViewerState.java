package com.crschnick.pdx_unlimiter.app.gui.editor.coa;

import com.crschnick.pdx_unlimiter.app.editor.EditorSimpleNode;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdx_unlimiter.app.gui.game.ImageLoader;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

public class GuiCk3CoaViewerState {

    public static enum FrameType {
        HOUSE,
        TITLE
    }

    private ObjectProperty<FrameType> frameType;
    private IntegerProperty size;
    private EditorState state;
    private EditorSimpleNode editorNode;
    private ObjectProperty<Image> image;

    GuiCk3CoaViewerState(EditorState state, EditorSimpleNode editorNode) {
        this.state = state;
        this.editorNode = editorNode;
        this.size = new SimpleIntegerProperty(256);
        this.frameType = new SimpleObjectProperty<>(FrameType.HOUSE);
        this.image = new SimpleObjectProperty<>(ImageLoader.DEFAULT_IMAGE);
    }

    private void updateImage() {
        Ck3TagRenderer.houseImage()
    }
}
