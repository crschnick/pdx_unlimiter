module com.crschnick.pdx_unlimiter.editor {
    requires com.crschnick.pdx_unlimiter.core;
    requires com.crschnick.pdx_unlimiter.app;
    requires java.se;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.kordamp.iconli.core;
    requires org.kordamp.ikonli.javafx;
    requires com.jfoenix;
    requires org.apache.commons.io;

    provides com.crschnick.pdx_unlimiter.app.savegame.EditorProvider with com.crschnick.pdx_unlimiter.editor.EditorImpl;
}