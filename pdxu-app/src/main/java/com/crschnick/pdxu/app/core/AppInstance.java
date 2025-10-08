package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.beacon.AppBeacon;
import com.crschnick.pdxu.app.core.beacon.AppBeaconMessage;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.ThreadHelper;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AppInstance {

    public static void init() {
        var reachable = AppBeacon.get().isExistingBeaconRunning();
        if (!reachable) {
            // Even in case we are unable to reach another beacon server
            // there might be another instance running, for example
            // starting up
            if (!AppDataLock.hasLock()) {
                TrackEvent.info(
                        "Data directory " + AppProperties.get().getDataDir().toString()
                                + " is already locked. Is another instance running?");
                AppOperationMode.halt(1);
            }

            // We are good to start up!
            return;
        }

        try {
            var inputs = AppProperties.get().getArguments().getOpenArgs();
            // Assume that we want to open the GUI if we launched again
            AppBeacon.get().sendRequest(AppBeaconMessage.FocusRequest.builder().build());
            if (!inputs.isEmpty()) {
                AppBeacon.get()
                        .sendRequest(AppBeaconMessage.OpenRequest.builder()
                                .arguments(inputs)
                                .build());
            }
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
            return;
        }

        if (OsType.ofLocal() == OsType.MACOS) {
            Desktop.getDesktop().setOpenURIHandler(e -> {
                try {
                    AppBeacon.get()
                            .sendRequest(AppBeaconMessage.OpenRequest.builder()
                                    .arguments(List.of(e.getURI().toString()))
                                    .build());
                } catch (Exception ex) {
                    ErrorEventFactory.fromThrowable(ex).expected().omit().handle();
                }
            });
            ThreadHelper.sleep(1000);
        }
        TrackEvent.info("Another instance is already running. Quitting ...");
        AppOperationMode.halt(1);
    }
}
