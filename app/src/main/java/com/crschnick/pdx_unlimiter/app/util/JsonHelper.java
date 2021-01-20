package com.crschnick.pdx_unlimiter.app.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonHelper {


    public static <T> T readObject(Class<T> clazz, InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        return mapper.readValue(in.readAllBytes(), clazz);
    }

    public static void writeObject(Object obj, OutputStream out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createGenerator(out)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        mapper.writeValue(g, obj);
        out.close();
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
