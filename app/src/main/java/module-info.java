module com.crschnick.pdx_unlimiter.app {
    requires java.se;
    requires com.crschnick.pdx_unlimiter.eu4;
    requires com.realityinteractive.imageio.tga;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires net.nikr.dds;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;
    requires io.sentry;
    requires org.jnativehook;

    requires org.slf4j;
    requires org.slf4j.simple;

    uses javax.imageio.spi.ImageReaderSpi;
    uses net.nikr.dds.DDSImageReaderSpi;

    exports com.crschnick.pdx_unlimiter.app;
    exports com.crschnick.pdx_unlimiter.app.savegame_mgr;
}