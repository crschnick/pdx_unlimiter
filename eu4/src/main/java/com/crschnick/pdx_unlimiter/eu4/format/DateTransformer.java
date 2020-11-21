package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.*;

import java.util.function.Function;

public class DateTransformer<T> extends NodeTransformer {

    public static final DateTransformer<GameDate> EU4 = new DateTransformer<>(GameDate::fromLong, GameDate::fromString, GameDate::toNode);
    public static final DateTransformer<Hoi4Date> HOI4 = new DateTransformer<>(Hoi4Date::fromLong, Hoi4Date::fromString, Hoi4Date::toNode);

    private Function<Long,T> longFunc;
    private Function<String,T> stringFunc;
    private Function<T,Node> nodeFunc;


    public DateTransformer(Function<Long, T> longFunc, Function<String, T> stringFunc, Function<T, Node> nodeFunc) {
        this.longFunc = longFunc;
        this.stringFunc = stringFunc;
        this.nodeFunc = nodeFunc;
    }

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode v = (ValueNode) kv.getNode();
        T d = null;
        if (v.getValue() instanceof Long) {
            d = longFunc.apply((Long) v.getValue());
        } else if (v.getValue() instanceof String) {
            String s = (String) v.getValue();
            d = stringFunc.apply(s);
        }
        if (d == null) {
            throw new IllegalArgumentException("Invalid date value " + v.getValue());
        }
        kv.setNode(nodeFunc.apply(d));
    }

    @Override
    public void reverse(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        kv.setNode(new ValueNode(GameDate.fromNode(kv.getNode()).toString()));
    }
}
