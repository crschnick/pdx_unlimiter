module com.crschnick.pdx_unlimiter.app {
    requires com.crschnick.pdx_unlimiter.core;
    requires com.realityinteractive.imageio.tga;
    requires org.apache.commons.lang;
    requires org.apache.commons.io;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires net.nikr.dds;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.jnativehook;
    requires org.kordamp.iconli.core;
    requires org.kordamp.ikonli.javafx;
    requires com.jfoenix;
    requires com.google.gson;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires io.sentry;
    requires org.apache.commons.collections4;
    requires java.desktop;
    requires java.net.http;

    exports com.crschnick.pdx_unlimiter.app;
    exports com.crschnick.pdx_unlimiter.app.core;
    exports com.crschnick.pdx_unlimiter.app.util;
    exports com.crschnick.pdx_unlimiter.app.savegame;
    exports com.crschnick.pdx_unlimiter.app.gui;
    uses com.crschnick.pdx_unlimiter.app.savegame.EditorProvider;
}
