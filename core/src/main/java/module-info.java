module com.crschnick.pdx_unlimiter.core {
    requires java.se;
    requires org.apache.commons.compress;

    exports com.crschnick.pdx_unlimiter.core.parser;
    exports com.crschnick.pdx_unlimiter.core.savegame;
    exports com.crschnick.pdx_unlimiter.core.data;
}