package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.comp.base.TileButtonComp;
import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.UserReportComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import com.crschnick.pdxu.app.util.DesktopHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;

import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import javax.management.MBeanServer;

public class TroubleshootCategory extends AppPrefsCategory {

    @SneakyThrows
    private static void heapDump() {
        var file =
                AppSystemInfo.ofCurrent().getDesktop().resolve(AppNames.ofMain().getSnakeName() + ".hprof");
        FileUtils.deleteQuietly(file.toFile());
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
                server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        mxBean.dumpHeap(file.toString(), true);
        DesktopHelper.browseFileInDirectory(file);
    }

    @Override
    public String getId() {
        return "troubleshoot";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdoal-bug_report");
    }

    @Override
    public Comp<?> create() {
        OptionsBuilder b = new OptionsBuilder()
                .addTitle("troubleshootingOptions")
                .spacer(19)
                .addComp(
                        new TileButtonComp("reportIssue", "reportIssueDescription", "mdal-bug_report", e -> {
                                    var event = ErrorEventFactory.fromMessage("User Report");
                                    if (AppLogs.get().isWriteToFile()) {
                                        event.attachment(AppLogs.get().getSessionLogsDirectory());
                                    }
                                    UserReportComp.show(event.build());
                                    e.consume();
                                })
                                .maxWidth(2000),
                        null)
                .addComp(
                        new TileButtonComp("launchDebugMode", "launchDebugModeDescription", "mdmz-refresh", e -> {
                                    AppOperationMode.executeAfterShutdown(() -> {
                                        var script = AppInstallation.ofCurrent().getDebugScriptPath();
                                        DesktopHelper.openInDefaultApplication(script);
                                    });
                                    e.consume();
                                })
                                .maxWidth(2000),
                        null);

        if (AppLogs.get().isWriteToFile()) {
            b.addComp(
                    new TileButtonComp(
                                    "openCurrentLogFile", "openCurrentLogFileDescription", "mdmz-text_snippet", e -> {
                                        AppLogs.get().flush();
                                        ThreadHelper.sleep(100);
                                        DesktopHelper.browsePath(AppLogs.get()
                                                .getSessionLogsDirectory()
                                                .resolve(AppNames.ofMain().getKebapName() + ".log"));
                                        e.consume();
                                    })
                            .maxWidth(2000),
                    null);
        }

        b.addComp(
                        new TileButtonComp(
                                        "openInstallationDirectory",
                                        "openInstallationDirectoryDescription",
                                        "mdomz-snippet_folder",
                                        e -> {
                                            DesktopHelper.browsePath(
                                                    AppInstallation.ofCurrent().getBaseInstallationPath());
                                            e.consume();
                                        })
                                .maxWidth(2000),
                        null)
                .addComp(
                        new TileButtonComp(
                                        "clearUserData", "clearUserDataDescription", "mdi2t-trash-can-outline", e -> {
                                            var modal = ModalOverlay.of(
                                                    "clearUserDataTitle",
                                                    AppDialog.dialogTextKey("clearUserDataContent"));
                                            modal.withDefaultButtons(() -> {
                                                ThreadHelper.runFailableAsync(() -> {
                                                    var dir =
                                                            AppProperties.get().getDataDir();
                                                    try (var stream = Files.list(dir)) {
                                                        var dirs = stream.toList();
                                                        for (var path : dirs) {
                                                            if (path.getFileName()
                                                                            .toString()
                                                                            .equals("logs")
                                                                    || path.getFileName()
                                                                            .toString()
                                                                            .equals("shell")) {
                                                                continue;
                                                            }

                                                            FileUtils.deleteQuietly(path.toFile());
                                                        }
                                                    }
                                                    AppOperationMode.halt(0);
                                                });
                                            });
                                            modal.show();
                                            e.consume();
                                        })
                                .maxWidth(2000),
                        null)
                .addComp(
                        new TileButtonComp("clearCaches", "clearCachesDescription", "mdi2t-trash-can-outline", e -> {
                                    var modal = ModalOverlay.of(
                                            "clearCachesAlertTitle",
                                            AppDialog.dialogTextKey("clearCachesAlertContent"));
                                    modal.withDefaultButtons(() -> {
                                        AppCache.clear();
                                    });
                                    modal.show();
                                    e.consume();
                                })
                                .maxWidth(2000),
                        null)
                .addComp(
                        new TileButtonComp("createHeapDump", "createHeapDumpDescription", "mdi2m-memory", e -> {
                                    heapDump();
                                    e.consume();
                                })
                                .maxWidth(2000),
                        null);

        return b.buildComp();
    }
}
