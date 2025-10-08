package com.crschnick.pdxu.app.core.beacon;

import com.crschnick.pdxu.app.core.AppOpenArguments;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.core.window.AppMainWindow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AppBeaconMessage.FocusRequest.class),
    @JsonSubTypes.Type(value = AppBeaconMessage.ExitRequest.class),
    @JsonSubTypes.Type(value = AppBeaconMessage.OpenRequest.class)
})
public interface AppBeaconMessage {

    void handle();

    @Value
    @Builder
    @Jacksonized
    @JsonTypeName("focus")
    class FocusRequest implements AppBeaconMessage {

        @Override
        public void handle() {
            if (!AppOperationMode.switchToSyncIfPossible(AppOperationMode.GUI)) {
                return;
            }

            var w = AppMainWindow.get();
            if (w != null) {
                w.focus(true);
            }
        }
    }

    @Value
    @Builder
    @Jacksonized
    @JsonTypeName("exit")
    public static class ExitRequest implements AppBeaconMessage {

        @Override
        public void handle() {}
    }

    @Value
    @Builder
    @Jacksonized
    @JsonTypeName("open")
    public static class OpenRequest implements AppBeaconMessage {

        List<String> arguments;

        @Override
        public void handle() {
            AppOpenArguments.handle(arguments);
        }
    }
}
