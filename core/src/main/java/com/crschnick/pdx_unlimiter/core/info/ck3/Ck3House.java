package com.crschnick.pdx_unlimiter.core.info.ck3;

import java.util.Objects;

public class Ck3House {

    private String name;
    private Ck3CoatOfArms coa;

    public Ck3House() {
    }

    public Ck3House(String name, Ck3CoatOfArms coa) {
        this.name = name;
        this.coa = coa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ck3House ck3House = (Ck3House) o;
        return name.equals(ck3House.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public Ck3CoatOfArms getCoatOfArms() {
        return coa;
    }
}
