package com.crschnick.pdx_unlimiter.converters;

import java.util.Map;

public abstract class Converter {

    private String fromGame;
    private String toGame;
    private String id;

    public abstract void removeEntries(Map<String,String> map);

    public abstract void addEntries(Map<String,String> map);
}
