package com.crschnick.pdx_unlimiter.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public abstract class Node {

    private static final Pattern LONG = Pattern.compile("-?[0-9]+");
    private static final Pattern DOUBLE = Pattern.compile("-?([0-9]+)\\.([0-9]+)");

    public static Node combine(Node... nodes) {
        ArrayNode a = new ArrayNode();
        for (Node n : nodes) {
            a.getNodes().addAll(n.getNodeArray());
        }
        return a;
    }

    public Optional<Node> getNodeForKeyIfExistent(Node this, String key) {
        var list = getNodesForKey(key);
        return list.size() == 0 ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean getBoolean() {
        if (!(this instanceof ValueNode)) {
            throw new NodeFormatException("Not a value this:\n" + this.toString());
        }

        String v = getString();
        if (!v.equals("yes") && !v.equals("no")) {
            throw new NodeFormatException("Not a boolean this:\n" + this.toString());
        }

        return v.equals("yes");
    }

    public String getString() {
        if (!(this instanceof ValueNode)) {
            throw new NodeFormatException("Not a value this:\n" + this.toString());
        }

        return (String) ((ValueNode) this).getValue();
    }

    public int getInteger() {
        String sv = getString();
        if (LONG.matcher(sv).matches()) {
            long v = Long.parseLong(sv);
            if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
                return (int) v;
            } else {
                throw new NodeFormatException("Cannot cast long to int");
            }
        } else {
            throw new NodeFormatException("Not a int node:\n" + this.toString());
        }
    }

    public long getLong() {
        String sv = getString();
        if (LONG.matcher(sv).matches()) {
            long v = Long.parseLong(sv);
            return v;
        } else {
            throw new NodeFormatException("Not a long node:\n" + this.toString());
        }
    }

    public boolean isValue() {
        return this instanceof ValueNode;
    }

    public boolean isBoolean() {
        if (!isValue()) {
            return false;
        }

        String sv = getString();
        return sv.equals("yes") || sv.equals("no");
    }

    public boolean isInteger() {
        if (!isValue()) {
            return false;
        }

        String sv = getString();
        return LONG.matcher(sv).matches();
    }

    public boolean isDouble() {
        if (!isValue()) {
            return false;
        }

        String sv = getString();
        return LONG.matcher(sv).matches() || DOUBLE.matcher(sv).matches();
    }

    public double getDouble() {
        String sv = getString();
        if (LONG.matcher(sv).matches() || DOUBLE.matcher(sv).matches()) {
            double v = Double.parseDouble(sv);
            return v;
        } else {
            throw new NodeFormatException("Not a double node:\n" + this.toString());
        }
    }


    public List<Node> getNodeArray() {
        if (!(this instanceof ArrayNode)) {
            throw new NodeFormatException("Not an array this:\n" + this.toString());
        }

        return ((ArrayNode) this).getNodes();
    }

    public boolean hasKey(String key) {
        if (!(this instanceof ArrayNode)) {
            throw new NodeFormatException("Not an array this:\n" + this.toString());
        }

        var list = getNodesForKey(key);
        return list.size() > 0;
    }

    public KeyValueNode getKeyValueNode() {
        if (!(this instanceof KeyValueNode)) {
            throw new NodeFormatException("Not a key-value this:\n" + this.toString());
        }

        return (KeyValueNode) this;
    }

    public Node getNodeForKey(String key) {
        var list = getNodesForKey(key);
        if (list.size() > 1) {
            throw new NodeFormatException("Too many entries for key " + key + " for this:\n" + this.toString());
        }
        if (list.size() == 0) {
            throw new NodeFormatException("Invalid key " + key + " for this:\n" + this.toString());
        }
        return list.get(0);
    }

    public List<Node> getNodesForKey(String key) {
        List<Node> thiss = new ArrayList<>();
        if (!(this instanceof ArrayNode)) {
            throw new NodeFormatException("Not an array this:\n" + this.toString());
        }

        ArrayNode array = (ArrayNode) this;
        for (Node sub : array.getNodes()) {
            if (sub instanceof KeyValueNode) {
                KeyValueNode kvNode = (KeyValueNode) sub;
                if (key.equals("*") || kvNode.getKeyName().equals(key)) {
                    thiss.add(kvNode.getNode());
                }
            }
        }

        return thiss;
    }
}
