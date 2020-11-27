package com.crschnick.pdx_unlimiter.eu4.data;


public class GameVersion {

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
}
