package com.crschnick.pdx_unlimiter.core.node;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public class NodeContext {

    private byte[] data;
    private Charset charset;

    public NodeContext(String data) {
        this.data = data.getBytes(charset);
        this.charset = StandardCharsets.UTF_8;
    }

    public NodeContext(byte[] data, Charset charset) {
        this.data = data;
        this.charset = charset;
    }

    public byte[] getSubData(int begin, int length) {
        return Arrays.copyOfRange(data, begin, begin + length);
    }

    public String evaluate(int begin, int length) {
        return new String(data, begin, length, charset);
    }

    public byte[] getData() {
        return data;
    }

    public Charset getCharset() {
        return charset;
    }
}
