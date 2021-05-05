package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Ck3CoatOfArms {

    private List<Sub> subs;

    public Ck3CoatOfArms() {
    }

    public Ck3CoatOfArms(List<Sub> subs) {
        this.subs = subs;
    }

    static Ck3CoatOfArms empty() {
        return new Ck3CoatOfArms(List.of(Sub.empty()));
    }

    public static Map<Long, Ck3CoatOfArms> createCoaMap(Node node) {
        Map<Long, Ck3CoatOfArms> map = new HashMap<>();
        node.forEach((k, v) -> {
            var coa = fromNode(v);
            map.put(Long.parseLong(k), coa);
        });
        return map;
    }

    public static Ck3CoatOfArms fromNode(Node n) {
        List<Sub> subs = new ArrayList<>();
        subs.addAll(Sub.fromNode(n));
        for (var subNode : n.getNodesForKey("sub")) {
            subs.addAll(Sub.fromNode(subNode));
        }
        return new Ck3CoatOfArms(subs);
    }

    public List<Sub> getSubs() {
        return subs;
    }

    public static final class Sub {

        static Sub empty() {
            return new Sub(0, 0, 1, 1, "pattern_solid.dds", List.of("black", "black"), List.of(Emblem.empty()));
        }

        private double x = 0.5;
        private double y = 0.5;

        private double scaleX = 1.0;
        private double scaleY = 1.0;

        private String patternFile;
        private List<String> colors;
        private List<Emblem> emblems;

        public Sub() {
        }

        public Sub(double x, double y, double scaleX, double scaleY, String patternFile, List<String> colors, List<Emblem> emblems) {
            this.x = x;
            this.y = y;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.patternFile = patternFile;
            this.colors = colors;
            this.emblems = emblems;
        }

        public static List<Sub> fromNode(Node n) {
            List<Sub> subs = new ArrayList<>();
            if (!n.hasKey("instance")) {
                subs.add(subInstance(n, null));
            } else {
                for (var in : n.getNodesForKey("instance")) {
                    subs.add(subInstance(n, in));
                }
            }
            return subs;
        }

        public static Sub subInstance(Node n, Node instanceNode) {
            var patternFile = n.getNodeForKeyIfExistent("pattern").map(Node::getString).orElse(null);

            List<String> colors = new ArrayList<>();
            n.getNodeForKeyIfExistent("color1").filter(Node::isValue).map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color2").filter(Node::isValue).map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color3").filter(Node::isValue).map(Node::getString).ifPresent(colors::add);
            n.getNodeForKeyIfExistent("color4").filter(Node::isValue).map(Node::getString).ifPresent(colors::add);

            List<Emblem> emblems = new ArrayList<>();
            emblems.addAll(n.getNodesForKey("colored_emblem").stream()
                    .map(Emblem::fromColoredEmblemNode)
                    .collect(Collectors.toList()));

            emblems.addAll(n.getNodesForKey("textured_emblem").stream()
                    .map(Emblem::fromTexturedEmblemNode)
                    .collect(Collectors.toList()));

            double x = 0;
            double y = 0;
            if (instanceNode != null) {
                var offset = instanceNode.getNodeForKeyIfExistent("offset").orElse(null);
                if (offset != null) {
                    x = offset.getNodeArray().get(0).getDouble();
                    y = offset.getNodeArray().get(1).getDouble();
                }
            }

            double sx = 1;
            double sy = 1;
            if (instanceNode != null) {
                var scale = instanceNode.getNodeForKeyIfExistent("scale").orElse(null);
                if (scale != null) {
                    sx = scale.getNodeArray().get(0).getDouble();
                    sy = scale.getNodeArray().get(1).getDouble();
                }
            }

            return new Sub(x, y, sx, sy, patternFile, colors, emblems);
        }

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

        public String getPatternFile() {
            return patternFile;
        }

        public List<String> getColors() {
            return colors;
        }

        public List<Emblem> getEmblems() {
            return emblems;
        }
    }

    public static final class Instance {
        private double x = 0.5;
        private double y = 0.5;

        private double scaleX = 1.0;
        private double scaleY = 1.0;

        private int rotation = 0;

        private int depth = 0;

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

        public int getDepth() {
            return depth;
        }
    }

    public static class Emblem {

        private String file;
        private List<Integer> mask;
        private List<String> colors;
        private List<Instance> instances;

        private static Emblem empty() {
            Emblem c = new Emblem();
            c.file = "_default.dds";
            c.mask = new ArrayList<>();
            c.colors = List.of();
            c.instances = List.of(new Instance());
            return c;
        }

        private static Emblem fromTexturedEmblemNode(Node n) {
            Emblem c = new Emblem();
            c.file = n.getNodeForKey("texture").getString();
            c.colors = new ArrayList<>();
            c.instances = List.of(new Instance());
            
            n.getNodeForKeyIfExistent("mask").ifPresentOrElse(r -> {
                c.mask = r.getNodeArray().stream().map(Node::getInteger).collect(Collectors.toList());
            }, () -> c.mask = new ArrayList<>());

            return c;
        }

        private static Emblem fromColoredEmblemNode(Node n) {
            Emblem c = new Emblem();
            c.file = n.getNodeForKey("texture").getString();

            c.colors = new ArrayList<>();
            // Even color1 can sometimes be missing
            n.getNodeForKeyIfExistent("color1").filter(Node::isValue).map(Node::getString).ifPresent(c.colors::add);
            n.getNodeForKeyIfExistent("color2").filter(Node::isValue).map(Node::getString).ifPresent(c.colors::add);

            n.getNodeForKeyIfExistent("mask").ifPresentOrElse(r -> {
                c.mask = r.getNodeArray().stream().map(Node::getInteger).collect(Collectors.toList());
            }, () -> c.mask = new ArrayList<>());

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
                i.getNodeForKeyIfExistent("depth").ifPresent(r -> {
                    instance.depth = r.getInteger();
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

        public List<Integer> getMask() {
            return mask;
        }
    }
}
