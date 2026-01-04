package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;

import java.awt.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class DesktopHelper {

    public static void openUrl(String uri) {
        if (uri == null) {
            return;
        }

        URI parsed;
        try {
            parsed = URI.create(uri);
        } catch (IllegalArgumentException e) {
            ErrorEventFactory.fromThrowable("Invalid URI: " + uri, e.getCause() != null ? e.getCause() : e)
                    .handle();
            return;
        }

        if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            if (OsType.ofLocal() == OsType.LINUX) {
                LocalExec.executeAsync("xdg-open", parsed.toString());
                return;
            }
        }

        // This can be a blocking operation
        ThreadHelper.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(parsed);
                return;
            } catch (Exception e) {
                // Some basic linux systems have trouble with the API call
                ErrorEventFactory.fromThrowable(e)
                        .expected()
                        .omitted(OsType.ofLocal() == OsType.LINUX)
                        .handle();
            }

            if (OsType.ofLocal() == OsType.LINUX) {
                LocalExec.executeAsync("xdg-open", parsed.toString());
            }
        });
    }

    public static void browseFile(Path file) {
        if (file == null || !Files.exists(file)) {
            return;
        }

        if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            if (OsType.ofLocal() == OsType.LINUX) {
                LocalExec.executeAsync("xdg-open", file.toString());
                return;
            }
        }

        // This can be a blocking operation
        ThreadHelper.runAsync(() -> {
            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(file.toFile().toURI());
                    return;
                } catch (Exception e) {
                    // Some basic linux systems have trouble with the API call
                    ErrorEventFactory.fromThrowable(e)
                            .expected()
                            .omitted(OsType.ofLocal() == OsType.LINUX)
                            .handle();
                }
            }

            if (OsType.ofLocal() == OsType.LINUX) {
                LocalExec.executeAsync("xdg-open", file.toString());
            }
        });
    }

    public static void browseFileInDirectory(Path file) {
        if (file == null || !Files.exists(file)) {
            return;
        }

        // Windows does not support Action.BROWSE_FILE_DIR
        if (OsType.ofLocal() == OsType.WINDOWS) {
            // Explorer does not support single quotes, so use normal quotes
            if (Files.isDirectory(file)) {
                LocalExec.readStdoutIfPossible("explorer", "\"" + file + "\"");
            } else {
                LocalExec.readStdoutIfPossible("explorer", "/select,", "\"" + file + "\"");
            }
            return;
        }

        if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            browseFile(file.getParent());
            return;
        }

        // This can be a blocking operation
        ThreadHelper.runAsync(() -> {
            var xdg = OsType.ofLocal() == OsType.LINUX;
            try {
                Desktop.getDesktop().browseFileDirectory(file.toFile());
                return;
            } catch (Exception e) {
                ErrorEventFactory.fromThrowable(e).expected().omitted(xdg).handle();
            }

            // Some basic linux systems have trouble with the API call
            // As a fallback, use xdg-open
            if (xdg) {
                LocalExec.executeAsync("xdg-open", file.getParent().toString());
            }
        });
    }

    public static void openFileInDefaultApplication(Path file) {
        if (file == null || !Files.exists(file)) {
            return;
        }

        // This can be a blocking operation
        ThreadHelper.runFailableAsync(() -> {
            Desktop.getDesktop().open(file.toFile());
        });
    }
}
