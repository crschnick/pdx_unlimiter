package com.crschnick.pdxu.app.platform;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppLayoutModel;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ClipboardHelper {

    private static final AppLayoutModel.QueueEntry COPY_QUEUE_ENTRY = new AppLayoutModel.QueueEntry(
            AppI18n.observable("passwordCopied"),
            new LabelGraphic.IconGraphic("mdi2c-clipboard-check-outline"),
            () -> {});

    private static void apply(Map<DataFormat, Object> map, boolean showNotification) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        Map<DataFormat, Object> contents = Stream.of(
                        DataFormat.PLAIN_TEXT,
                        DataFormat.URL,
                        DataFormat.RTF,
                        DataFormat.HTML,
                        DataFormat.IMAGE,
                        DataFormat.FILES)
                .map(dataFormat -> {
                    try {
                        // This can fail if the clipboard data is invalid
                        return new AbstractMap.SimpleEntry<>(dataFormat, clipboard.getContent(dataFormat));
                    } catch (Exception e) {
                        return new AbstractMap.SimpleEntry<>(dataFormat, null);
                    }
                })
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
        contents.putAll(map);
        contents.entrySet().removeIf(e -> e.getValue() == null);
        clipboard.setContent(contents);

        if (showNotification) {
            AppLayoutModel.get().showQueueEntry(COPY_QUEUE_ENTRY, java.time.Duration.ofSeconds(15), true);
        }
    }

    public static void copyText(String s) {
        PlatformThread.runLaterIfNeeded(() -> {
            apply(Map.of(DataFormat.PLAIN_TEXT, s), true);
        });
    }

    public static void copyUrl(String s) {
        PlatformThread.runLaterIfNeeded(() -> {
            apply(Map.of(DataFormat.URL, s, DataFormat.PLAIN_TEXT, s), true);
        });
    }
}
