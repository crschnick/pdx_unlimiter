package com.crschnick.pdx_unlimiter.core.data;


import java.util.Comparator;

public class GameVersion implements Comparable<GameVersion> {

    private int first;
    private int second;
    private int third;
    private int fourth;
    private String name;

    public GameVersion(int first, int second, int third, int fourth, String name) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.name = name;
    }

    @Override
    public String toString() {
        return first + "." + second + "." + third + (fourth != 0 ? "." + fourth : "") + (name != null ? " " + name : "");
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public int getThird() {
        return third;
    }

    public int getFourth() {
        return fourth;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(GameVersion o) {
        return (first - o.first) != 0 ? (first - o.first) :
                (second - o.second) != 0 ? (second - o.second) :
                        (third - o.third) != 0 ? (third - o.third) :
                                (fourth - o.fourth);
    }
}
