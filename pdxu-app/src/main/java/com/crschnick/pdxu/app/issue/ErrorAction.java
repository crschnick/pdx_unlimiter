package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.util.FailableSupplier;
import com.crschnick.pdxu.app.util.Hyperlinks;

public interface ErrorAction {

    static ErrorAction openDocumentation(String link) {
        return translated("openDocumentation", () -> {
            Hyperlinks.open(link);
            return false;
        });
    }

    static ErrorAction translated(String key, FailableSupplier<Boolean> r) {
        return new ErrorAction() {
            @Override
            public String getName() {
                return AppI18n.get(key);
            }

            @Override
            public String getDescription() {
                return AppI18n.get(key + "Description");
            }

            @Override
            public boolean handle(ErrorEvent event) {
                return r.get();
            }
        };
    }

    static IgnoreAction ignore() {
        return new IgnoreAction();
    }

    String getName();

    String getDescription();

    boolean handle(ErrorEvent event);

    class IgnoreAction implements ErrorAction {
        @Override
        public String getName() {
            return AppI18n.get("ignoreError");
        }

        @Override
        public String getDescription() {
            return AppI18n.get("ignoreErrorDescription");
        }

        @Override
        public boolean handle(ErrorEvent event) {
            if (!event.isReportable()) {
                return true;
            }

            if (event.isShouldSendDiagnostics()) {
                SentryErrorHandler.getInstance().handle(event);
            }
            return true;
        }
    }
}
