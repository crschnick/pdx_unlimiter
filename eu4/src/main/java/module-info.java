@SuppressWarnings("all")
module com.crschnick.pdx_unlimiter.eu4 {
    requires java.se;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    exports com.crschnick.pdx_unlimiter.eu4.parser;
    exports com.crschnick.pdx_unlimiter.eu4.format;
    exports com.crschnick.pdx_unlimiter.eu4.savegame;
    exports com.crschnick.pdx_unlimiter.eu4.data;
}