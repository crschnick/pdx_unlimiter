
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.editor.Editor;

module com.crschnick.pdxu.editor {
    provides EditorProvider with Editor;

    uses com.crschnick.pdxu.editor.adapter.EditorSavegameAdapter;
    provides com.crschnick.pdxu.editor.adapter.EditorSavegameAdapter with
            com.crschnick.pdxu.editor.adapter.Eu4SavegameAdapter,
            com.crschnick.pdxu.editor.adapter.Ck3SavegameAdapter,
            com.crschnick.pdxu.editor.adapter.Hoi4SavegameAdapter,
            com.crschnick.pdxu.editor.adapter.StellarisSavegameAdapter,
            com.crschnick.pdxu.editor.adapter.Ck2SavegameAdapter,
            com.crschnick.pdxu.editor.adapter.Vic2SavegameAdapter,
            com.crschnick.pdxu.editor.adapter.Vic3SavegameAdapter,
            com.crschnick.pdxu.editor.adapter.NoGameAdapter;

    exports com.crschnick.pdxu.editor;
    exports com.crschnick.pdxu.editor.node;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    requires com.crschnick.pdxu.app;
    requires com.crschnick.pdxu.io;
    requires com.crschnick.pdxu.model;
    requires org.slf4j;
    requires com.jfoenix;
    requires org.kordamp.ikonli.javafx;
    requires org.apache.commons.io;
    requires org.apache.commons.collections4;
    requires org.apache.commons.lang3;
    requires atlantafx.base;


}