package com.crschnick.pdx_unlimiter.app.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.OutputStream;

public class JsonHelper {

    public static void write(JsonNode node, OutputStream out) throws IOException {
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createGenerator(out)
                .setPrettyPrinter(new DefaultPrettyPrinter());
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeTree(g, node);
        out.close();
    }
}
