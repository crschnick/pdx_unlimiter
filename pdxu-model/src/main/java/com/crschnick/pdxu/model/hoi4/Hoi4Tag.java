package com.crschnick.pdxu.model.hoi4;

import java.util.Objects;

public class Hoi4Tag {

    private String tag;
    private String ideology;

    public Hoi4Tag() {
    }

    public Hoi4Tag(String tag, String ideology) {
        this.tag = tag;
        this.ideology = ideology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hoi4Tag hoi4Tag = (Hoi4Tag) o;
        return Objects.equals(tag, hoi4Tag.tag) &&
                Objects.equals(ideology, hoi4Tag.ideology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, ideology);
    }

    public String getTag() {
        return tag;
    }

    public String getIdeology() {
        return ideology;
    }
}
