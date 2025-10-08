import com.crschnick.pdxu.app.core.AppLogs;
import com.crschnick.pdxu.app.util.AppJacksonModule;
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.app.util.ModuleLayerLoader;

import com.fasterxml.jackson.databind.Module;
import org.slf4j.spi.SLF4JServiceProvider;

open module com.crschnick.pdxu.app {
    exports com.crschnick.pdxu.app.core;
    exports com.crschnick.pdxu.app.util;
    exports com.crschnick.pdxu.app.issue;
    exports com.crschnick.pdxu.app.comp.base;
    exports com.crschnick.pdxu.app.core.mode;
    exports com.crschnick.pdxu.app.prefs;
    exports com.crschnick.pdxu.app.update;
    exports com.crschnick.pdxu.app.core.check;
    exports com.crschnick.pdxu.app.core.window;
    exports com.crschnick.pdxu.app.comp;
    exports com.crschnick.pdxu.app.platform;
    exports com.crschnick.pdxu.app.page;
    exports com.crschnick.pdxu.app.core.beacon;
    exports com.crschnick.pdxu.app;
    exports com.crschnick.pdxu.app.savegame;
    exports com.crschnick.pdxu.app.info;
    exports com.crschnick.pdxu.app.installation;
    exports com.crschnick.pdxu.app.gui;
    exports com.crschnick.pdxu.app.gui.game;

    requires static lombok;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires org.slf4j;
    requires atlantafx.base;
    requires com.vladsch.flexmark;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires io.xpipe.modulefs;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.web;
    requires javafx.graphics;
    requires org.kordamp.ikonli.javafx;
    requires io.sentry;
    requires info.picocli;
    requires java.instrument;
    requires java.management;
    requires jdk.management;
    requires jdk.management.agent;
    requires java.net.http;
    requires org.kohsuke.github;
    requires com.crschnick.pdxu.io;
    requires com.crschnick.pdxu.model;
    requires com.jfoenix;
    requires org.apache.commons.collections4;
    requires com.realityinteractive.imageio.tga;
    requires java.desktop;
    requires com.github.kwhat.jnativehook;

    // Required runtime modules
    requires jdk.charsets;
    requires jdk.crypto.cryptoki;
    requires jdk.localedata;
    requires jdk.accessibility;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.materialdesign;
    requires org.kordamp.ikonli.bootstrapicons;
    requires org.kordamp.ikonli.feather;
    requires jdk.zipfs;
    requires org.graalvm.polyglot;
    requires org.graalvm.js;
    requires io.github.ititus.ddsiio;

    uses ModuleLayerLoader;
    uses Module;
    uses EditorProvider;

    provides Module with
            AppJacksonModule;
    provides SLF4JServiceProvider with
            AppLogs.Slf4jProvider;
}
