package com.paradox_challenges.eu4_unlimiter.parser;

import java.time.Month;
import java.util.regex.Pattern;

public class GameDate {

    private static int getDay(Month m, int days) {
        int sum = 0;
        for (Month month : Month.values()) {
            if (month == m) {
                return days - sum;
            } else {
                sum += month.length(false);
            }
        }
        return -1;
    }

    private static Month getMonth(int days) {
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

    private static int getYear(int days) {
        return (int) Math.floor(days / 365f);
    }

    public static GameDate fromInteger(int date) {
        date /= 24;
        date -= (5000 * 365);
        int year = getYear(date);
        date -= (year * 365);
        Month m = getMonth(date);
        int day = getDay(m, date);
        return new GameDate(day, m, year);
    }

    public static GameDate fromString(String s) {
        if (Pattern.matches("\\d\\d\\d\\d\\.\\d\\d?\\.\\d\\d?", s)) {
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

    @Override
    public String toString() {
        return day + "." + month.ordinal() + "." + year;
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
}
