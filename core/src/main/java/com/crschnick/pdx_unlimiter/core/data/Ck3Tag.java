package com.crschnick.pdx_unlimiter.core.data;

import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Ck3Tag {

    private Person ruler;
    private List<Title> titles;

    public Ck3Tag(Person ruler, List<Title> titles) {
        this.ruler = ruler;
        this.titles = titles;
    }

    public static Ck3Tag getPlayerTag(Node n, Set<Ck3Tag> tags) {
        return tags.stream().filter(t ->
                t.ruler.id == n.getNodeForKey("played_character").getNodeForKey("character").getInteger())
                .findFirst().get();
    }

    public static Set<Ck3Tag> fromNode(Node living, Node landedTitles, Node coatOfArms) {
        var coas = coatOfArms.getNodeForKey("coat_of_arms_manager_database").getNodeArray().stream()
                .map(CoatOfArms::fromNode)
                .collect(Collectors.toList());

        var titles = landedTitles.getNodeForKey("landed_titles").getNodeArray().stream()
                .map(n -> Title.fromNode(n, coas))
                .collect(Collectors.toList());

        Map<Integer,Title> titleIds = titles.stream().collect(Collectors.toMap(t -> t.id, t -> t));
        var tags = living.getNodeArray().stream()
                .filter(n -> n.getKeyValueNode().getNode().hasKey("landed_data"))
                .map(n -> {
                    var domain = n.getKeyValueNode().getNode().getNodeForKey("landed_data").getNodeForKey("domain");

                    var tagTitles = domain.getNodeArray().stream()
                            .map(id -> titleIds.get(id.getInteger()))
                            .collect(Collectors.toList());
                    var ruler = Person.fromNode(n);
                    return new Ck3Tag(ruler, tagTitles);
                })
                .collect(Collectors.toSet());

        return tags;
    }

    public Person getRuler() {
        return ruler;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public Title getPrimaryTitle() {
        return titles.get(0);
    }

    public static class Person {
        private int id;
        private String firstName;

        public static Person fromNode(Node kv) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();

            Person p = new Person();
            p.id = Integer.parseInt(kvn.getKeyName());
            p.firstName = n.getNodeForKey("first_name").getString();
            return p;
        }

        public int getId() {
            return id;
        }

        public String getFirstName() {
            return firstName;
        }
    }

    public static class Title {
        private int id;
        private String name;
        private CoatOfArms coatOfArms;

        public Title(int id, String name, CoatOfArms coatOfArms) {
            this.id = id;
            this.name = name;
            this.coatOfArms = coatOfArms;
        }

        public static Title fromNode(Node kv, List<CoatOfArms> coas) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();

            var id = Integer.parseInt(kvn.getKeyName());
            var name = n.getNodeForKey("name").getString();
            var coaId = n.getNodeForKey("coat_of_arms_id").getInteger();
            var coatOfArms = coas.stream().filter(c -> c.id == coaId).findFirst().get();
            return new Title(id, name, coatOfArms);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public CoatOfArms getCoatOfArms() {
            return coatOfArms;
        }
    }

    public static class CoatOfArms {
        private int id;

        private String patternFile;
        private List<String> colors;

        private String emblemFile;
        private List<String> emblemColors;

        public CoatOfArms(int id, String patternFile, List<String> colors, String emblemFile, List<String> emblemColors) {
            this.id = id;
            this.patternFile = patternFile;
            this.colors = colors;
            this.emblemFile = emblemFile;
            this.emblemColors = emblemColors;
        }

        public static CoatOfArms fromNode(Node kv) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();

            var id = Integer.parseInt(kvn.getKeyName());
            var patternFile = n.getNodeForKeyIfExistent("pattern").map(Node::getString).orElse(null);

            List<String> colors = new ArrayList<>();
            n.getNodeForKeyIfExistent("color1").map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color2").map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color3").map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color4").map(Node::getString).ifPresent(colors::add);

            List<String> emblemColors = new ArrayList<>();
            String emblemFile = null;
            boolean isColoredEmblem = n.hasKey("colored_emblem");
            if (isColoredEmblem) {
                var e = n.getNodesForKey("colored_emblem").get(0);
                emblemFile = e.getNodeForKey("texture").getString();
                emblemColors.add(e.getNodeForKey("color1").getString());
                e.getNodeForKeyIfExistent("color2").map(Node::getString).ifPresent(emblemColors::add);
            } else if (n.hasKey("textured_emblem")) {
                emblemFile = n.getNodeForKey("textured_emblem").getNodeForKey("texture").getString();
            }
            return new CoatOfArms(id, patternFile, colors, emblemFile, emblemColors);
        }

        public String getPatternFile() {
            return patternFile;
        }

        public List<String> getColors() {
            return colors;
        }

        public String getEmblemFile() {
            return emblemFile;
        }

        public List<String> getEmblemColors() {
            return emblemColors;
        }
    }
}
