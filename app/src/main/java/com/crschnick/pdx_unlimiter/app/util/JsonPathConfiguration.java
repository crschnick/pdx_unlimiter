package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import java.io.InputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonPathConfiguration {

    private static final JsonProvider PROVIDER = new JsonProvider() {

        @Override
        public Object parse(String s) throws InvalidJsonException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object parse(InputStream inputStream, String s) throws InvalidJsonException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toJson(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object createArray() {
            return new ArrayNode();
        }

        @Override
        public Object createMap() {
            return new ArrayNode();
        }

        @Override
        public boolean isArray(Object o) {
            if (!(o instanceof ArrayNode)) {
                return false;
            }

            ArrayNode a = (ArrayNode) o;
            if (a.getNodes().size() == 0) {
                return true;
            }

            return !(a.getNodes().get(0) instanceof KeyValueNode);
        }

        @Override
        public int length(Object o) {
            if (o instanceof ValueNode) {
                return Node.getString((Node) o).length();
            } else if (!(o instanceof ArrayNode)) {
                throw new IllegalArgumentException();
            }

            ArrayNode a = (ArrayNode) o;
            return a.getNodes().size();
        }

        @Override
        public Iterable<?> toIterable(Object o) {
            if (!(o instanceof ArrayNode)) {
                throw new IllegalArgumentException();
            }

            ArrayNode a = (ArrayNode) o;
            return a.getNodes().stream().map(this::unwrap).collect(Collectors.toList());
        }

        @Override
        public Collection<String> getPropertyKeys(Object o) {
            if (isMap(o)) {
                return Node.getNodeArray((Node) o).stream()
                        .map(kv -> ((KeyValueNode) kv).getKeyName())
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        @Override
        public Object getArrayIndex(Object o, int i) {
            return unwrap(Node.getNodeArray((Node) o).get(i));
        }

        @Override
        @SuppressWarnings("deprecation")
        public Object getArrayIndex(Object o, int i, boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setArrayIndex(Object o, int i, Object o1) {
            if (!(o instanceof ArrayNode)) {
                throw new IllegalArgumentException();
            }

            ArrayNode a = (ArrayNode) o;
            if (a.getNodes().size() == i) {
                a.getNodes().add(new ValueNode(o1));
            } else {
                a.getNodes().set(i, new ValueNode(o1));
            }
        }

        @Override
        public Object getMapValue(Object o, String s) {
            return Node.getNodeForKeyIfExistent((Node) o, s).map(this::unwrap).orElse(UNDEFINED);
        }

        @Override
        public void setProperty(Object o, Object o1, Object o2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeProperty(Object o, Object o1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isMap(Object o) {
            if (!(o instanceof ArrayNode)) {
                return false;
            }

            ArrayNode a = (ArrayNode) o;
            if (a.getNodes().size() == 0) {
                return false;
            }

            return a.getNodes().get(0) instanceof KeyValueNode;
        }

        @Override
        public Object unwrap(Object o) {
            if (o == null) {
                return null;
            }

            if (!(o instanceof ValueNode)) {
                return o;
            }

            ValueNode node = (ValueNode) o;
            Object value = node.getValue();
            return value;
        }
    };
    private static final MappingProvider MAPPER = new MappingProvider() {

        @Override
        public <T> T map(Object o, Class<T> aClass, Configuration configuration) {
            if (aClass == List.class) {
                return (T) ((ArrayNode) o).getNodes().stream().map(v -> PROVIDER.unwrap(v)).collect(Collectors.toList());
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T map(Object o, TypeRef<T> typeRef, Configuration configuration) {
            throw new UnsupportedOperationException();
        }
    };

    public static void init() {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = PROVIDER;
            private final MappingProvider mappingProvider = MAPPER;

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.of(Option.ALWAYS_RETURN_LIST);
            }
        });

    }

}
