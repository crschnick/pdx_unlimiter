module com.crschnick.pdx_unlimiter.core {
    requires java.se;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires io.sentry;

    exports com.crschnick.pdx_unlimiter.core.parser;
    exports com.crschnick.pdx_unlimiter.core.savegame;
    exports com.crschnick.pdx_unlimiter.core.node;
    exports com.crschnick.pdx_unlimiter.core.info;
    exports com.crschnick.pdx_unlimiter.core.info.eu4;
    exports com.crschnick.pdx_unlimiter.core.info.ck3;
    exports com.crschnick.pdx_unlimiter.core.info.hoi4;
    exports com.crschnick.pdx_unlimiter.core.info.stellaris;
}