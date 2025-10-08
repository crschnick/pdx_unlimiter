package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.core.App;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.platform.PlatformInit;
import com.crschnick.pdxu.app.util.ModuleLayerLoader;

import java.util.function.Consumer;

public class GuiErrorHandlerBase {

    protected boolean startupGui(Consumer<Throwable> onFail) {
        try {
            AppProperties.init();
            ModuleLayerLoader.loadAll(ModuleLayer.boot(), t -> {
                ErrorEventFactory.fromThrowable(t).handle();
            });

            if (PlatformInit.isLoadingThread()) {
                return false;
            }

            if (App.getApp() == null) {
                return false;
            }

            PlatformInit.init(true);
            AppMainWindow.init(true);
        } catch (Throwable ex) {
            onFail.accept(ex);
            return false;
        }

        return true;
    }
}
