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
import java.util.regex.Pattern;

public class Eu4Date implements Comparable<Eu4Date> {

    private int day;
    private Month month;
    private int year;


    public Eu4Date(int day, Month month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

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

    public static Node toNode(Eu4Date date) {
        List<Node> nodes = new ArrayList<>(3);
        nodes.add(KeyValueNode.create("day", new ValueNode((long) date.getDay())));
        nodes.add(KeyValueNode.create("month", new ValueNode((long) date.getMonth().getValue())));
        nodes.add(KeyValueNode.create("year", new ValueNode((long) date.getYear())));
        nodes.add(KeyValueNode.create("days_since_beginning", new ValueNode(toDaysSinceBeginning(date))));
        return new ArrayNode(nodes);
    }

    public static Eu4Date fromNode(Node node) {
        int day = Node.getInteger(Node.getNodeForKey(node, "day"));
        int month = Node.getInteger(Node.getNodeForKey(node, "month"));
        int year = Node.getInteger(Node.getNodeForKey(node, "year"));
        return new Eu4Date(day, Month.of(month), year);
    }

    public static Eu4Date fromLong(long date) {
        date /= 24;
        date -= (5000 * 365);
        int year = getYear(date);
        date -= (year * 365);
        Month m = getMonth(date);
        int day = getDay(m, date);
        return new Eu4Date(day, m, year);
    }

    private static long toDaysSinceBeginning(Eu4Date date) {
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
        return v;
    }

    public static long toLong(Eu4Date date) {
        long v = toDaysSinceBeginning(date);
        v *= 24;
        return v;
    }

    public static Eu4Date fromString(String s) {
        if (Pattern.matches("-?\\d+\\.\\d+\\.\\d+", s)) {
            String[] split = s.split("\\.");
            int year = Integer.parseInt(split[0]);
            Month m = Month.of(Integer.parseInt(split[1]));
            int day = Integer.parseInt(split[2]);
            return new Eu4Date(day, m, year);
        } else {
            return null;
        }
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
    public int compareTo(Eu4Date o) {
        return (int) (toLong(this) - toLong(o));
    }
}
