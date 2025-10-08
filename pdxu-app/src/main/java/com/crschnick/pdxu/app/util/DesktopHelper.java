package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.core.AppSystemInfo;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;

import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShellAPI;
import com.sun.jna.platform.win32.User32;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DesktopHelper {

    private static final String[] browsers = {
        "xdg-open", "google-chrome", "firefox", "opera", "konqueror", "mozilla", "gnome-open", "open"
    };

    public static void openUrlInBrowser(String uri) {
        try {
            if (OsType.ofLocal() == OsType.WINDOWS) {
                var pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", uri);
                pb.directory(AppSystemInfo.ofCurrent().getUserHome().toFile());
                pb.redirectErrorStream(true);
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.start();
            } else if (OsType.ofLocal() == OsType.LINUX) {
                String browser = null;
                for (String b : browsers) {
                    if (browser == null
                            && Runtime.getRuntime()
                                            .exec(new String[] {"which", b})
                                            .getInputStream()
                                            .read()
                                    != -1) {
                        Runtime.getRuntime().exec(new String[] {browser = b, uri});
                    }
                }
            } else {
                var pb = new ProcessBuilder("open", uri);
                pb.directory(AppSystemInfo.ofCurrent().getUserHome().toFile());
                pb.redirectErrorStream(true);
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.start();
            }
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }
    }

    public static void browsePath(Path file) {
        if (file == null) {
            return;
        }

        if (!Files.exists(file)) {
            return;
        }

        ThreadHelper.runAsync(() -> {
            var xdg = OsType.ofLocal() == OsType.LINUX;
            if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(file.toFile().toURI());
                    return;
                } catch (Exception e) {
                    ErrorEventFactory.fromThrowable(e).expected().omitted(xdg).handle();
                }
            }

            if (xdg) {
                LocalExec.readStdoutIfPossible("xdg-open", file.toString());
            }
        });
    }

    public static void browseFileInDirectory(Path file) {
        if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
            browsePath(file.getParent());
            return;
        }

        ThreadHelper.runAsync(() -> {
            var xdg = OsType.ofLocal() == OsType.LINUX;
            try {
                Desktop.getDesktop().browseFileDirectory(file.toFile());
                return;
            } catch (Exception e) {
                ErrorEventFactory.fromThrowable(e).expected().omitted(xdg).handle();
            }

            if (xdg) {
                LocalExec.readStdoutIfPossible("xdg-open", file.getParent().toString());
            }
        });
    }

    public static void openWithAnyApplication(Path localFile) {
        try {
            switch (OsType.ofLocal()) {
                case OsType.Windows ignored -> {
                    // See https://learn.microsoft.com/en-us/windows/win32/api/shellapi/ns-shellapi-shellexecuteinfoa
                    var struct = new ShellAPI.SHELLEXECUTEINFO();
                    struct.fMask = 0x100 | 0xC;
                    struct.lpVerb = "openas";
                    struct.lpFile = localFile.toString();
                    struct.nShow = User32.SW_SHOWDEFAULT;
                    Shell32.INSTANCE.ShellExecuteEx(struct);
                }
                case OsType.Linux ignored -> {
                    throw new UnsupportedOperationException();
                }
                case OsType.MacOs ignored -> {
                    throw new UnsupportedOperationException();
                }
            }
        } catch (Throwable e) {
            ErrorEventFactory.fromThrowable("Unable to open file " + localFile, e)
                    .handle();
        }
    }

    public static void openInDefaultApplication(Path localFile) {
        switch (OsType.ofLocal()) {
            case OsType.Linux linux -> {
                LocalExec.readStdoutIfPossible("xdg-open", localFile.toString());
            }
            case OsType.MacOs macOs -> {
                LocalExec.readStdoutIfPossible("open", localFile.toString());
            }
            case OsType.Windows windows -> {
                LocalExec.readStdoutIfPossible("cmd", "/c", "start \"\" \"" + localFile.toString() + "\"");
            }
        }
    }
}
