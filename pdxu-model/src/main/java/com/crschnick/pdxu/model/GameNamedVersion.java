package com.crschnick.pdxu.model;

import java.util.Objects;

public final class GameNamedVersion extends GameVersion {

    private String name;

    public GameNamedVersion() {
    }

    public GameNamedVersion(int first, int second, int third, int fourth, String name) {
        super(first, second, third, fourth);
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String toString() {
        return super.toString() + " " + name;
    }
}
