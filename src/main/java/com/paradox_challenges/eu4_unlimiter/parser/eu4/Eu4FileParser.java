package com.paradox_challenges.eu4_unlimiter.parser.eu4;

import com.paradox_challenges.eu4_unlimiter.format.Namespace;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.GamedataParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Eu4FileParser {

    private static final GamedataParser normalParser = new Eu4NormalParser();

    private static ZipEntry getEntryByName(String name, ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    public static Eu4Savegame parse(Path file) throws IOException {
        boolean isZipped = new ZipInputStream(Files.newInputStream(file)).getNextEntry() != null;
        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ZipEntry gamestate = getEntryByName("gamestate", zipFile);
            ZipEntry meta = getEntryByName("meta", zipFile);
            ZipEntry ai = getEntryByName("ai", zipFile);

            Optional<Node> gamestateNode = new Eu4IronmanParser(Namespace.EU4_GAMESTATE).parse(zipFile.getInputStream(gamestate));
            if (gamestateNode.isPresent()) {
                Node metaNode = new Eu4IronmanParser(Namespace.EU4_META).parse(zipFile.getInputStream(meta)).get();
                Node aiNode = new Eu4IronmanParser(Namespace.EU4_AI).parse(zipFile.getInputStream(ai)).get();
                return new Eu4Savegame(gamestateNode.get(), metaNode, aiNode);
            }
        } else {

        }
        return null;
    }
}
