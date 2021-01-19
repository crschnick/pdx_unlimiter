package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalisationHelper {

    public static Map<String, String> loadTranslations(Path file, Language lang) {
        Path langFile = file.resolveSibling(FilenameUtils.getBaseName(file.toString()) + "_" + lang.id +
                "." + FilenameUtils.getExtension(file.toString()));
        Map<String, String> map = new HashMap<>();
        Pattern p = Pattern.compile("^\\s+([A-Za-z0-9_]+):(\\d*) \"(.+)\"$");
        try {
            Files.lines(langFile).forEach(s -> {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    map.put(m.group(1), m.group(3));
                }
            });
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
        return map;
    }

    public static enum Language {
        ENGLISH("l_english"),
        GERMAN("l_german");

        private String id;

        Language(String id) {
            this.id = id;
        }
    }
}
