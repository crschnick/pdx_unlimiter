package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.AppResources;
import com.crschnick.pdxu.app.core.AppSystemInfo;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.platform.MarkdownHelper;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

public class MarkdownComp extends Comp<CompStructure<StackPane>> {

    private static Boolean WEB_VIEW_SUPPORTED;

    private final ObservableValue<String> markdown;
    private final UnaryOperator<String> htmlTransformation;
    private final boolean bodyPadding;

    public MarkdownComp(String markdown, UnaryOperator<String> htmlTransformation, boolean bodyPadding) {
        this.markdown = new SimpleStringProperty(markdown);
        this.htmlTransformation = htmlTransformation;
        this.bodyPadding = bodyPadding;
    }

    public MarkdownComp(
            ObservableValue<String> markdown, UnaryOperator<String> htmlTransformation, boolean bodyPadding) {
        this.markdown = markdown;
        this.htmlTransformation = htmlTransformation;
        this.bodyPadding = bodyPadding;
    }

    private Path getHtmlFile(String markdown) {
        if (markdown == null) {
            return null;
        }

        // Key the file by the final html so that the padding and transformation are taken into account
        var html = MarkdownHelper.toHtml(markdown, s -> s, htmlTransformation, bodyPadding ? "padded" : null);
        // Rebuild files for updates in case the css have been changed
        var hash = AppProperties.get().isImage()
                ? sha256(html + AppProperties.get().getVersion())
                : sha256(html);
        var file = AppSystemInfo.ofCurrent().getTemp().resolve("webview").resolve("md-" + hash + ".html");
        if (Files.exists(file)) {
            return file;
        }

        try {
            // Workaround for https://bugs.openjdk.org/browse/JDK-8199014
            FileUtils.forceMkdir(file.getParent().toFile());
            Files.writeString(file, html);
            return file;
        } catch (IOException e) {
            // Any possible IO errors can occur here
            ErrorEventFactory.fromThrowable(e).expected().handle();
            return null;
        }
    }

    @SneakyThrows
    private static String sha256(String s) {
        var digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    @SneakyThrows
    private WebView createWebView() {
        var wv = new WebView();
        wv.setMinWidth(100);
        wv.setMinHeight(100);
        wv.getEngine().setJavaScriptEnabled(false);
        wv.setContextMenuEnabled(false);
        wv.setPageFill(Color.TRANSPARENT);
        wv.getEngine()
                .setUserDataDirectory(
                        AppProperties.get().getDataDir().resolve("webview").toFile());
        var theme = AppPrefs.get() != null
                        && AppPrefs.get().theme().getValue() != null
                        && AppPrefs.get().theme().getValue().isDark()
                ? "misc/github-markdown-dark.css"
                : "misc/github-markdown-light.css";
        var url = AppResources.getResourceURL(AppResources.MAIN_MODULE, theme).orElseThrow();
        wv.getEngine().setUserStyleSheetLocation(url.toString());

        // The content is loaded asynchronously, so hide the web view until then
        // Otherwise, the empty initial page is briefly shown as a white rectangle
        wv.setVisible(false);
        wv.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED || newValue == Worker.State.FAILED) {
                wv.setVisible(true);
            }
        });

        var counter = new AtomicInteger();
        markdown.subscribe(val -> {
            // Rendering the markdown and writing the file can take a while, so do it in the background
            var current = counter.incrementAndGet();
            ThreadHelper.runAsync(() -> {
                var file = getHtmlFile(val);
                if (file == null) {
                    return;
                }

                PlatformThread.runLaterIfNeeded(() -> {
                    // Ignore outdated results if the markdown changed in the meantime
                    if (current != counter.get()) {
                        return;
                    }

                    wv.getEngine().load(file.toUri().toString());
                });
            });
        });

        wv.getStyleClass().add("markdown-comp");
        addLinkHandler(wv.getEngine());
        return wv;
    }

    private void addLinkHandler(WebEngine engine) {
        engine.getLoadWorker()
                .stateProperty()
                .addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                    String toBeopen =
                            engine.getLoadWorker().getMessage().strip().replace("Loading ", "");
                    if (toBeopen.contains("http://") || toBeopen.contains("https://") || toBeopen.contains("mailto:")) {
                        engine.getLoadWorker().cancel();
                        Hyperlinks.open(toBeopen);
                    }
                }));
    }

    @Override
    public CompStructure<StackPane> createBase() {
        var sp = new StackPane();

        if (OsType.ofLocal() == OsType.WINDOWS && AppProperties.get().getArch().equals("arm64")) {
            WEB_VIEW_SUPPORTED = false;
        }

        if (WEB_VIEW_SUPPORTED == null || WEB_VIEW_SUPPORTED) {
            try {
                var wv = createWebView();
                WEB_VIEW_SUPPORTED = true;
                sp.getChildren().addAll(wv);
            } catch (Throwable t) {
                ErrorEventFactory.fromThrowable(t).handle();
                WEB_VIEW_SUPPORTED = false;
            }
        }

        if (!WEB_VIEW_SUPPORTED) {
            var text = new TextArea();
            text.setEditable(false);
            text.setWrapText(true);
            markdown.subscribe(s -> {
                PlatformThread.runLaterIfNeeded(() -> {
                    text.setText(s);
                });
            });
            sp.getChildren().add(text);
        }

        return new SimpleCompStructure<>(sp);
    }
}
