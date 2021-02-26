package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.NodeContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface NodeWriter {

    static String writeToString(ArrayNode node, int maxLines, String indent) {
        var out = new ByteArrayOutputStream();
        var writer = new NodeWriterImpl(out, StandardCharsets.UTF_8, maxLines, indent);
        try {
            node.writeTopLevel(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    static void write(OutputStream out, Charset charset, ArrayNode node, String indent) throws IOException {
        var writer = new NodeWriterImpl(out, charset, Integer.MAX_VALUE, indent);
        node.writeTopLevel(writer);
    }

    void incrementIndent();

    void decrementIndent();

    void indent() throws IOException;

    void write(NodeContext ctx, int index) throws IOException;

    void write(String s) throws IOException;

    void newLine() throws IOException;
}
