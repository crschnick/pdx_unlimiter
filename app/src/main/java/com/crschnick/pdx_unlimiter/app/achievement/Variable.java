package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Variable {

    public static List<Variable> defaultVariables() {
        var list = new ArrayList<Variable>();
        list.add(new ValueVariable("eu4.installDir", GameInstallation.EU4.getPath().toString()));

        list.add(new ValueVariable("condition.no_custom_nation", "$[*][?(@.is_custom == true)]"));
        list.add(new ValueVariable("condition.normal_start_date", "$.start_date.days_since_beginning == 2352374"));
        list.add(new ValueVariable("condition.normal_province_values", "$.gameplaysettings.setgameplayoptions.province_values == 'normal'"));
        list.add(new ValueVariable("condition.historical_lucky_nations", "$.gameplaysettings.setgameplayoptions.lucky_nations == 'historical'"));
        list.add(new ValueVariable("condition.ironman", "$.ironman == true"));

        list.add(new ValueVariable("path.player", "$.player"));
        list.add(new ValueVariable("path.difficulty", "$.gameplaysettings.setgameplayoptions.difficulty"));
        return list;
    }

    public static Variable fromNode(String name, JsonNode json) {
        String type = json.get("type").textValue();
        if (type.equals("pathValue")) {
            return new PathVariable(name, json.get("node").textValue(), json.get("path").textValue());
        } else {
            throw new IllegalArgumentException("Invalid variable type: " + type);
        }
    }

    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public abstract String evaluate(Eu4IntermediateSavegame sg);

    public String getName() {
        return name;
    }

    public static class ValueVariable extends Variable {

        private String value;

        public ValueVariable(String name, String value) {
            super(name);
            this.value = value;
        }

        @Override
        public String evaluate(Eu4IntermediateSavegame sg) {
            return value;
        }
    }

    public static class PathVariable extends Variable {

        private String node;
        private String path;

        public PathVariable(String name, String node, String path) {
            super(name);
            this.node = node;
            this.path = path;
        }

        @Override
        public String evaluate(Eu4IntermediateSavegame sg) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), path);
            if (r.getNodes().size() > 1) {
                throw new JsonPathException();
            }

            Object value = ((ValueNode) r.getNodes().get(0)).getValue();
            return value.toString();
        }
    }
}
