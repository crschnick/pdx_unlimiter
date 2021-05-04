package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.NodeContext;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface NodeWriter {

    static byte[] writeToBytes(ArrayNode node, int maxLines, String indent) {
        var out = new ByteArrayOutputStream();
        var writer = new NodeWriterImpl(out, StandardCharsets.UTF_8, maxLines, indent);
        try {
            node.writeTopLevel(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    static String writeToString(ArrayNode node, int maxLines, String indent) {
        return new String(writeToBytes(node, maxLines, indent), StandardCharsets.UTF_8);
    }

    static void write(OutputStream out, Charset charset, ArrayNode node, String indent) throws IOException {
        var bout = new BufferedOutputStream(out, 1000000);
        try {
            var writer = new NodeWriterImpl(bout, charset, Integer.MAX_VALUE, indent);
            node.writeTopLevel(writer);
            bout.flush();
        } catch (IOException e) {
            bout.close();
            throw e;
        }
    }

    void incrementIndent();

    void decrementIndent();

    void indent() throws IOException;

    void write(NodeContext ctx, int index) throws IOException;

    void write(String s) throws IOException;

    void space() throws IOException;

    void newLine() throws IOException;
}
