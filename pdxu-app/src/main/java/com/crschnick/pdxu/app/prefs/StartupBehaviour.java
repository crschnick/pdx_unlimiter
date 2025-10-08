package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.core.mode.AppOperationModeSelection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StartupBehaviour implements PrefsChoiceValue {
    GUI("app.startGui", AppOperationModeSelection.GUI) {},
    TRAY("app.startInTray", AppOperationModeSelection.TRAY) {
        public boolean isSelectable() {
            return AppOperationMode.TRAY.isSupported();
        }
    },
    BACKGROUND("app.startInBackground", AppOperationModeSelection.BACKGROUND) {};

    private final String id;
    private final AppOperationModeSelection mode;
}
