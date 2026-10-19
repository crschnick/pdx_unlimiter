package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;

import java.util.LinkedHashMap;

public class Vic3Names {

    public static String resolve(SavegameData<?> data, Node name) {
        if (name == null) {
            return null;
        }

        if (name.isValue()) {
            return GameLocalisation.getLocalisedValue(name.getString(), data);
        }

        if (!name.isArray()) {
            return null;
        }

        var key = name.getNodeForKeyIfExistent("key")
                .filter(Node::isValue)
                .map(Node::getString)
                .orElse(null);
        if (key == null) {
            return null;
        }

        var vars = new LinkedHashMap<String, String>();
        name.getNodeForKeyIfExistent("variables").filter(Node::isArray).ifPresent(variables -> {
            for (var v : variables.getNodeArray()) {
                if (!v.isArray()) {
                    continue;
                }

                var varKey = v.getNodeForKeyIfExistent("key").filter(Node::isValue).map(Node::getString);
                var value = v.getNodeForKeyIfExistent("value").map(n -> resolve(data, n));
                if (varKey.isPresent() && value.isPresent() && value.get() != null) {
                    vars.put(varKey.get(), value.get());
                }
            }
        });

        var text = GameLocalisation.getLocalisedValue(key, data);
        if (text.equals(key)) {
            if (vars.containsKey("NAME")) {
                return vars.get("NAME");
            }
            return vars.isEmpty() ? key : String.join(" ", vars.values());
        }

        for (var e : vars.entrySet()) {
            text = text.replace("$" + e.getKey() + "$", e.getValue());
        }
        return text.trim();
    }

    public static String getCountryName(SavegameData<?> data, Node country, String definition) {
        var dynamicName = country != null
                ? country.getNodeForKeyIfExistent("dynamic_name")
                        .flatMap(n -> n.getNodeForKeyIfExistent("dynamic_country_name"))
                        .filter(Node::isValue)
                        .map(Node::getString)
                        .orElse(null)
                : null;
        if (dynamicName != null) {
            var localised = GameLocalisation.getLocalisedValue(dynamicName, data);
            if (!localised.equals(dynamicName)) {
                return localised;
            }
        }
        return GameLocalisation.getLocalisedValue(definition, data);
    }
}
