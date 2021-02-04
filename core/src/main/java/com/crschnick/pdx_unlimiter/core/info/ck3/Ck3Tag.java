package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.util.*;
import java.util.stream.Collectors;

public class Ck3Tag {

    private Person ruler;
    private List<Title> titles;
    private List<Title> claims;

    public Ck3Tag() {
    }

    public Ck3Tag(Person ruler, List<Title> titles, List<Title> claims) {
        this.ruler = ruler;
        this.titles = titles;
        this.claims = claims;
    }

    public static Ck3Tag getPlayerTag(Node n, Set<Ck3Tag> tags) {
        var tag = tags.stream().filter(t ->
                t.ruler.id == n.getNodeForKey("played_character").getNodeForKey("character").getLong())
                .findFirst().get();
        return tag;
    }

    public static Set<Ck3Tag> fromNode(Node living, Node landedTitles, Node coatOfArms, Node dynasties) {
        var coas = coatOfArms.getNodeForKey("coat_of_arms_manager_database").getNodeArray().stream()
                .map(CoatOfArms::fromNode)
                .collect(Collectors.toList());

        var dyns = dynasties.getNodeForKey("dynasty_house").getNodeArray().stream()
                .map(n -> Dynasty.fromNode(n, coas, dynasties.getNodeForKey("dynasties")))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        var houses = dynasties.getNodeForKey("dynasty_house").getNodeArray().stream()
                .map(n -> House.fromNode(n, dyns, coas))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());


