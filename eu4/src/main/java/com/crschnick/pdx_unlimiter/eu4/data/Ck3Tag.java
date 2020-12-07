package com.crschnick.pdx_unlimiter.eu4.data;

import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Ck3Tag {

    private Person ruler;
    private List<Title> titles;

    public Ck3Tag(Person ruler, List<Title> titles) {
        this.ruler = ruler;
        this.titles = titles;
    }

    public static Ck3Tag getPlayerTag(Node gamestate, Set<Ck3Tag> tags) {
        return tags.stream().filter(t -> t.ruler.id == Node.getInteger(Node.getNodeForKey(
                Node.getNodeForKey(gamestate, "played_character"), "character")))
                .findFirst().get();
    }

    public static Set<Ck3Tag> fromNode(Node living, Node landedTitles, Node coatOfArms) {
        var coas = Node.getNodeArray(
                Node.getNodeForKey(coatOfArms, "coat_of_arms_manager_database")).stream()
                .map(CoatOfArms::fromNode)
                .collect(Collectors.toList());

        var titles = Node.getNodeArray(
                Node.getNodeForKey(landedTitles, "landed_titles")).stream()
                .map(n -> Title.fromNode(n, coas))
                .collect(Collectors.toList());

        var tags = Node.getNodeArray(living).stream()
                .filter(n -> Node.hasKey(Node.getKeyValueNode(n).getNode(), "landed_data"))
                .map(n -> {
                    var domain = Node.getNodeForKey(
                            Node.getNodeForKey(Node.getKeyValueNode(n).getNode(), "landed_data"), "domain");

                    var tagTitles = Node.getNodeArray(domain).stream()
                            .map(id -> titles.stream().filter(t -> t.id == Node.getInteger(id)).findFirst().get())
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
            var kvn = Node.getKeyValueNode(kv);
            var n = kvn.getNode();

            Person p = new Person();
            p.id = Integer.parseInt(kvn.getKeyName());
            p.firstName = Node.getString(Node.getNodeForKey(n, "first_name"));
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

        public static Title fromNode(Node kv, List<CoatOfArms> coas) {
            var kvn = Node.getKeyValueNode(kv);
            var n = kvn.getNode();

            var id = Integer.parseInt(kvn.getKeyName());
            var name = Node.getString(Node.getNodeForKey(n, "name"));
            var coaId = Node.getInteger(Node.getNodeForKey(n, "coat_of_arms_id"));
            var coatOfArms = coas.stream().filter(c -> c.id == coaId).findFirst().get();
            return new Title(id, name, coatOfArms);
        }

        public Title(int id, String name, CoatOfArms coatOfArms) {
            this.id = id;
            this.name = name;
            this.coatOfArms = coatOfArms;
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

        public static CoatOfArms fromNode(Node kv) {
            var kvn = Node.getKeyValueNode(kv);
            var n = kvn.getNode();

            var id = Integer.parseInt(kvn.getKeyName());
            var patternFile = Node.getNodeForKeyIfExistent(n, "pattern").map(Node::getString).orElse(null);

            List<String> colors = new ArrayList<>();
            Node.getNodeForKeyIfExistent(n, "color1").map(Node::getString).ifPresent(colors::add);
            Node.getNodeForKeyIfExistent(n, "color2").map(Node::getString).ifPresent(colors::add);
            Node.getNodeForKeyIfExistent(n, "color3").map(Node::getString).ifPresent(colors::add);
            Node.getNodeForKeyIfExistent(n, "color4").map(Node::getString).ifPresent(colors::add);

            List<String> emblemColors = new ArrayList<>();
            String emblemFile = null;
            boolean isColoredEmblem = Node.hasKey(n, "colored_emblem");
            if (isColoredEmblem) {
                var e = Node.getNodesForKey(n, "colored_emblem").get(0);
                emblemFile = Node.getString(Node.getNodeForKey(e, "texture"));
                emblemColors.add(Node.getString(Node.getNodeForKey(e, "color1")));
                Node.getNodeForKeyIfExistent(e, "color2").map(Node::getString).ifPresent(emblemColors::add);
            } else if (Node.hasKey(n, "textured_emblem")) {
                emblemFile = Node.getString(Node.getNodeForKey(
                        Node.getNodeForKey(n, "textured_emblem"), "texture"));
            }
            return new CoatOfArms(id, patternFile, colors, emblemFile, emblemColors);
        }

        public CoatOfArms(int id, String patternFile, List<String> colors, String emblemFile, List<String> emblemColors) {
            this.id = id;
            this.patternFile = patternFile;
            this.colors = colors;
            this.emblemFile = emblemFile;
            this.emblemColors = emblemColors;
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
