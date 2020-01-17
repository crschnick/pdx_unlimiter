package com.paradox_challenges.eu4_unlimiter.format;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Namespace {

    public static final Namespace EU4 = new Namespace("EU4bin", "eu4.txt");

    static {
        EU4.load();
    }

    private String magic;

    private String fileName;

    private Map<Integer, String> keyNames;

    public Namespace(String magic, String name) {
        this.magic = magic;
        this.fileName = name;
        this.keyNames = new HashMap<>();
    }

    public void load() {
        InputStream in = Namespace.class.getResourceAsStream(fileName);
        String strLine = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while( (strLine = reader.readLine()) != null) {
                if (strLine.isEmpty()) {
                    continue;
                }

                String[] parts = strLine.split("=");
                int id = Integer.parseInt(parts[0]);
                String key = parts[1];
                if (keyNames.containsKey(id)) {
                    throw new RuntimeException("Duplicate entries for id " + id + ": " + keyNames.get(id) + ", " + key);
                }

                keyNames.put(id, key);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find the file: fileName");
        } catch (IOException e) {
            System.err.println("Unable to read the file: fileName");
        }
    }

    public String getKeyName(String id) {
        if (Pattern.compile("[0-9]+").matcher(id).matches()) {
            int idInt = Integer.parseInt(id);
            return keyNames.containsKey(idInt) ? keyNames.get(idInt) : "ID[" + id + "]";
        } else {
            return id;
        }
    }
}
