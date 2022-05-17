package com.crschnick.pdxu.model;

import java.util.Locale;
import java.util.stream.IntStream;

public final class GameDate implements Comparable<GameDate> {

    public static GameDate zero(GameDateType t) {
        return new GameDate(0, 0, 0, 0, t);
    }

    private int hour;
    private int day;
    private int month;
    private int year;
    private GameDateType type;

    public GameDate() {
    }

    public GameDate(int hour, int day, int month, int year, GameDateType type) {
        this.hour = hour;
        this.day = day;
        this.month = month;
        this.year = year;
        this.type = type;
    }

    public static int yearsBetween(GameDate start, GameDate end) {
        int yearLength = IntStream.range(1, 13).map(start.type::getDaysInMonth).sum();
        var time = end.toLong() - start.toLong();
        return (int) (time / 24 / yearLength);
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return type.toShortString(this);
    }

    public String toDisplayString(Locale l) {
        return type.toLongString(l, this);
    }

    public long toLong() {
        return type.toHoursSinceBeginning(this);
    }

    @Override
    public int compareTo(GameDate o) {
        if (!type.equals(o.type)) {
            throw new IllegalArgumentException();
        }

        return (int) (type.toHoursSinceBeginning(this) - o.type.toHoursSinceBeginning(o));
    }
}
