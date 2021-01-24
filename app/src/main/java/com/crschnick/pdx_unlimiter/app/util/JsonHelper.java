package com.crschnick.pdx_unlimiter.app.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonHelper {

    public static <T> T readObject(Class<T> clazz, Path in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        return mapper.readValue(Files.readAllBytes(in), clazz);
    }

    public static void writeObject(Object obj, Path out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        JsonFactory f = new JsonFactory();
        try (JsonGenerator g = f.createGenerator(out.toFile(), JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter())) {
            mapper.writeValue(g, obj);
        }
    }

    public static JsonNode read(Path in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        return mapper.readTree(in.toFile());
    }

    public static void write(JsonNode node, OutputStream out) throws IOException {
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createGenerator(out)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeTree(g, node);
        out.close();
    }
}
