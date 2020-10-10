module com.crschnick.pdx_unlimiter.app {
    requires java.se;
    requires javafx.controls;
    requires javafx.swing;
    requires com.crschnick.pdx_unlimiter.eu4;
    requires com.realityinteractive.imageio.tga;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires net.nikr.dds;

    uses javax.imageio.spi.ImageReaderSpi;
    uses net.nikr.dds.DDSImageReaderSpi;

    exports com.crschnick.pdx_unlimiter.app;
    exports com.crschnick.pdx_unlimiter.app.savegame_mgr;
}