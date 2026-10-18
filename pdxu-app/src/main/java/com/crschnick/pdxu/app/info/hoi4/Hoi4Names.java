package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameLocalisation;

import java.util.ArrayList;
import java.util.List;

public class Hoi4Names {

    public static String getCountryName(SavegameData<?> data, String tag, String ideology, String cosmeticTag) {
        var candidates = new ArrayList<String>();
        for (var base : cosmeticTag != null ? List.of(cosmeticTag, tag) : List.of(tag)) {
            if (ideology != null) {
                candidates.add(base + "_" + ideology);
            }
            candidates.add(base);
        }

        for (var key : candidates) {
            var name = GameLocalisation.getLocalisedValue(key, data);
            if (!name.equals(key)) {
                return name;
            }
        }
        return tag;
    }
}
