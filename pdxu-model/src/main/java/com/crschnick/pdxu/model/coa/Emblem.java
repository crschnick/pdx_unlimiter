package com.crschnick.pdxu.model.coa;

import com.crschnick.pdxu.io.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Emblem {

    private String file;
    private List<Integer> mask;
    private String[] colors;
    private List<Instance> instances;

    static Emblem empty() {
        Emblem c = new Emblem();
        c.file = "_default.dds";
        c.mask = new ArrayList<>();
        c.colors = new String[3];
        c.instances = List.of(new Instance());
        return c;
    }

    static Emblem fromTexturedEmblemNode(Node n) {
        Emblem c = new Emblem();
        n.getNodeForKeyIfExistent("texture")
                .ifPresentOrElse(
                        tex -> {
                            c.file = tex.getString();
                        },
                        () -> c.file = "_default.dds"
                );
        c.colors = null;

        n.getNodeForKeyIfExistent("mask")
                .ifPresentOrElse(
                        r -> {
                            c.mask = r.getNodeArray().stream()
                                    .map(Node::getInteger)
                                    .collect(Collectors.toList());
                        },
                        () -> c.mask = new ArrayList<>()
                );

        c.instances = n.getNodesForKey("instance").stream()
                .map(i -> {
                    Instance instance = new Instance();
                    i.getNodeForKeyIfExistent("position").ifPresent(p -> {
                        if (p.isArray()) {
                            instance.x = p.getNodeArray().get(0).getDouble();
                            instance.y = p.getNodeArray().get(1).getDouble();
                        }
                    });
                    i.getNodeForKeyIfExistent("scale").ifPresent(s -> {
                        if (s.isArray()) {
                            instance.scaleX = s.getNodeArray().get(0).getDouble();
                            instance.scaleY = s.getNodeArray().get(1).getDouble();
                        }
                    });
                    i.getNodeForKeyIfExistent("rotation").ifPresent(r -> {
                        instance.rotation = r.getDouble();
                    });
                    i.getNodeForKeyIfExistent("depth").ifPresent(r -> {
                        instance.depth = r.getDouble();
                    });
                    return instance;
                })
                .collect(Collectors.toList());

        if (c.instances.size() == 0) {
            c.instances.add(new Instance());
        }

        return c;
    }

    static Emblem fromColoredEmblemNode(Node n, CoatOfArms.Sub sub) {
        Emblem c = new Emblem();
        n.getNodeForKeyIfExistent("texture")
                .ifPresentOrElse(
                        tex -> {
                            c.file = tex.getString();
                        },
                        () -> c.file = "_default.dds"
                );

        c.colors = new String[3];

        // Color Values
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            n.getNodeForKeyIfExistent(CoatOfArms.COLOR_NAMES.get(i))
                    .filter(node -> (node.isValue() || node.isTagged()) && !CoatOfArms.COLOR_NAMES.contains(node.getString()))
                    .map(Node::getString)
                    .ifPresent(s -> c.colors[finalI] = s);
        }

        // Instance Color References
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            n.getNodeForKeyIfExistent(CoatOfArms.COLOR_NAMES.get(i))
                    .filter(node -> node.isValue() && CoatOfArms.COLOR_NAMES.contains(node.getString()))
                    .map(node -> CoatOfArms.COLOR_NAMES.indexOf(node.getString()))
                    .ifPresent(referenceIndex -> c.colors[finalI] = sub.getColors()[referenceIndex]);
        }

        n.getNodeForKeyIfExistent("mask")
                .ifPresentOrElse(
                        r -> {
                            c.mask = r.getNodeArray().stream()
                                    .map(Node::getInteger)
                                    .collect(Collectors.toList());
                        },
                        () -> c.mask = new ArrayList<>()
                );

        c.instances = n.getNodesForKey("instance").stream()
                .map(i -> {
                    Instance instance = new Instance();
                    i.getNodeForKeyIfExistent("position").ifPresent(p -> {
                        if (p.isArray()) {
                            instance.x = p.getNodeArray().get(0).getDouble();
                            instance.y = p.getNodeArray().get(1).getDouble();
                        }
                    });
                    i.getNodeForKeyIfExistent("scale").ifPresent(s -> {
                        if (s.isArray() && s.getArrayNode().size() == 1) {
                            instance.scaleX = s.getNodeArray().get(0).getDouble();
                            instance.scaleY = 1;
                        } else if (s.isArray()) {
                            instance.scaleX = s.getNodeArray().get(0).getDouble();
                            instance.scaleY = s.getNodeArray().get(1).getDouble();
                        }
                    });
                    i.getNodeForKeyIfExistent("rotation").ifPresent(r -> {
                        instance.rotation = r.getDouble();
                    });
                    i.getNodeForKeyIfExistent("depth").ifPresent(r -> {
                        instance.depth = r.getDouble();
                    });
                    return instance;
                })
                .collect(Collectors.toList());
        if (c.instances.size() == 0) {
            c.instances.add(new Instance());
        }
        return c;
    }

    public String getFile() {
        return file;
    }

    public String[] getColors() {
        return colors;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public List<Integer> getMask() {
        return mask;
    }

    public static final class Instance {
        private double x = 0.5;
        private double y = 0.5;

        private double scaleX = 1.0;
        private double scaleY = 1.0;

        private double rotation = 0;

        // Since 1.5 the depth can be a double
        private double depth = 0;

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

        public double getRotation() {
            return rotation;
        }

        public double getDepth() {
            return depth;
        }
    }
}
