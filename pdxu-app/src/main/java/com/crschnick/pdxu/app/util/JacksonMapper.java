package com.crschnick.pdxu.app.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class JacksonMapper {

    private static final JsonMapper BASE = create();
    private static final List<JacksonModule> MODULES = new ArrayList<>();
    private static JsonMapper INSTANCE = BASE;

    private static JsonMapper create() {
        JsonMapper.Builder builder = JsonMapper.builder();

        // Reflection config
        builder.changeDefaultVisibility(visibilityChecker -> {
            return visibilityChecker
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE);
        });

        // Write format config
        builder.enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Read config
        builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        return builder.build();
    }

    private static List<JacksonModule> findModules(ModuleLayer layer) {
        ArrayList<JacksonModule> modules = new ArrayList<>();
        ServiceLoader<JacksonModule> loader = layer != null
                ? ServiceLoader.load(layer, JacksonModule.class)
                : ServiceLoader.load(JacksonModule.class);
        for (JacksonModule module : loader) {
            modules.add(module);
        }
        return modules;
    }

    public static synchronized JsonMapper getDefault() {
        return INSTANCE;
    }

    public static class Loader implements ModuleLayerLoader {

        @Override
        public void init(ModuleLayer layer) {
            synchronized (JacksonMapper.class) {
                MODULES.addAll(findModules(layer));
                INSTANCE = BASE.rebuild().addModules(MODULES).build();
            }
        }
    }
}
