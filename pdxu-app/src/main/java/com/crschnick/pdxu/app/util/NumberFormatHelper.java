package com.crschnick.pdxu.app.util;

import java.util.Locale;

public class NumberFormatHelper {

    public static String formatCompact(double value) {
        var abs = Math.abs(value);
        if (abs < 999.5) {
            return String.valueOf(Math.round(value));
        }
        if (abs < 999_500) {
            return Math.round(value / 1_000) + "k";
        }
        if (abs < 999_950_000) {
            return formatDecimal(value / 1_000_000) + "M";
        }
        return formatDecimal(value / 1_000_000_000) + "B";
    }

    public static String formatCompactSigned(double value) {
        return (value >= 0 ? "+" : "") + formatCompact(value);
    }

    public static String formatDecimal(double value) {
        var s = String.format(Locale.ROOT, "%.1f", value);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
}
