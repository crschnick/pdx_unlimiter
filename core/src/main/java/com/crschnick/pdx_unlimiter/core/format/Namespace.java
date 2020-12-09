package com.crschnick.pdx_unlimiter.core.format;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Namespace {

    public static final Namespace EMPTY = new EmptyNamespace();
    public static final Namespace CK3 = new FileNamespace("ck3.txt");
    public static final Namespace EU4_GAMESTATE = new FileNamespace("eu4_gamestate.txt");
    public static final Namespace EU4_AI = new FileNamespace("eu4_ai.txt");
    public static final Namespace EU4_META = new FileNamespace("eu4_meta.txt");
    public static final Namespace HOI4 = new FileNamespace("hoi4.txt");

    public abstract String getKeyName(String id);

    public abstract int getKeyOfValue(String value);

    private static class EmptyNamespace extends Namespace {

        @Override
        public String getKeyName(String id) {
            return id;
        }

        @Override
        public int getKeyOfValue(String value) {
            return 0;
        }
    }

    private static class FileNamespace extends Namespace {

        private Map<Integer, String> keyNames;

        private FileNamespace(String name) {
            this.keyNames = new HashMap<>();
            load(name);
        }


        private void load(String fileName) {
            InputStream in = Namespace.class.getResourceAsStream(fileName);
            String strLine = "";
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(Base64.getDecoder().wrap(in)));
                while ((strLine = reader.readLine()) != null) {
                    if (strLine.isEmpty()) {
                        continue;
                    }

                    String[] parts = strLine.split("=");
                    int id = Integer.parseInt(parts[0]);
                    String key = parts[1];
                    if (keyNames.containsKey(id)) {
                        System.err.println("Duplicate entries for id " + id + ": " + keyNames.get(id) + ", " + key);
                    } else {
                        keyNames.put(id, key);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getKeyOfValue(String value) {
            for (var e : keyNames.entrySet()) {
                if (e.getValue().equals(value)) {
                    return e.getKey();
                }
            }

            if (Pattern.compile("[0-9]+").matcher(value).matches()) {
                int idInt = Integer.parseInt(value);
                return idInt;
            }

            throw new RuntimeException("Key not found for " + value);
        }

        public String getKeyName(String id) {
            if (Pattern.compile("[0-9]+").matcher(id).matches()) {
                int idInt = Integer.parseInt(id);
                if (!keyNames.containsKey(idInt)) {
                    //System.err.println("Unable to find name for key " + idInt);
                }
                return keyNames.containsKey(idInt) ? keyNames.get(idInt) : id;
            } else {
                return id;
            }
        }
    }
}
