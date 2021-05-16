package com.crschnick.pdxu.app.core.settings;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.util.ConfigHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SettingsIO {

    public static void load(AbstractSettings s) {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve(s.getName() + ".json");
        JsonNode sNode;
        if (Files.exists(file)) {
            JsonNode node = ConfigHelper.readConfig(file);
            sNode = Optional.ofNullable(node.get("settings")).orElse(JsonNodeFactory.instance.objectNode());
        } else {
            sNode = JsonNodeFactory.instance.objectNode();
        }

        for (var field : s.getClass().getFields()) {
            if (!SettingsEntry.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                SettingsEntry<?> e = (SettingsEntry<?>) field.get(s);
                var node = sNode.get(e.getSerializationName());
                if (node != null) {
                    e.set(node);
                } else {
                    e.setDefault();
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public static void save(AbstractSettings s) {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve(s.getName() + ".json");
        try {
            FileUtils.forceMkdirParent(file.toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ObjectNode i = n.putObject("settings");

        for (var field : s.getClass().getFields()) {
            if (!SettingsEntry.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                SettingsEntry<?> e = (SettingsEntry<?>) field.get(s);
                var node = e.toNode();
                if (node != null) {
                    i.set(e.getSerializationName(), node);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
        ConfigHelper.writeConfig(file, n);
    }
}
