package com.crschnick.pdxu.app.installation;


import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameLocalisationHelper {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\w+?\\$");

    public static Map<String, String> loadTranslations(Path file, GameLanguage lang) {
        Path langFile = file.resolveSibling(FilenameUtils.getBaseName(file.toString()) + "_" + lang.getId() +
                "." + FilenameUtils.getExtension(file.toString()));
        return loadTranslations(langFile);
    }

    public static boolean isLanguage(Path file, GameLanguage lang) {
        try {
            try (var reader = new BufferedReader(new FileReader(file.toFile()))) {
                String line = reader.readLine();

                // Check for empty file!
                if (line == null) {
                    return false;
                }

                return line.contains(lang.getId());
            }
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
            return false;
        }
    }

    public static Map<String, String> loadTranslations(Path file) {
        Map<String, String> map = new HashMap<>();
        Pattern p = Pattern.compile("^\\s+([A-Za-z0-9_]+):(\\d*) \"(.+)\"$");

        try (var in = Files.newInputStream(file)) {
            var bin = BOMInputStream.builder().setInputStream(in).setInclude(true).setByteOrderMarks(new ByteOrderMark[]{
                    ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE}).get();
            ByteOrderMark bom = bin.getBOM();
            String charsetName = bom == null ? "UTF-8" : bom.getCharsetName();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(bin, charsetName))) {
                // Skip lang ID
                br.readLine();

                String line;
                while ((line = br.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        map.put(m.group(1), m.group(3));
                    }
                }

            }
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).handle();
            return Map.of();
        }

        return map;
    }

    public static String getValue(String s, String... vars) {
        Objects.requireNonNull(s);

        s = s.replace("\\n", "\n");
        for (var v : vars) {
            v = v != null ? v : "null";
            var matcher = VAR_PATTERN.matcher(s);
            if (matcher.find()) {
                var group = matcher.group();
                s = s.replace(group, v);
            } else {
                TrackEvent.warn("No match found for value " + v + " in string " + s);
            }
        }
        return s;
    }
}
