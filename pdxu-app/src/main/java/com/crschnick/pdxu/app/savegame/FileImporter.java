package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.gui.dialog.GuiImporter;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import javafx.application.Platform;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FileImporter {

    public static void onArgumentsPass(List<String> arguments) {
        var importTargets = arguments.stream()
                .map(FileImportTarget::createTargets)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        FileImporter.importTargets(importTargets);
    }

    public static void onFileDrop(List<File> files) {
        var importTargets = files.stream()
                .map(File::toString)
                .map(FileImportTarget::createTargets)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        FileImporter.importTargets(importTargets);
    }

    public static void importTargets(Collection<? extends FileImportTarget> targets) {
        Map<FileImportTarget, SavegameParseResult> statusMap = new HashMap<>();
        targets.forEach(t -> t.importTarget(s -> {
            // Only save non success results
            s.ifPresent(result -> {
                statusMap.put(t, result);
            });

            if (AppPrefs.get().deleteOnImport().getValue()) {
                TrackEvent.debug("Deleting import target " + t.getName());
                t.delete();
            }
        }));
        TaskExecutor.getInstance().submitTask(
                () -> {
                    // Report errors
                    statusMap.entrySet().stream()
                            .filter(e -> e.getValue() instanceof SavegameParseResult.Error)
                            .findFirst()
                            .ifPresent(e -> {
                                ErrorEventFactory.fromThrowable(((SavegameParseResult.Error) e.getValue()).error).handle();
                            });

                    Platform.runLater(() -> GuiImporter.showResultDialog(statusMap));
                }, false);
    }

    private static void importFromString(String input) {
        TrackEvent.debug("Starting to import " + input);
        var targets = FileImportTarget.createTargets(input);
        if (targets.size() == 0) {
            TrackEvent.debug("No targets to import");

        } else {
            for (FileImportTarget t : targets) {
                TrackEvent.debug("Starting to import target " + t.getName() + " from " + input);
                t.importTarget(s -> {
                    if (AppPrefs.get().deleteOnImport().getValue()) {
                        TrackEvent.debug("Deleting import target " + t.getName());
                        t.delete();
                    }
                });
            }
        }
    }
}
