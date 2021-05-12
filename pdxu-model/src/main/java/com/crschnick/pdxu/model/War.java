package com.crschnick.pdxu.model;

import java.util.List;

public class War<T> {

    private String title;
    private List<T> attackers;
    private List<T> defenders;

    public War() {
    }

    public War(String title, List<T> attackers, List<T> defenders) {
        this.title = title;
        this.attackers = attackers;
        this.defenders = defenders;
    }

    public boolean isAttacker(T tag) {
        return attackers.contains(tag);
    }

    public String getTitle() {
        return title;
    }

    public List<T> getAttackers() {
        return attackers;
    }

    public List<T> getDefenders() {
        return defenders;
    }
}
