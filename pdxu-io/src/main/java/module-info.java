module com.crschnick.pdxu.io {
    exports com.crschnick.pdxu.io.parser;
    exports com.crschnick.pdxu.io.savegame;
    exports com.crschnick.pdxu.io.node;

    requires org.apache.commons.lang;
    requires org.graalvm.js.scriptengine;
    requires static lombok;
}