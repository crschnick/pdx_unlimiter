package com.crschnick.pdx_unlimiter.eu4.data;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class GameDateType {

    public static final GameDateType EU4 = new GameDateType() {
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
        public String toLongString(GameDate date) {
            return date.getDay() + " " + Month.of(date.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + date.getYear();
        }
    };

    public static final GameDateType CK3 = EU4;

    public static final GameDateType STELLARIS = new GameDateType() {
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
        public String toLongString(GameDate date) {
            return date.getDay() + " " + Month.of(date.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + date.getYear();
        }
    };

    public static final GameDateType HOI4 = new GameDateType() {
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
        public String toLongString(GameDate date) {
            return date.getHour() + ":00, " + date.getDay() + " " +
                    Month.of(date.getMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + date.getYear();
        }
    };

    public abstract boolean hasHours();
    public abstract int getDaysInMonth(int month);
    public abstract String toShortString(GameDate date);
    public abstract String toLongString(GameDate date);

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

    public Node toNode(GameDate date) {
        List<Node> nodes = new ArrayList<>();
        if (hasHours()) {
            nodes.add(KeyValueNode.create("hour", new ValueNode((long) date.getHour())));
        }
        nodes.add(KeyValueNode.create("day", new ValueNode((long) date.getDay())));
        nodes.add(KeyValueNode.create("month", new ValueNode((long) date.getMonth())));
        nodes.add(KeyValueNode.create("year", new ValueNode((long) date.getYear())));
        if (hasHours()) {
            nodes.add(KeyValueNode.create("hours_since_beginning", new ValueNode(toHoursSinceBeginning(date))));
        } else {
            nodes.add(KeyValueNode.create("days_since_beginning", new ValueNode(toHoursSinceBeginning(date) / 24)));
        }
        return new ArrayNode(nodes);
    }

    public GameDate fromNode(Node node) {
        int hours = 0;
        if (hasHours()) {
hours = Node.getInteger(Node.getNodeForKey(node, "hour"));
        }
        int day = Node.getInteger(Node.getNodeForKey(node, "day"));
        int month = Node.getInteger(Node.getNodeForKey(node, "month"));
        int year = Node.getInteger(Node.getNodeForKey(node, "year"));
        return new GameDate(hours, day, month, year, this);
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
}
