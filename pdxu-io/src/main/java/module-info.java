open module com.crschnick.pdxu.io {
    exports com.crschnick.pdxu.io.parser;
    exports com.crschnick.pdxu.io.savegame;
    exports com.crschnick.pdxu.io.node;

    requires static org.graalvm.polyglot;
    requires static lombok;
}