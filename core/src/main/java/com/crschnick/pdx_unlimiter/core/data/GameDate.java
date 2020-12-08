package com.crschnick.pdx_unlimiter.core.data;

public final class GameDate implements Comparable<GameDate> {

    private int hour;
    private int day;
    private int month;
    private int year;
    private GameDateType type;

    public GameDate(int hour, int day, int month, int year, GameDateType type) {
        this.hour = hour;
        this.day = day;
        this.month = month;
        this.year = year;
        this.type = type;
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

    public String toDisplayString() {
        return type.toLongString(this);
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
