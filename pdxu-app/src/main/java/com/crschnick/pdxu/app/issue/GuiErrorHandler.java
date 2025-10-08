package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.platform.LabelGraphic;

import java.time.Duration;

public class GuiErrorHandler extends GuiErrorHandlerBase implements ErrorHandler {

    private final ErrorHandler log = new LogErrorHandler();

    @Override
    public void handle(ErrorEvent event) {
        log.handle(event);

        if (!startupGui(throwable -> {
            var second = ErrorEventFactory.fromThrowable(throwable).build();
            log.handle(second);
            ErrorAction.ignore().handle(second);
        })) {
            return;
        }

        if (event.isOmitted()) {
            ErrorAction.ignore().handle(event);
            if (AppLayoutModel.get() != null) {
                AppLayoutModel.get()
                        .showQueueEntry(
                                new AppLayoutModel.QueueEntry(
                                        AppI18n.observable("errorOccurred"),
                                        new LabelGraphic.IconGraphic("mdoal-error_outline"),
                                        () -> {
                                            handleGui(event);
                                        }),
                                Duration.ofSeconds(10),
                                true);
            }
            return;
        }

        handleGui(event);
    }

    private void handleGui(ErrorEvent event) {
        ErrorHandlerDialog.showAndWait(event);
    }
}
