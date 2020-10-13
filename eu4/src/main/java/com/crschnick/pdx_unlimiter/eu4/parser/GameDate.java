package com.crschnick.pdx_unlimiter.eu4.parser;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class GameDate implements Comparable<GameDate> {

    private static int getDay(Month m, long days) {
        int sum = 0;
        for (Month month : Month.values()) {
            if (month == m) {
                return (int) ((days - sum) + 1);
            } else {
                sum += month.length(false);
            }
        }
        return -1;
    }

    private static Month getMonth(long days) {
        int sum = 0;
        for (Month m : Month.values()) {
            if (days <= sum + m.length(false)) {
                return m;
            } else {
                sum += m.length(false);
            }
        }
        return null;
    }

    private static int getYear(long days) {
        return (int) Math.floor(days / 365f);
    }


    public static Node toNode(GameDate date) {
        List<Node> nodes = new ArrayList<>(3);
        nodes.add(KeyValueNode.create("day", new ValueNode<Long>((long) date.getDay())));
        nodes.add(KeyValueNode.create("month", new ValueNode<Long>((long) date.getMonth().getValue())));
        nodes.add(KeyValueNode.create("year", new ValueNode<Long>((long) date.getYear())));
        return new ArrayNode(nodes);
    }

    public static GameDate fromNode(Node node) {
        int day = Node.getInteger(Node.getNodeForKey(node, "day"));
        int month = Node.getInteger(Node.getNodeForKey(node, "month"));
        int year = Node.getInteger(Node.getNodeForKey(node, "year"));
        return new GameDate(day, Month.of(month), year);
    }

    public static GameDate fromLong(long date) {
        date /= 24;
        date -= (5000 * 365);
        int year = getYear(date);
        date -= (year * 365);
        Month m = getMonth(date);
        int day = getDay(m, date);
        return new GameDate(day, m, year);
    }

    public static long toLong(GameDate date) {
        long v = 0;
        for (Month m : Month.values()) {
            if (m.equals(date.getMonth())) {
                break;
            }
            v += m.length(false);
        }
        v += (date.getDay() - 1);
        v += date.getYear() * 365;
        v += (5000 * 365);
        v *= 24;
        return v;
    }

    public static GameDate fromString(String s) {
        if (Pattern.matches("-?\\d+\\.\\d+\\.\\d+", s)) {
            String[] split = s.split("\\.");
            int year = Integer.parseInt(split[0]);
            Month m = Month.of(Integer.parseInt(split[1]));
            int day = Integer.parseInt(split[2]);
            return new GameDate(day, m, year);
        } else {
            return null;
        }
    }

    private int day;

    private Month month;

    private int year;

    public GameDate(int day, Month month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public String toDisplayString() {
        return day + " " + month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ", " + year;
    }

    @Override
    public String toString() {
        return year + "." + month.getValue() + "." + day;
    }

    public int getDay() {
        return day;
    }

    public Month getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public int compareTo(GameDate o) {
        return (int) (toLong(this) - toLong(o));
    }
}
