module com.crschnick.pdx_unlimiter.updater {
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires java.desktop;
    requires io.sentry;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j;

    exports com.crschnick.pdx_unlimiter.updater;
}