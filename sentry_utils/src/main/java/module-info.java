module com.crschnick.pdx_unlimiter.sentry_utils {
    uses com.crschnick.pdx_unlimiter.sentry_utils.ErrorHandler;
    exports com.crschnick.pdx_unlimiter.sentry_utils;

    requires transitive io.sentry;
}