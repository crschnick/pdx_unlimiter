package com.crschnick.pdx_unlimiter.core.info.ck3;

public class Ck3House {
    private String name;
    private Ck3CoatOfArms coa;

    public Ck3House() {
    }

    public Ck3House(String name, Ck3CoatOfArms coa) {
        this.name = name;
        this.coa = coa;
    }

    public String getName() {
        return name;
    }

    public Ck3CoatOfArms getCoatOfArms() {
        return coa;
    }
}
