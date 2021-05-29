package com.crschnick.pdxu.model.ck2;

public class Ck2Tag {

    private String primaryTitle;
    private String rulerName;

    public Ck2Tag() {
    }

    public Ck2Tag(String primaryTitle, String rulerName) {
        this.primaryTitle = primaryTitle;
        this.rulerName = rulerName;
    }

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public String getRulerName() {
        return rulerName;
    }
}
