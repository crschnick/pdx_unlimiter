package com.crschnick.pdxu.app.core.settings;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.util.ConfigHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class SettingsIO {


    private static final Logger logger = LoggerFactory.getLogger(SettingsIO.class);

    public static void load(AbstractSettings s) {
        logger.debug("Loading settings " + s.getName() + " ...");
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve(s.getName() + ".json");
        JsonNode sNode;
        boolean exists;
        if (Files.exists(file)) {
            logger.debug("Found settings file " + file);
            JsonNode node = ConfigHelper.readConfig(file);
            sNode = Optional.ofNullable(node.get("settings")).orElse(JsonNodeFactory.instance.objectNode());
            exists = true;
        } else {
            logger.debug("Found no settings");
            sNode = JsonNodeFactory.instance.objectNode();
            exists = false;
        }

        for (var field : s.getClass().getFields()) {
            if (!SettingsEntry.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                SettingsEntry<?> e = (SettingsEntry<?>) field.get(s);
                var node = sNode.get(e.getSerializationName());
                if (node != null) {
                    logger.trace("Entry " + e.getName() + " has a stored value");
                    e.set(node);
                } else {
                    logger.trace("Entry " + e.getName() + " no stored value, defaulting");
                    e.setDefault(exists);
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

        logger.debug("Saving settings " + s.getName() + " to file " + file);
        for (var field : s.getClass().getFields()) {
            if (!SettingsEntry.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                SettingsEntry<?> e = (SettingsEntry<?>) field.get(s);
                var node = e.toNode();
                if (node != null) {
                    logger.trace("Saving entry " + e.getName());
                    i.set(e.getSerializationName(), node);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
        ConfigHelper.writeConfig(file, n);
    }
}
