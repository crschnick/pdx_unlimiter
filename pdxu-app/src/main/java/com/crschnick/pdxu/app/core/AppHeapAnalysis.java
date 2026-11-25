package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.gui.GuiLayoutComp;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.util.ThreadHelper;

import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.SneakyThrows;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import javax.management.ObjectName;

public class AppHeapAnalysis {

    private static final int ROUNDS = 5;

    public static void run() throws Throwable {
        // Warm up caches and lazy initialization so that only per-round growth remains
        round();

        var histograms = new ArrayList<Map<String, Long>>();
        histograms.add(histogram());
        for (int i = 0; i < ROUNDS; i++) {
            round();
            histograms.add(histogram());
        }

        var before = histograms.getFirst();
        var after = histograms.getLast();
        // Only report classes that grow steadily, retained state of the last round or GC noise do not
        var leaks = after.entrySet().stream()
                .filter(e -> isRelevant(e.getKey()))
                .filter(e -> e.getValue() - before.getOrDefault(e.getKey(), 0L) >= ROUNDS)
                .filter(e -> growsContinually(histograms, e.getKey()))
                // A one-time increase in the first rounds is lazy initialization, a leak still grows later on
                .filter(e -> e.getValue() > histograms.get(ROUNDS / 2).getOrDefault(e.getKey(), 0L))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .toList();
        for (var leak : leaks) {
            TrackEvent.withWarn("Possible leak")
                    .tag("class", leak.getKey())
                    .tag("before", before.getOrDefault(leak.getKey(), 0L))
                    .tag("after", leak.getValue())
                    .tag(
                            "rounds",
                            histograms.stream()
                                    .map(h -> String.valueOf(h.getOrDefault(leak.getKey(), 0L)))
                                    .collect(Collectors.joining(" ")))
                    .handle();
        }

        if (!leaks.isEmpty()) {
            dumpHeap("heap.hprof");
        } else {
            TrackEvent.withInfo("No leaks detected").handle();
        }

        AppOperationMode.switchToSyncOrThrow(AppOperationMode.GUI);
    }

    public static void runAfterShutdown() throws Exception {
        var remaining = histogram().entrySet().stream()
                .filter(e -> isRelevant(e.getKey()))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(50)
                .toList();
        for (var entry : remaining) {
            TrackEvent.withInfo("Remaining after shutdown")
                    .tag("class", entry.getKey())
                    .tag("instances", entry.getValue())
                    .handle();
        }
        dumpHeap("heap-shutdown.hprof");
    }

    private static boolean isRelevant(String className) {
        return className.startsWith("javafx.scene.") || className.startsWith("com.crschnick.pdxu.");
    }

    private static void dumpHeap(String name) throws Exception {
        var file = AppProperties.get().getDataDir().resolve(name);
        Files.deleteIfExists(file);
        ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class).dumpHeap(file.toString(), true);
        TrackEvent.withWarn("Heap dump written").tag("file", file).handle();
    }

    private static boolean growsContinually(List<Map<String, Long>> histograms, String name) {
        for (int i = 1; i < histograms.size(); i++) {
            if (histograms.get(i).getOrDefault(name, 0L) < histograms.get(i - 1).getOrDefault(name, 0L)) {
                return false;
            }
        }
        return true;
    }

    @SneakyThrows
    private static void round() {
        AppOperationMode.switchToSyncOrThrow(AppOperationMode.GUI);
        ThreadHelper.sleep(1000);

        var mw = AppMainWindow.get();
        PlatformThread.runLaterIfNeededBlocking(() -> {
            AppStyle.reloadStylesheets(mw.getStage().getScene());
        });

        for (var entry : AppLayoutModel.get().getEntries()) {
            AppLayoutModel.get().getSelected().setValue(entry);
            if (entry.comp() instanceof GuiLayoutComp layout) {
                openFirstSavegame(layout.getSavegameManagerState());
            }
            ThreadHelper.sleep(3000);
        }

        AppOperationMode.switchToSyncOrThrow(AppOperationMode.BACKGROUND);
    }

    private static <T, I extends SavegameInfo<T>> void openFirstSavegame(SavegameManagerState<T, I> state) {
        var campaign = state.getShownCollections().stream().findFirst().orElse(null);
        if (campaign == null) {
            return;
        }

        state.selectCollectionAsync(campaign);
        if (waitFor(
                () -> campaign.equals(state.globalSelectedCollectionProperty().get())
                        && !state.getShownEntries().isEmpty(),
                10000)) {
            var savegame = state.getShownEntries().stream().findFirst().orElse(null);
            if (savegame != null) {
                // Entries are normally only loaded once their list cell is shown
                state.loadEntryAsync(savegame);
                if (waitFor(() -> savegame.getState() == SavegameEntry.State.LOADED, 30000)) {
                    PlatformThread.runLaterIfNeededBlocking(() -> state.selectEntry(savegame));
                    ThreadHelper.sleep(5000);
                }
            }
        }

        // Unselect again so that the next round loads everything again instead of keeping the selection
        state.selectCollectionAsync(null);
        waitFor(() -> state.globalSelectedCollectionProperty().get() == null, 10000);
    }

    private static boolean waitFor(BooleanSupplier condition, long timeoutMillis) {
        var end = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < end) {
            if (condition.getAsBoolean()) {
                return true;
            }
            ThreadHelper.sleep(100);
        }
        return condition.getAsBoolean();
    }

    private static Map<String, Long> histogram() throws Exception {
        for (int i = 0; i < 3; i++) {
            System.gc();
            ThreadHelper.sleep(500);
        }

        var out = (String) ManagementFactory.getPlatformMBeanServer()
                .invoke(
                        new ObjectName("com.sun.management:type=DiagnosticCommand"),
                        "gcClassHistogram",
                        new Object[] {new String[0]},
                        new String[] {String[].class.getName()});
        var map = new HashMap<String, Long>();
        for (var line : out.lines().toList()) {
            // Format: "   1:   12345   678900  java.lang.String (java.base@25)"
            var parts = line.trim().split("\s+");
            if (parts.length >= 4 && parts[0].endsWith(":")) {
                map.merge(parts[3], Long.parseLong(parts[1]), Long::sum);
            }
        }
        return map;
    }
}
