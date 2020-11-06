package com.crschnick.pdx_unlimiter.eu4.parser;


public class GameVersion {

    private int first;
    private int second;
    private int third;
    private int fourth;

    public GameVersion(int first, int second, int third, int fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @Override
    public String toString() {
        return first + "." + second + "." + third + "." + fourth;
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
}
