package com.crschnick.pdx_unlimiter.core.io;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class SavegameWriter {


    public static String indent(int amount) {
        String s = "";
        for (int i = 0; i < amount; i++) {
            s = s.concat("  ");
        }
        return s;
    }

    public static void writeNode(Node node, OutputStream out) throws IOException {
        out.write(writeNode(node, 0).getBytes(StandardCharsets.UTF_8));
    }

    private static String writeNode(Node node, int indentation) {
        if (node instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) node;
            return "{\n" + a.getNodes().stream().map(
                    (n) -> indent(indentation + 1) + writeNode(n, indentation + 1))
                    .collect(Collectors.joining(",\n")) + "\n" + indent(indentation) + "}";
        } else if (node instanceof KeyValueNode) {
            KeyValueNode kv = (KeyValueNode) node;
            return kv.getKeyName() + "=" + writeNode(kv.getNode(), indentation);
        } else if (node instanceof ValueNode) {
            ValueNode v = (ValueNode) node;
            return v.getValue().toString();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
