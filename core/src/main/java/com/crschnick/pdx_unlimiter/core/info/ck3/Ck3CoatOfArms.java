package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Ck3CoatOfArms {

    private String patternFile;
    private List<String> colors;
    private List<Emblem> emblems;

    public Ck3CoatOfArms() {
    }

    private Ck3CoatOfArms(String patternFile, List<String> colors, List<Emblem> emblems) {
        this.patternFile = patternFile;
        this.colors = colors;
        this.emblems = emblems;
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

        return new Ck3CoatOfArms(patternFile, colors, emblems);
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
