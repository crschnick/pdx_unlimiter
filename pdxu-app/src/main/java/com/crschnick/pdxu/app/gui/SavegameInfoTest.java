package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SavegameInfoTest {

    private record Failure(String location, String stage, Throwable error) {}

    public static void run() {
        var failures = new ArrayList<Failure>();
        var report = new StringBuilder();
        int total = 0;
        for (var storage : SavegameStorage.ALL.values()) {
            var game = SavegameStorage.ALL.getKey(storage);
            if (GameInstallation.ALL.containsKey(game)) {
                GameImage.loadGameImages(game);
            }
            GameCacheManager.getInstance().onGameChange();
            var count = test(storage, failures);
            total += count;
            report.append(storage.getName()).append(": ").append(count).append(" infos\n");
        }

        report.append("\n").append(failures.size()).append(" failures in ").append(total).append(" infos\n");
        for (var failure : failures) {
            report.append("\n")
                    .append(failure.location())
                    .append(" [")
                    .append(failure.stage())
                    .append("]\n")
                    .append(failure.error())
                    .append("\n");
            for (var element : failure.error().getStackTrace()) {
                if (element.getClassName().startsWith("com.crschnick")) {
                    report.append("    at ").append(element).append("\n");
                }
            }
        }

        try {
            var file = Files.createTempFile("pdxu-savegame-info-test", ".txt");
            Files.writeString(file, report);
            TrackEvent.info("Savegame info test finished, report written to " + file);
            var summary = failures.size() + " failures in " + total + " infos.\nReport: " + file;
            ErrorEventFactory.fromMessage(summary).handle();
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
        }
    }

    private static <T, I extends SavegameInfo<T>> int test(SavegameStorage<T, I> storage, List<Failure> failures) {
        int count = 0;
        for (var campaign : List.copyOf(storage.getCollections())) {
            GameCacheManager.getInstance().onSelectedSavegameCollectionChange();
            var latest = campaign.getSavegames().stream().max(Comparator.comparing(SavegameEntry::getDate));
            for (var entry : latest.stream().toList()) {
                count++;
                var location = storage.getName() + " / " + campaign.getName() + " / " + entry.getName() + " ("
                        + storage.getSavegameFile(entry) + ")";
                TrackEvent.info("Testing " + location);

                SavegameStorage.ParsedSavegame parsed;
                try {
                    parsed = storage.parseContent(entry);
                } catch (Throwable t) {
                    failures.add(new Failure(location, "parse", t));
                    continue;
                }

                var info = testInfo(storage, parsed, location, failures);
                if (info == null) {
                    continue;
                }

                if (!supportsSimulatedPlayer(info.getData())) {
                    continue;
                }

                var tags = info.getData().getAllTags();
                var playable = tags.stream()
                        .filter(t -> info.getData().isPlayableTag(parsed.content(), t))
                        .toList();
                TrackEvent.info("Simulating " + playable.size() + " of " + tags.size() + " tags");
                for (var tag : playable) {
                    var tagLocation = location + " as " + describe(tag);
                    SavegameInfo.simulatePlayer(tag, tags, () -> testInfo(storage, parsed, tagLocation, failures));
                }
                count += playable.size();
            }
        }
        return count;
    }

    private static <T, I extends SavegameInfo<T>> I testInfo(
            SavegameStorage<T, I> storage,
            SavegameStorage.ParsedSavegame parsed,
            String location,
            List<Failure> failures) {
        var result = new ArrayList<I>();
        SavegameInfo.collectCompErrors(t -> failures.add(new Failure(location, "comp", t)), () -> {
            I info;
            try {
                info = storage.createInfo(parsed);
            } catch (Throwable t) {
                failures.add(new Failure(location, "info", t));
                return;
            }

            try {
                info.createContainer();
            } catch (Throwable t) {
                failures.add(new Failure(location, "create", t));
            }
            result.add(info);
        });
        return result.isEmpty() ? null : result.getFirst();
    }

    private static boolean supportsSimulatedPlayer(SavegameData<?> data) {
        try {
            // The generic init overload is overridden through a bridge method with the erased signature
            data.getClass().getDeclaredMethod("init", SavegameContent.class, Object.class, List.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static String describe(Object tag) {
        for (var getter : List.of("getTag", "getTagId", "getId")) {
            try {
                return String.valueOf(tag.getClass().getMethod(getter).invoke(tag));
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return String.valueOf(tag);
    }
}
