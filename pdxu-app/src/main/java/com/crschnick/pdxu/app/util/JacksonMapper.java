package com.crschnick.pdxu.app.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class JacksonMapper {

    private static final ObjectMapper BASE = new ObjectMapper();
    private static final ObjectMapper INSTANCE;

    @Getter
    private static boolean init = false;

    static {
        configureBase(BASE);
        INSTANCE = BASE.copy();
    }

    @SuppressWarnings("deprecation")
    private static void configureBase(ObjectMapper objectMapper) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        objectMapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setVisibility(objectMapper
                .getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
    }

    private static List<Module> findModules(ModuleLayer layer) {
        ArrayList<Module> modules = new ArrayList<>();
        ServiceLoader<Module> loader =
                layer != null ? ServiceLoader.load(layer, Module.class) : ServiceLoader.load(Module.class);
        for (Module module : loader) {
            modules.add(module);
        }
        return modules;
    }

    public static ObjectMapper getDefault() {
        if (!JacksonMapper.isInit()) {
            return BASE;
        }

        return INSTANCE;
    }

    public static class Loader implements ModuleLayerLoader {

        @Override
        public void init(ModuleLayer layer) {
            List<Module> modules = findModules(layer);
            INSTANCE.registerModules(modules);
            init = true;
        }
    }
}
