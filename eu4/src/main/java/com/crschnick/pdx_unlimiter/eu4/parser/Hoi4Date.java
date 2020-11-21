package com.crschnick.pdx_unlimiter.eu4.parser;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Hoi4Date implements Comparable<Hoi4Date> {

    private int hour;
    private int day;
    private Month month;
    private int year;

    public Hoi4Date(int hour, int day, Month month, int year) {
        this.hour = hour;
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public static Node toNode(Hoi4Date date) {
        List<Node> nodes = new ArrayList<>(3);
        nodes.add(KeyValueNode.create("hour", new ValueNode((long) date.getDay())));
        nodes.add(KeyValueNode.create("day", new ValueNode((long) date.getDay())));
        nodes.add(KeyValueNode.create("month", new ValueNode((long) date.getMonth().getValue())));
        nodes.add(KeyValueNode.create("year", new ValueNode((long) date.getYear())));
        nodes.add(KeyValueNode.create("hours_since_beginning", new ValueNode(toDaysSinceBeginning(date))));
        return new ArrayNode(nodes);
    }

    public static Hoi4Date fromNode(Node node) {
        int hour = Node.getInteger(Node.getNodeForKey(node, "hour"));
        int day = Node.getInteger(Node.getNodeForKey(node, "day"));
        int month = Node.getInteger(Node.getNodeForKey(node, "month"));
        int year = Node.getInteger(Node.getNodeForKey(node, "year"));
        return new Hoi4Date(hour, day, Month.of(month), year);
    }

    private static long toDaysSinceBeginning(Hoi4Date date) {
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

    public static Hoi4Date fromString(String s) {
        if (Pattern.matches("-?\\d+\\.\\d+\\.\\d+\\.\\d+", s)) {
            String[] split = s.split("\\.");
            int year = Integer.parseInt(split[0]);
            Month m = Month.of(Integer.parseInt(split[1]));
            int day = Integer.parseInt(split[2]);
            int hour = Integer.parseInt(split[3]);
            return new Hoi4Date(hour, day, m, year);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return year + "." + month.getValue() + "." + day + "." + hour;
    }

    public int getHour() {
        return hour;
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
    public int compareTo(Hoi4Date o) {
        return (int) (toDaysSinceBeginning(this) - toDaysSinceBeginning(o));
    }
}
