package com.crschnick.pdx_unlimiter.app.gui.editor;


import com.crschnick.pdx_unlimiter.app.editor.EditorNode;
import javafx.scene.Node;

public abstract class GuiEditorNodeImage {

    public abstract boolean checkIfApplicable(EditorNode node);

    public abstract Node create(EditorNode node);
}
