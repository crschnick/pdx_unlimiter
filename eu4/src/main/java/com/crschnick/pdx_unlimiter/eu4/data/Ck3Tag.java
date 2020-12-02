package com.crschnick.pdx_unlimiter.eu4.data;

import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Ck3Tag {

    private Person ruler;
    private List<Title> titles;

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

                    Ck3Tag tag = new Ck3Tag();
                    tag.titles = Node.getNodeArray(domain).stream()
                            .map(id -> titles.stream().filter(t -> t.id == Node.getInteger(id)).findFirst().get())
                            .collect(Collectors.toList());
                    tag.ruler = Person.fromNode(n);
                    return tag;
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

            Title t = new Title();
            t.id = Integer.parseInt(kvn.getKeyName());
            t.name = Node.getString(Node.getNodeForKey(n, "name"));
            var coaId = Node.getInteger(Node.getNodeForKey(n, "coat_of_arms_id"));
            t.coatOfArms = coas.stream().filter(c -> c.id == coaId).findFirst().get();
            return t;
        }

        private Title() {}

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
        private String[] colors;

        private String emblemFile;
        private String[] emblemColors;

        public static CoatOfArms fromNode(Node kv) {
            var kvn = Node.getKeyValueNode(kv);
            var n = kvn.getNode();

            CoatOfArms c = new CoatOfArms();
            c.id = Integer.parseInt(kvn.getKeyName());
            c.patternFile = Node.getNodeForKeyIfExistent(n, "pattern").map(Node::getString).orElse(null);

            c.colors = new String[4];
            c.colors[0] = Node.getNodeForKeyIfExistent(n, "color1").map(Node::getString).orElse(null);
            c.colors[1] = Node.getNodeForKeyIfExistent(n, "color2").map(Node::getString).orElse(null);
            c.colors[2] = Node.getNodeForKeyIfExistent(n, "color3").map(Node::getString).orElse(null);
            c.colors[3] = Node.getNodeForKeyIfExistent(n, "color4").map(Node::getString).orElse(null);

            c.emblemColors = new String[2];
            boolean isColoredEmbled = Node.hasKey(n, "colored_emblem");
            if (isColoredEmbled) {
                var e = Node.getNodesForKey(n, "colored_emblem").get(0);
                c.emblemFile = Node.getString(Node.getNodeForKey(e, "texture"));
                c.emblemColors[0] = Node.getString(Node.getNodeForKey(e, "color1"));
                c.emblemColors[1] = Node.getNodeForKeyIfExistent(e, "color2").map(Node::getString).orElse(null);
            } else if (Node.hasKey(n, "textured_emblem")) {
                c.emblemFile = Node.getString(Node.getNodeForKey(
                        Node.getNodeForKey(n, "textured_emblem"), "texture"));
            }
            return c;
        }

        private CoatOfArms() {}

        public String getPatternFile() {
            return patternFile;
        }

        public String[] getColors() {
            return colors;
        }

        public String getEmblemFile() {
            return emblemFile;
        }

        public String[] getEmblemColors() {
            return emblemColors;
        }
    }
}
