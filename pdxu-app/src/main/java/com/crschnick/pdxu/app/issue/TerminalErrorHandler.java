package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.platform.PlatformInit;
import com.crschnick.pdxu.app.update.AppDistributionType;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ModuleLayerLoader;
import com.crschnick.pdxu.app.util.ThreadHelper;

public class TerminalErrorHandler extends GuiErrorHandlerBase implements ErrorHandler {

    private final ErrorHandler log = new LogErrorHandler();

    @Override
    public void handle(ErrorEvent event) {
        log.handle(event);

        if (event.isOmitted() || AppOperationMode.isInShutdown()) {
            ErrorAction.ignore().handle(event);
            // Wait a bit to the beacon the ability to respond to any open requests with an error
            ThreadHelper.sleep(3000);
            AppOperationMode.halt(1);
            return;
        }

        if (!startupGui(throwable -> {
            handleWithSecondaryException(event, throwable);
            ErrorAction.ignore().handle(event);
        })) {
            // Exit if we couldn't initialize the GUI
            // Wait a bit to the beacon the ability to respond to any open requests with an error
            ThreadHelper.sleep(3000);
            AppOperationMode.halt(1);
            return;
        }

        handleGui(event);
    }

    private void handleGui(ErrorEvent event) {
        try {
            AppProperties.init();
            ModuleLayerLoader.loadAll(ModuleLayer.boot(), t -> {
                ErrorEventFactory.fromThrowable(t).handle();
            });
            PlatformInit.init(true);
            ErrorHandlerDialog.showAndWait(event);
        } catch (Throwable r) {
            event.clearAttachments();
            handleWithSecondaryException(event, r);
            return;
        }

        if (AppOperationMode.isInStartup() && !AppProperties.get().isDevelopmentEnvironment()) {
            handleProbableUpdate();
        }

        ThreadHelper.sleep(1000);
        AppOperationMode.halt(1);
    }

    private void handleWithSecondaryException(ErrorEvent event, Throwable t) {
        ErrorAction.ignore().handle(event);

        var second = ErrorEventFactory.fromThrowable(t).build();
        log.handle(second);
        ErrorAction.ignore().handle(second);
        ThreadHelper.sleep(1000);
        AppOperationMode.halt(1);
    }

    private void handleProbableUpdate() {
        if (AppProperties.get().isDevelopmentEnvironment()) {
            return;
        }

        try {
            var rel = AppDistributionType.get().getUpdateHandler().refreshUpdateCheck();
            if (rel != null) {
                var updateModal = ModalOverlay.of(
                        "updateAvailableTitle",
                        AppDialog.dialogText(AppI18n.get("updateAvailableContent", rel.getVersion())));
                updateModal.addButton(
                        new ModalButton("checkOutUpdate", () -> Hyperlinks.open(rel.getReleaseUrl()), false, true));
                updateModal.addButton(new ModalButton("ignore", null, true, false));
                AppDialog.showAndWait(updateModal);
            }
        } catch (Throwable t) {
            var event = ErrorEventFactory.fromThrowable(t).build();
            log.handle(event);
            ErrorAction.ignore().handle(event);
            ThreadHelper.sleep(1000);
            AppOperationMode.halt(1);
        }
    }
}
