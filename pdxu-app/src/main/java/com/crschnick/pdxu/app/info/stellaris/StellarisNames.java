package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.model.stellaris.StellarisName;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StellarisNames {

    public static String getCountryName(SavegameData<?> data, StellarisTag tag) {
        if (tag.getNameData() == null) {
            return tag.getName();
        }

        var name = resolve(data, tag.getNameData());
        return name.isBlank() ? tag.getName() : name;
    }

    public static String resolve(SavegameData<?> data, StellarisName name) {
        if (name.isLiteral()) {
            return name.getKey();
        }

        var vars = new LinkedHashMap<String, String>();
        if (name.getVariables() != null) {
            for (var v : name.getVariables()) {
                vars.put(v.getKey(), resolve(data, v.getValue()));
            }
        }

        var key = name.getKey();
        if (key.equals("%ADJECTIVE%")) {
            return join(Stream.of(vars.get("adjective"), vars.get("1")));
        }

        if (key.startsWith("%") && key.endsWith("%")) {
            return join(vars.values().stream());
        }

        var text = GameLocalisation.getLocalisedValue(key, data);
        var replaced = false;
        for (var e : vars.entrySet()) {
            for (var placeholder : new String[] {"$" + e.getKey() + "$", "[" + e.getKey() + "]"}) {
                if (text.contains(placeholder)) {
                    text = text.replace(placeholder, e.getValue());
                    replaced = true;
                }
            }
        }

        if (!replaced && !vars.isEmpty()) {
            text = join(Stream.concat(Stream.of(text), vars.values().stream()));
        }
        return text.trim();
    }

    private static String join(Stream<String> parts) {
        return parts.filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(" "));
    }
}
