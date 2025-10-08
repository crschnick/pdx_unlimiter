package com.crschnick.pdxu.app.util;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.nio.charset.Charset;
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

        context.setMixInAnnotations(Throwable.class, ThrowableTypeMixIn.class);

        context.addSerializers(_serializers);
        context.addDeserializers(_deserializers);
    }

    public static class OsTypeSerializer extends JsonSerializer<OsType> {

        @Override
        public void serialize(OsType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.getId());
        }
    }

    public static class OsTypeDeserializer extends JsonDeserializer<OsType> {

        @Override
        public OsType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var stream = Stream.of(OsType.WINDOWS, OsType.LINUX, OsType.MACOS);
            var n = p.getValueAsString();
            return stream.filter(osType ->
                            osType.getName().equals(n) || osType.getId().equals(n))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static class CharsetSerializer extends JsonSerializer<Charset> {

        @Override
        public void serialize(Charset value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.name());
        }
    }

    public static class CharsetDeserializer extends JsonDeserializer<Charset> {

        @Override
        public Charset deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Charset.forName(p.getValueAsString());
        }
    }

    public static class LocalPathSerializer extends JsonSerializer<Path> {

        @Override
        public void serialize(Path value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(value.toString());
        }
    }

    public static class LocalPathDeserializer extends JsonDeserializer<Path> {

        @Override
        public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                return Path.of(p.getValueAsString());
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
