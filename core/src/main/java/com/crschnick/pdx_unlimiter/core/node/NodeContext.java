package com.crschnick.pdx_unlimiter.core.node;

import java.nio.charset.Charset;
import java.util.function.Consumer;

public class NodeContext {

    private byte[] data;
    private Charset charset;

    public NodeContext(byte[] data, Charset charset) {
        this.data = data;
        this.charset = charset;
    }

    public String evaluate(int begin, int end) {
        return new String(data, begin, end, charset);
    }

    public byte[] getData() {
        return data;
    }

    public Charset getCharset() {
        return charset;
    }
}
