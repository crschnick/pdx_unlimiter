package com.crschnick.pdx_unlimiter.app.editor;

import javafx.beans.property.ObjectProperty;

import java.util.Queue;

public class EditorNavHistory {

    private Queue<EditorNavPath> history;
    private ObjectProperty<EditorNavPath> currentPath;
}
