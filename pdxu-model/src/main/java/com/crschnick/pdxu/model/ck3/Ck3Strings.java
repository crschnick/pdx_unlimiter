package com.crschnick.pdxu.model.ck3;

import java.util.ArrayList;

public class Ck3Strings {

    private static final String METADATA_END = String.valueOf(new char[]{21, '!'});

    public static String cleanCk3FormatData(String title) {
        var validParts = new ArrayList<String>();
        for (var part : title.split(" ")) {
            if (part.length() == 0) {
                continue;
            }

            if (part.charAt(0) == 21) {
                continue;
            }

            validParts.add(part.replace(METADATA_END, ""));
        }
        return String.join(" ", validParts);
    }
}
