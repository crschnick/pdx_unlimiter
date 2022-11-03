package com.crschnick.pdxu.app.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonHelper {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        ObjectMapper objectMapper = MAPPER;
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.setVisibility(objectMapper
                                           .getSerializationConfig()
                                           .getDefaultVisibilityChecker()
                                           .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                           .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                           .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                           .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                                           .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public static <T> T readObject(Class<T> clazz, Path in) throws IOException {
        return MAPPER.readValue(Files.readAllBytes(in), clazz);
    }

    public static void writeObject(Object obj, Path out) throws IOException {
        JsonFactory f = new JsonFactory();
        try (JsonGenerator g = f.createGenerator(out.toFile(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter())) {
            MAPPER.writeValue(g, obj);
        }
    }

    public static JsonNode read(Path in) throws IOException {
        return MAPPER.readTree(in.toFile());
    }

    public static void write(JsonNode node, Path out) throws IOException {
        JsonFactory f = new JsonFactory();
        try (JsonGenerator g = f.createGenerator(out.toFile(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter())) {
            MAPPER
                    .writeTree(g, node);
        }
    }
}
