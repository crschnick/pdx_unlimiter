package com.crschnick.pdx_unlimiter.core.node;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class NodeContext {

    private final byte[] data;
    private final Charset charset;
    private final int[] literalsBegin;
    private final short[] literalsLength;
    private final int literalsCount;

    public NodeContext() {
        this.data = null;
        this.charset = StandardCharsets.UTF_8;
        this.literalsBegin = null;
        this.literalsLength = null;
        this.literalsCount = 0;
    }

    public NodeContext(String data) {
        this.data = data.getBytes();
        this.charset = StandardCharsets.UTF_8;
        this.literalsBegin = new int[]{0};
        this.literalsLength = new short[]{(short) data.length()};
        this.literalsCount = 1;
    }

    public NodeContext(byte[] data, Charset charset, int[] literalsBegin, short[] literalsLength, int literalsCount) {
        this.data = data;
        this.charset = charset;
        this.literalsBegin = literalsBegin;
        this.literalsLength = literalsLength;
        this.literalsCount = literalsCount;
    }

    public String evaluate(int literalIndex) {
        return new String(data, literalsBegin[literalIndex], literalsLength[literalIndex], charset);
    }

    public byte[] getData() {
        return data;
    }

    public Charset getCharset() {
        return charset;
    }

    public int[] getLiteralsBegin() {
        return literalsBegin;
    }

    public short[] getLiteralsLength() {
        return literalsLength;
    }
}
