package com.crschnick.pdx_unlimiter.core.info;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.regex.Pattern;

@JsonDeserialize(using = GameDateType.Deserializer.class)
public abstract class GameDateType {

    public static final GameDateType EU4 = new GameDateType() {

        @Override
        @JsonValue
        public String name() {
            return "eu4";
        }

        @Override
        public boolean hasHours() {
            return false;
        }

        @Override
        public int getDaysInMonth(int month) {
            return Month.of(month).length(false);
        }

        @Override
        public String toShortString(GameDate date) {
            return date.getYear() + "." + date.getMonth() + "." + date.getDay();
        }

        @Override
        public String toLongString(Locale l, GameDate date) {
            return date.getDay() + " " + Month.of(date.getMonth()).getDisplayName(TextStyle.FULL, l) + ", " + date.getYear();
        }
    };
    public static final GameDateType CK3 = EU4;
    public static final GameDateType STELLARIS = new GameDateType() {
        @Override
        @JsonValue
        public String name() {
            return "stellaris";
        }

        @Override
        public boolean hasHours() {
            return false;
        }

        @Override
        public int getDaysInMonth(int month) {
            return 30;
        }

        @Override
        public String toShortString(GameDate date) {
            return date.getYear() + "." + date.getMonth() + "." + date.getDay();
        }

        @Override
        public String toLongString(Locale l, GameDate date) {
            return date.getDay() + " " + Month.of(date.getMonth()).getDisplayName(TextStyle.FULL, l) + ", " + date.getYear();
        }
    };
    public static final GameDateType HOI4 = new GameDateType() {
        @Override
        @JsonValue
        public String name() {
            return "hoi4";
        }

        @Override
        public boolean hasHours() {
            return true;
        }

        @Override
        public int getDaysInMonth(int month) {
            return Month.of(month).length(false);
        }

        @Override
        public String toShortString(GameDate date) {
            return date.getYear() + "." + date.getMonth() + "." + date.getDay() + "." + date.getHour();
        }

        @Override
        public String toLongString(Locale l, GameDate date) {
            return date.getHour() + ":00, " + date.getDay() + " " +
                    Month.of(date.getMonth()).getDisplayName(TextStyle.FULL, l) + ", " + date.getYear();
        }
    };

    public abstract String name();

    public abstract boolean hasHours();

    public abstract int getDaysInMonth(int month);

    public abstract String toShortString(GameDate date);

    public abstract String toLongString(Locale l, GameDate date);

    private int getDay(int m, long days) {
        int sum = 0;
        for (int i = 1; i <= 12; i++) {
            if (i == m) {
                return (int) ((days - sum) + 1);
            } else {
                sum += getDaysInMonth(i);
            }
        }
        return -1;
    }

    private int getMonth(long days) {
        int sum = 0;
        for (int i = 1; i <= 12; i++) {
            if (days <= sum + getDaysInMonth(i)) {
                return i;
            } else {
                sum += getDaysInMonth(i);
            }
        }
        throw new IllegalArgumentException();
    }

    private int getYear(long days) {
        return (int) Math.floor(days / 365f);
    }

    public long toHoursSinceBeginning(GameDate date) {
        long v = 0;
        for (int i = 1; i <= 12; i++) {
            if (i == date.getMonth()) {
                break;
            }
            v += getDaysInMonth(i);
        }
        v += (date.getDay() - 1);
        v += date.getYear() * 365;
        v += (5000 * 365);
        v *= 24;
        v += date.getHour();
        return v;
    }

    public GameDate fromLong(long date) {
        int hour = (int) (date % 24);
        date -= hour;
        date /= 24;
        date -= (5000 * 365);
        int year = getYear(date);
        date -= (year * 365);
        int m = getMonth(date);
        int day = getDay(m, date);
        return new GameDate(hasHours() ? hour + 1 : 0, day, m, year, this);
    }

    public boolean isDate(String s) {
        if (hasHours() && Pattern.matches("-?\\d+\\.\\d+\\.\\d+\\.\\d+", s)) {
            return true;
        } else return !hasHours() && Pattern.matches("-?\\d+\\.\\d+\\.\\d+", s);
    }

    public GameDate fromString(String s) {
        if (hasHours() && Pattern.matches("-?\\d+\\.\\d+\\.\\d+\\.\\d+", s)) {
            String[] split = s.split("\\.");
            int year = Integer.parseInt(split[0]);
            int m = Integer.parseInt(split[1]);
            int day = Integer.parseInt(split[2]);
            int hour = Integer.parseInt(split[3]);
            return new GameDate(hour, day, m, year, this);
        } else if (!hasHours() && Pattern.matches("-?\\d+\\.\\d+\\.\\d+", s)) {
            String[] split = s.split("\\.");
            int year = Integer.parseInt(split[0]);
            int m = Integer.parseInt(split[1]);
            int day = Integer.parseInt(split[2]);
            return new GameDate(0, day, m, year, this);
        }
        throw new IllegalArgumentException("Invalid date string: " + s);
    }

    public static class Deserializer extends StdDeserializer<GameDateType> {

        public Deserializer() {
            super(GameDateType.class);
        }

        @Override
        public GameDateType deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            if (node.textValue().equals("eu4")) {
                return EU4;
            }
            if (node.textValue().equals("hoi4")) {
                return HOI4;
            }
            if (node.textValue().equals("stellaris")) {
                return STELLARIS;
            }
            throw new IllegalArgumentException();
        }
    }
}
