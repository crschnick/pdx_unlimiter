package com.crschnick.pdx_unlimiter.core.data;


import java.util.Comparator;

public class GameVersion implements Comparator<GameVersion> {

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
    public int compare(GameVersion o1, GameVersion o2) {
        return (o1.first - o2.first) != 0 ? (o1.first - o2.first) :
                (o1.second - o2.second) != 0 ? (o1.second - o2.second) :
                        (o1.third - o2.third) != 0 ? (o1.third - o2.third) :
                                (o1.fourth - o2.fourth);
    }
}
