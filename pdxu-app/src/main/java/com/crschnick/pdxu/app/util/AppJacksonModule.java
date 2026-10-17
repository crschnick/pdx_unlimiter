package com.crschnick.pdxu.app.util;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.module.SimpleModule;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class AppJacksonModule extends SimpleModule {

    @Override
    public void setupModule(SetupContext context) {
        addSerializer(OsType.class, new OsTypeSerializer());
        addDeserializer(OsType.class, new OsTypeDeserializer());

        addSerializer(Charset.class, new CharsetSerializer());
        addDeserializer(Charset.class, new CharsetDeserializer());

        addSerializer(Path.class, new LocalPathSerializer());
        addDeserializer(Path.class, new LocalPathDeserializer());

        setMixInAnnotation(Throwable.class, ThrowableTypeMixIn.class);

        super.setupModule(context);
    }

    public static class OsTypeSerializer extends ValueSerializer<OsType> {

        @Override
        public void serialize(OsType value, JsonGenerator jgen, SerializationContext context) {
            jgen.writeString(value.getId());
        }
    }

    public static class OsTypeDeserializer extends ValueDeserializer<OsType> {

        @Override
        public OsType deserialize(JsonParser p, DeserializationContext ctxt) {
            var stream = Stream.of(OsType.WINDOWS, OsType.LINUX, OsType.MACOS);
            var n = p.getValueAsString();
            return stream.filter(osType ->
                            osType.getName().equals(n) || osType.getId().equals(n))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static class CharsetSerializer extends ValueSerializer<Charset> {

        @Override
        public void serialize(Charset value, JsonGenerator jgen, SerializationContext context) {
            jgen.writeString(value.name());
        }
    }

    public static class CharsetDeserializer extends ValueDeserializer<Charset> {

        @Override
        public Charset deserialize(JsonParser p, DeserializationContext ctxt) {
            return Charset.forName(p.getValueAsString());
        }
    }

    public static class LocalPathSerializer extends ValueSerializer<Path> {

        @Override
        public void serialize(Path value, JsonGenerator jgen, SerializationContext context) {
            jgen.writeString(value.toString());
        }
    }

    public static class LocalPathDeserializer extends ValueDeserializer<Path> {

        @Override
        public Path deserialize(JsonParser p, DeserializationContext ctxt) {
            var s = p.getValueAsString();
            if (s == null) {
                return null;
            }

            // Older versions did not register this module and therefore used the Jackson default path serializer,
            // which writes paths as file URIs, e.g. file:///C:/Program%20Files/
            if (s.startsWith("file:")) {
                try {
                    return Path.of(URI.create(s));
                } catch (IllegalArgumentException | FileSystemNotFoundException ignored) {
                }
            }

            try {
                return Path.of(s);
            } catch (InvalidPathException ignored) {
                return null;
            }
        }
    }

    @JsonSerialize(as = Throwable.class)
    public abstract static class ThrowableTypeMixIn {

        @SuppressWarnings("unused")
        @JsonIdentityInfo(generator = ObjectIdGenerators.StringIdGenerator.class, property = "$id")
        private Throwable cause;
    }
}