        var titles = landedTitles.getNodeForKey("landed_titles").getNodeArray().stream()
                .map(n -> Title.fromNode(n, coas))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        Map<Long, Title> titleIds = titles.stream().collect(Collectors.toMap(t -> t.id, t -> t));
        var tags = living.getNodeArray().stream()
                .filter(n -> n.getKeyValueNode().getNode().hasKey("landed_data"))
                .map(n -> {
                    var domain = n.getKeyValueNode().getNode().getNodeForKey("landed_data").getNodeForKey("domain");

                    var tagTitles = domain.getNodeArray().stream()
                            .map(id -> titleIds.get(id.getLong()))
                            .collect(Collectors.toList());
                    var tagClaims = new ArrayList<Title>();
                    var ruler = Person.fromNode(n, houses);
                    //var h = n.getKeyValueNode().getNode().getNodeForKey("alive_data").getNodeForKeyIfExistent("heir");
                    n.getKeyValueNode().getNode().getNodeForKey("alive_data")
                            .getNodeForKeyIfExistent("claim").ifPresent(claims -> {
                        tagClaims.addAll(claims.getNodeArray().stream().map(c ->
                                titleIds.get(c.getNodeForKey("title").getLong())).collect(Collectors.toList()));
                    });
                    return new Ck3Tag(ruler, tagTitles, tagClaims);
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

    public List<Title> getClaims() {
        return claims;
    }

    public Title getPrimaryTitle() {
        return titles.get(0);
    }

    public static class House {
        private long id;
        private Dynasty dynasty;
        private String name;
        private String prefix;
        private CoatOfArms coa;

        public static Optional<House> fromNode(Node kv, List<Dynasty> dynasties, List<CoatOfArms> coas) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();
            if (n.isValue() && n.getString().equals("none")) {
                return Optional.empty();
            }

            House h = new House();
            h.id = Long.parseLong(kvn.getKeyName());
            if (!n.hasKey("name") && !n.hasKey("key")) {
                return Optional.empty();
            }

            var dynId = n.getNodeForKey("dynasty").getLong();
            h.dynasty = dynasties.stream().filter(d -> d.id == dynId).findFirst().orElse(null);
            if (h.dynasty == null) {
                return Optional.empty();
            }

            // Entry is a dynasty and a house
            if (dynId == h.id) {
                h.name = h.dynasty.name;
                h.prefix = h.dynasty.prefix;
                h.coa = h.dynasty.coa;
                return Optional.of(h);
            }

            if (n.hasKey("name")) {
                h.name = n.getNodeForKey("name").getString()
                        .replace("dynn_", "")
                        .replace("_", " ");
                h.prefix = n.getNodeForKeyIfExistent("prefix").map(s -> s.getString()
                        .replace("dynnp_", "")
                        .replace("_", " ")).orElse(null);
            } else {
                h.name = n.getNodeForKey("key").getString()
                        .replace("house_", "")
                        .replace("_", " ");
            }

            if (n.hasKey("coat_of_arms_id")) {
                var coaId = n.getNodeForKey("coat_of_arms_id").getLong();
                h.coa = coas.stream().filter(d -> d.id == coaId).findFirst().get();
            }

            return Optional.of(h);
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Optional<String> getPrefix() {
            return Optional.ofNullable(prefix);
        }

        public Dynasty getDynasty() {
            return dynasty;
        }

        public Optional<CoatOfArms> getCoatOfArms() {
            return Optional.ofNullable(coa);
        }
    }

    public static class Dynasty {
        private long id;
        private String name;
        private String prefix;
        private CoatOfArms coa;

        public Dynasty() {
        }

        public static Optional<Dynasty> fromNode(Node kv, List<CoatOfArms> coas, Node dynasties) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();
            if (n.isValue() && n.getString().equals("none")) {
                return Optional.empty();
            }

            Dynasty d = new Dynasty();
            d.id = Long.parseLong(kvn.getKeyName());
            if (!n.hasKey("name")) {
                return Optional.empty();
            }

            var dynId = n.getNodeForKey("dynasty").getLong();
            // Entry is a house
            if (dynId != d.id) {
                return Optional.empty();
            }

            d.name = n.getNodeForKey("name").getString()
                    .replace("dynn_", "")
                    .replace("_", " ");
            d.prefix = n.getNodeForKeyIfExistent("prefix").map(s -> s.getString()
                    .replace("dynnp_", "")
                    .replace("_", " ")).orElse(null);


            long coaId;
            if (n.hasKey("coat_of_arms_id")) {
                coaId = n.getNodeForKey("coat_of_arms_id").getLong();
            } else {
                coaId = dynasties.getNodeForKey(String.valueOf(d.id))
                        .getNodeForKeyIfExistent("coat_of_arms_id").map(Node::getLong).orElse(0L);
            }
            d.coa = coas.stream().filter(c -> c.id == coaId).findFirst().get();

            return Optional.of(d);
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Optional<String> getPrefix() {
            return Optional.ofNullable(prefix);
        }

        public Optional<CoatOfArms> getCoatOfArms() {
            return Optional.ofNullable(coa);
        }
    }

    public static class Person {
        private long id;
        private GameDate birth;
        private House house;
        private String firstName;
        private List<Integer> skills;

        public Person() {
        }

        public static Person fromNode(Node kv, List<House> houses) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();

            Person p = new Person();
            p.id = Long.parseLong(kvn.getKeyName());
            if (n.hasKey("dynasty_house")) {
                var id = n.getNodeForKey("dynasty_house").getInteger();
                p.house = houses.stream()
                        .filter(d -> d.id == id)
                        .findFirst().orElse(null);
            }
            p.birth = GameDateType.CK3.fromString(n.getNodeForKey("birth").getString());
            p.skills = n.getNodeForKey("skill").getNodeArray().stream()
                    .map(Node::getInteger)
                    .collect(Collectors.toList());
            p.firstName = n.getNodeForKey("first_name").getString();
            return p;
        }

        public GameDate getBirth() {
            return birth;
        }

        public long getId() {
            return id;
        }

        public Optional<House> getHouse() {
            return Optional.ofNullable(house);
        }

        public String getFirstName() {
            return firstName;
        }

        public List<Integer> getSkills() {
            return skills;
        }
    }

    public static class Title {
        private long id;
        private String name;
        private CoatOfArms coatOfArms;

        public Title() {
        }

        public Title(long id, String name, CoatOfArms coatOfArms) {
            this.id = id;
            this.name = name;
            this.coatOfArms = coatOfArms;
        }

        public static Optional<Title> fromNode(Node kv, List<CoatOfArms> coas) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();
            if (n instanceof ValueNode && n.getString().equals("none")) {
                return Optional.empty();
            }

            var id = Long.parseLong(kvn.getKeyName());
            var name = n.getNodeForKey("name").getString();
            var coaId = n.getNodeForKey("coat_of_arms_id").getLong();
            var coatOfArms = coas.stream().filter(c -> c.id == coaId).findFirst().get();
            return Optional.of(new Title(id, name, coatOfArms));
        }

        public long getId() {
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

        private long id;
        private String patternFile;
        private List<String> colors;
        private List<Emblem> emblems;

        public CoatOfArms() {
        }

        public CoatOfArms(long id, String patternFile, List<String> colors, List<Emblem> emblems) {
            this.id = id;
            this.patternFile = patternFile;
            this.colors = colors;
            this.emblems = emblems;
        }

        public static CoatOfArms fromNode(Node kv) {
            var kvn = kv.getKeyValueNode();
            var n = kvn.getNode();

            var id = Long.parseLong(kvn.getKeyName());
            var patternFile = n.getNodeForKeyIfExistent("pattern").map(Node::getString).orElse(null);

            List<String> colors = new ArrayList<>();
            n.getNodeForKeyIfExistent("color1").map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color2").map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color3").map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color4").map(Node::getString).ifPresent(colors::add);

            List<Emblem> emblems = new ArrayList<>();
            emblems.addAll(n.getNodesForKey("colored_emblem").stream()
                    .map(Emblem::fromColoredEmblemNode)
                    .collect(Collectors.toList()));

            emblems.addAll(n.getNodesForKey("textured_emblem").stream()
                    .map(Emblem::fromTexturedEmblemNode)
                    .collect(Collectors.toList()));

            return new CoatOfArms(id, patternFile, colors, emblems);
        }

        public String getPatternFile() {
            return patternFile;
        }

        public List<String> getColors() {
            return colors;
        }

        public List<Emblem> getEmblems() {
            return emblems;
        }

        public static final class Instance {
            private double x = 0.5;
            private double y = 0.5;

            private double scaleX = 1.0;
            private double scaleY = 1.0;

            private int rotation = 0;

            public double getX() {
                return x;
            }

            public double getY() {
                return y;
            }

            public double getScaleX() {
                return scaleX;
            }

            public double getScaleY() {
                return scaleY;
            }

            public int getRotation() {
                return rotation;
            }
        }

        public static class Emblem {

            private String file;
            private List<String> colors;
            private List<Instance> instances;

            private static Emblem fromTexturedEmblemNode(Node n) {
                Emblem c = new Emblem();
                c.file = n.getNodeForKey("texture").getString();
                c.colors = new ArrayList<>();
                c.instances = List.of(new Instance());
                return c;
            }

            private static Emblem fromColoredEmblemNode(Node n) {
                Emblem c = new Emblem();
                c.file = n.getNodeForKey("texture").getString();

                c.colors = new ArrayList<>();
                c.colors.add(n.getNodeForKey("color1").getString());
                n.getNodeForKeyIfExistent("color2").map(Node::getString).ifPresent(c.colors::add);

                c.instances = n.getNodesForKey("instance").stream().map(i -> {
                    Instance instance = new Instance();
                    i.getNodeForKeyIfExistent("position").ifPresent(p -> {
                        instance.x = p.getNodeArray().get(0).getDouble();
                        instance.y = p.getNodeArray().get(1).getDouble();
                    });
                    i.getNodeForKeyIfExistent("scale").ifPresent(s -> {
                        instance.scaleX = s.getNodeArray().get(0).getDouble();
                        instance.scaleY = s.getNodeArray().get(1).getDouble();
                    });
                    i.getNodeForKeyIfExistent("rotation").ifPresent(r -> {
                        instance.rotation = r.getInteger();
                    });
                    return instance;
                }).collect(Collectors.toList());
                if (c.instances.size() == 0) {
                    c.instances.add(new Instance());
                }
                return c;
            }

            public String getFile() {
                return file;
            }

            public List<String> getColors() {
                return colors;
            }

            public List<Instance> getInstances() {
                return instances;
            }
        }
    }
}
