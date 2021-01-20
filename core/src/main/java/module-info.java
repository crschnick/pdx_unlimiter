module com.crschnick.pdx_unlimiter.core {
    requires java.se;
    requires org.apache.commons.compress;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports com.crschnick.pdx_unlimiter.core.parser;
    exports com.crschnick.pdx_unlimiter.core.savegame;
    exports com.crschnick.pdx_unlimiter.core.data;
}