package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.NodeContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class NodeWriterImpl implements NodeWriter {

    private static final byte[] NEW_LINE = "\n".getBytes();

    public static String writeToString(Node node, int maxLines, String indent) throws IOException {
        var out = new ByteArrayOutputStream();
        var writer = new NodeWriterImpl(out, StandardCharsets.UTF_8, maxLines, indent);
        node.write(writer);
        return out.toString(StandardCharsets.UTF_8);
    }

    public static void write(OutputStream out, Charset charset, Node node, String indent) throws IOException {
        var writer = new NodeWriterImpl(out, charset, Integer.MAX_VALUE, indent);
        node.write(writer);
    }

    private final OutputStream out;
    private final Charset charset;
    private final int maxLines;
    private final byte[] indentValue;

    private int currentLines;
    private boolean hitMaxLines;
    private int indent;

    public NodeWriterImpl(OutputStream out, Charset charset, int maxLines, String indentValue) {
        this.out = out;
        this.charset = charset;
        this.maxLines = maxLines;
        this.indentValue = indentValue.getBytes();
    }

    @Override
    public void incrementIndent() {
        indent++;
    }

    @Override
    public void decrementIndent() {
        indent--;
    }

    @Override
    public void indent() throws IOException {
        if (hitMaxLines) {
            return;
        }

        for (int i = 0; i < indent; i++) {
            out.write(indentValue);
        }
    }

    @Override
    public void write(NodeContext ctx, int begin, int length) throws IOException {
        if (hitMaxLines) {
            return;
        }

        if (ctx.getCharset().equals(charset)) {
            out.write(ctx.getData(), begin, length);
        } else {
            var s = ctx.evaluate(begin, length);
            out.write(s.getBytes(charset));
        }
    }

    @Override
    public void write(String s) throws IOException {
        if (hitMaxLines) {
            return;
        }

        out.write(s.getBytes(charset));
    }

    @Override
    public void newLine() throws IOException {
        if (hitMaxLines) {
            return;
        }

        out.write(NEW_LINE);

        currentLines++;
        if (currentLines >= maxLines) {
            hitMaxLines = true;
        }
    }
}
