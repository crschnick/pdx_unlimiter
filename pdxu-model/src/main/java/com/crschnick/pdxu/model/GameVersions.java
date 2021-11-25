package com.crschnick.pdxu.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameVersions {

    private static Pattern CK2 = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");

    /*
        "EU4 v1.32.1.0 Songhai (78cb)"
        "EU4 v1.31.0.0 Majapahit.d8820ba111 (0fb9)"
        "EU4 v1.30.3.0 Austria.e77eb54244 (8406)"
     */
    private static Pattern EU4 = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)(?:\\.\\w+\\s.+)?");

    private static Pattern STELLARIS = Pattern.compile("((?:\\w|\\s)+?)\\s*v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?");


    public static GameNamedVersion extensive(String vs) {
        Matcher m = p.matcher(vs);
        if (m.matches()) {
            return new GameNamedVersion(
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    m.groupCount() == 5 ? Integer.parseInt(m.group(4)) : 0, 0, m.group(1));
        } else {
            throw new IllegalArgumentException("Unable to parse version information for " + vs);
        }
    }
}
