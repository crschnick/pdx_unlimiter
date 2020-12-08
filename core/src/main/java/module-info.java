module com.crschnick.pdx_unlimiter.core {
    requires java.se;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    exports com.crschnick.pdx_unlimiter.core.parser;
    exports com.crschnick.pdx_unlimiter.core.format;
    exports com.crschnick.pdx_unlimiter.core.savegame;
    exports com.crschnick.pdx_unlimiter.core.data;
    exports com.crschnick.pdx_unlimiter.core.io;
}