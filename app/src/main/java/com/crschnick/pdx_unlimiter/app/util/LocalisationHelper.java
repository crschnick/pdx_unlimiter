package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        return loadTranslations(langFile);
    }

    public static boolean isLanguage(Path file, Language lang) {
        try {
            try (var reader = new BufferedReader(new FileReader(file.toFile()))) {
                String line = reader.readLine();
                return line.contains(lang.id);
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            return false;
        }
    }

    public static Map<String, String> loadTranslations(Path file) {
        Map<String, String> map = new HashMap<>();
        Pattern p = Pattern.compile("^\\s+([A-Za-z0-9_]+):(\\d*) \"(.+)\"$");

        try (var in = Files.newInputStream(file)) {
            var bin = new BOMInputStream(in);
            ByteOrderMark bom = bin.getBOM();
            String charsetName = bom == null ? "UTF-8" : bom.getCharsetName();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(bin, charsetName))) {
                // Skip lang ID
                var lang = br.readLine();

                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        map.put(m.group(1), m.group(3));
                    }
                }

            }
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return Map.of();
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
