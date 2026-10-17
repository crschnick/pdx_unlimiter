package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.core.AppLogs;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.nio.file.Path;
import java.util.*;

@Builder
@Getter
public class ErrorEvent {

    // Throwable does not override equals, so this is an identity set
    // Weak references ensure that handled throwables can still be garbage collected
    private static final Set<Throwable> HANDLED =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Builder.Default
    private final boolean omitted = false;

    @Builder.Default
    private final boolean reportable = true;

    private final Throwable throwable;

    @Singular
    private final List<ErrorAction> customActions;

    private String description;
    private boolean terminal;

    @Setter
    private boolean shouldSendDiagnostics;

    @Singular
    private List<Path> attachments;

    private String link;

    private String email;
    private String userReport;
    private boolean unhandled;

    public void attachUserReport(String email, String text) {
        this.email = email;
        userReport = text;
    }

    private boolean shouldIgnore(Throwable throwable) {
        // Guard against cause cycles
        var visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        var current = throwable;
        while (current != null && visited.add(current)) {
            if (HANDLED.contains(current) && !terminal) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public void handle() {
        // Check object identity to allow for multiple exceptions with same trace
        if (shouldIgnore(throwable)) {
            return;
        }

        handleImpl();
        if (throwable != null) {
            HANDLED.add(throwable);
        }
    }

    private void handleImpl() {
        if (AppLogs.get() != null && AppLogs.get().getSessionLogsDirectory() != null) {
            addAttachment(AppLogs.get().getSessionLogsDirectory());
        }

        if (AppProperties.get() != null && AppProperties.get().isAotTrainMode()) {
            new LogErrorHandler().handle(this);
            if (this.isTerminal()) {
                AppOperationMode.halt(1);
            }
            return;
        }

        if (this.isTerminal()) {
            new TerminalErrorHandler().handle(this);
            return;
        }

        // Don't block shutdown
        if (AppOperationMode.isInShutdown()) {
            ErrorAction.ignore().handle(this);
            new LogErrorHandler().handle(this);
            return;
        }

        if (AppOperationMode.get() == null) {
            AppOperationMode.BACKGROUND.getErrorHandler().handle(this);
        } else {
            AppOperationMode.get().getErrorHandler().handle(this);
        }
    }

    public void addAttachment(Path file) {
        attachments = new ArrayList<>(attachments);
        attachments.add(file);
    }

    public void clearAttachments() {
        attachments = new ArrayList<>();
    }

    public static class ErrorEventBuilder {

        public ErrorEventBuilder term() {
            return terminal(true);
        }

        public ErrorEventBuilder omit() {
            return omitted(true);
        }

        public ErrorEventBuilder expected() {
            return reportable(false);
        }

        public ErrorEventBuilder discard() {
            return omit().expected();
        }

        public ErrorEventBuilder ignore() {
            if (throwable != null) {
                HANDLED.add(throwable);
            }
            return this;
        }

        public ErrorEvent handle() {
            var event = build();
            event.handle();
            return event;
        }

        Throwable getThrowable() {
            return throwable;
        }

        String getLink() {
            return link;
        }
    }
}
