package com.crschnick.pdxu.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = GameNamedVersion.class)
        }
)
public class GameVersion implements Comparable<GameVersion> {

    private int first;
    private int second;
    private int third;
    private int fourth;

    public GameVersion() {
    }

    public GameVersion(int first, int second, int third, int fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @Override
    public String toString() {
        return first + "." + second + "." + third + (fourth != 0 ? "." + fourth : "");
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

    @Override
    public int compareTo(GameVersion o) {
        return (first - o.first) != 0 ? (first - o.first) :
                (second - o.second) != 0 ? (second - o.second) :
                        (third - o.third) != 0 ? (third - o.third) :
                                (fourth - o.fourth);
    }
}
