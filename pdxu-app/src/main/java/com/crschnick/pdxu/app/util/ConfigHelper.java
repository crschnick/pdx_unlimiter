package com.crschnick.pdxu.app.util;


import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHelper {

    public static JsonNode readConfig(Path in) {
        JsonNode node = null;
        try {
            if (Files.exists(in)) {
                ObjectMapper o = new ObjectMapper();
                node = o.readTree(Files.readAllBytes(in));
            }
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }
        if (node != null && !node.isMissingNode()) {
            return node;
        }

        ErrorEventFactory.fromMessage("The config file " + in.toString() +
                " could not be read. Trying to revert to a backup").handle();
        var backupFile = in.resolveSibling(
                FilenameUtils.getBaseName(in.toString()) + "_old." + FilenameUtils.getExtension(in.toString()));
        if (Files.exists(backupFile)) {
            ObjectMapper o = new ObjectMapper();
            try {
                node = o.readTree(Files.readAllBytes(backupFile));
            } catch (IOException e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }
        } else {
            ErrorEventFactory.fromMessage("Backup config does not exist. Using blank config").handle();
            return JsonNodeFactory.instance.objectNode();
        }

        if (node != null) {
            return node;
        }

        ErrorEventFactory.fromMessage("The backup config file " + backupFile.toString() +
                " could also not be read.").handle();
        return JsonNodeFactory.instance.objectNode();
    }

    public static void writeConfig(Path out, JsonNode node) {
        try {
            FileUtils.forceMkdirParent(out.toFile());
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
            return;
        }

        String currentContent = "";
        if (Files.exists(out)) {
            try {
                currentContent = Files.readString(out);
            } catch (IOException e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }
        }

        JsonFactory f = new JsonFactory();
        var writer = new StringWriter();
        try (JsonGenerator g = f.createGenerator(writer)
                .setPrettyPrinter(new DefaultPrettyPrinter())) {
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                    .writeTree(g, node);
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
            return;
        }

        var newContent = writer.toString();
        try {
            if (!newContent.equals(currentContent)) {
                var backupFile = out.resolveSibling(
                        FilenameUtils.getBaseName(out.toString()) + "_old." + FilenameUtils.getExtension(out.toString()));
                Files.writeString(backupFile, currentContent);
                Files.writeString(out, newContent);
            }
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }
    }
}
