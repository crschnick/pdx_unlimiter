package com.paradox_challenges.eu4_unlimiter;

import com.paradox_challenges.eu4_unlimiter.format.NamespaceCreator;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4FileParser;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4IntermediateSavegame;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4NormalParser;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4Savegame;
import com.paradox_challenges.eu4_unlimiter.savegame_mgr.SavegameCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class CommandLine {

    public static void main(String[] args) throws InterruptedException, IOException {
        Eu4Savegame save = Eu4Savegame.fromFile(Paths.get("C:\\Users\\cschn\\Documents\\Paradox Interactive\\Europa Universalis IV\\save games\\namespace.eu4"), true);
        Eu4Savegame saveN = Eu4Savegame.fromFile(Paths.get("C:\\Users\\cschn\\Documents\\Paradox Interactive\\Europa Universalis IV\\save games\\namespace_named.eu4"), true);
        //System.out.println(NamespaceCreator.createNamespace(save, saveN));

        SavegameCache.EU4_CACHE.init();
        SavegameCache.EU4_CACHE.importSavegame(save);

        save.write("C:\\Users\\cschn\\Desktop\\test_eu4\\out1.3_raw.zip", true);
        Eu4IntermediateSavegame i = Eu4IntermediateSavegame.fromSavegame(save);
        i.write("C:\\Users\\cschn\\Desktop\\test_eu4\\out1.3.zip", true);
        //Optional<Node> node = new Eu4NormalParser().parse(new FileInputStream(new File("C:\\Users\\cschn\\Documents\\Paradox Interactive\\Europa Universalis IV\\save games\\test1.3.eu4")));
    }
}
