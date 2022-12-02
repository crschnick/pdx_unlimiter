package com.crschnick.pdxu.model.coa;

import com.crschnick.pdxu.io.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CoatOfArms {

    private List<Sub> subs;

    public CoatOfArms() {
    }

    public CoatOfArms(List<Sub> subs) {
        this.subs = subs;
    }

    public static CoatOfArms empty() {
        return new CoatOfArms(List.of(Sub.empty()));
    }

    public static Map<Long, CoatOfArms> createCoaMap(Node node) {
        Map<Long, CoatOfArms> map = new HashMap<>();
        if (node == null) {
            return map;
        }

        node.forEach((k, v) -> {
            var coa = fromNode(v, null);
            map.put(Long.parseLong(k), coa);
        });
        return map;
    }

    public static CoatOfArms fromNode(Node n, Function<String, Node> parentResolver) {
        List<Sub> subs = new ArrayList<>();
        subs.addAll(Sub.fromNode(n, parentResolver));
        for (var subNode : n.getNodesForKey("sub")) {
            subs.addAll(Sub.fromNode(subNode, parentResolver));
        }
        return new CoatOfArms(subs);
    }

    public List<Sub> getSubs() {
        return subs;
    }

    public static final List<String> COLOR_NAMES = List.of("color1", "color2", "color3", "color4", "color5");

    public static final class Sub {

        private double x = 0.0;
        private double y = 0.0;
        private double scaleX = 1.0;
        private double scaleY = 1.0;
        private String patternFile;
        private String[] colors = new String[COLOR_NAMES.size()];
        private List<Emblem> emblems = new ArrayList<>();

        public Sub() {
        }

        private Sub(
                double x,
                double y,
                double scaleX,
                double scaleY,
                String patternFile,
                String[] colors,
                List<Emblem> emblems
        ) {
            this.x = x;
            this.y = y;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.patternFile = patternFile;
            this.colors = colors;
            this.emblems = emblems;
        }

        static Sub empty() {
            return new Sub(0, 0, 1, 1, "pattern_solid.dds", new String[]{
                    "black",
                    "black",
                    null,
                    null,
                    null
            }, List.of(Emblem.empty()));
        }

        public static List<Sub> fromNode(Node n, Function<String, Node> parentResolver) {
            // A sub entry must be an array
            if (!n.isArray()) {
                return List.of();
            }

            List<Sub> subs = new ArrayList<>();
            if (!n.hasKey("instance")) {
                subs.add(subInstance(n, null, parentResolver));
            } else {
                for (var in : n.getNodesForKey("instance")) {
                    subs.add(subInstance(n, in, parentResolver));
                }
            }
            return subs;
        }

        public static Sub subInstance(Node n, Node instanceNode, Function<String, Node> parentResolver) {
            var parentNode = n.getNodeForKeyIfExistent("parent")
                    .filter(node -> parentResolver != null)
                    .map(node -> parentResolver.apply(node.getString()))
                    .orElse(null);
            var sub = parentNode != null ? subInstance(parentNode, null, parentResolver) : new Sub();

            // Color Values
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                n.getNodeForKeyIfExistent(COLOR_NAMES.get(i))
                        .filter(node -> (node.isValue() || node.isTagged()) && !COLOR_NAMES.contains(node.getString()))
                        .map(Node::getString)
                        .ifPresent(s -> sub.colors[finalI] = s);
            }

            // Color References
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                n.getNodeForKeyIfExistent(COLOR_NAMES.get(i))
                        .filter(node -> node.isValue() && COLOR_NAMES.contains(node.getString()))
                        .map(node -> COLOR_NAMES.indexOf(node.getString()))
                        .ifPresent(referenceIndex -> sub.colors[finalI] = sub.colors[referenceIndex]);
            }

            // Ugly fix to override colored emblem colors of parent
            var parentEmblemList = new ArrayList<>(parentNode != null ? parentNode.getNodesForKey("colored_emblem").stream()
                    .map(node -> Emblem.fromColoredEmblemNode(node, sub)).toList() : List.of());
            if (parentNode != null) {
                parentEmblemList.addAll(parentNode.getNodesForKey("textured_emblem").stream()
                                                .map(Emblem::fromTexturedEmblemNode).toList());
            }
            sub.emblems = parentEmblemList;

            n.getNodeForKeyIfExistent("pattern").map(Node::getString).ifPresent(s -> sub.patternFile = s);

            var emblemList = new ArrayList<>(n.getNodesForKey("colored_emblem").stream()
                                                     .map(node -> Emblem.fromColoredEmblemNode(node, sub)).toList());
            emblemList.addAll(n.getNodesForKey("textured_emblem").stream()
                                      .map(Emblem::fromTexturedEmblemNode).toList());
            if (emblemList.size() > 0) {
                sub.emblems.clear();
                sub.emblems.addAll(emblemList);
            }

            if (instanceNode != null) {
                var offset = instanceNode.getNodeForKeyIfExistent("offset").orElse(null);
                if (offset != null) {
                    sub.x = offset.getNodeArray().get(0).getDouble();
                    sub.y = offset.getNodeArray().get(1).getDouble();
                }

                var scale = instanceNode.getNodeForKeyIfExistent("scale").orElse(null);
                if (scale != null) {
                    sub.scaleX = Math.min(scale.getNodeArray().get(0).getDouble(), 1 - sub.x);
                    sub.scaleY = Math.min(scale.getNodeArray().get(1).getDouble(), 1 - sub.y);
                }
            }

            return sub;
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

        public String[] getColors() {
            return colors;
        }

        public List<Emblem> getEmblems() {
            return emblems;
        }
    }

}
