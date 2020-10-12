package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.installation.WindowsRegistry;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameManagerApp;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import javafx.application.Application;
import net.nikr.dds.DDSImageReaderSpi;

import javax.imageio.spi.ImageReaderSpi;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        try {
            Installation.loadConfig();

            Eu4Savegame save = Eu4Savegame.fromFile(Paths.get("C:\\Users\\cschn\\pdx_unlimiter\\savegames\\eu4\\backups\\swe1.eu4_79a6c3b2-edbc-404e-950f-c3effbc3c126.eu4"));
            //Eu4Savegame saveN = Eu4Savegame.fromFile(Paths.get("C:\\Users\\cschn\\Documents\\Paradox Interactive\\Europa Universalis IV\\save games\\namespace_named.eu4"), true);
            //System.out.println(NamespaceCreator.createNamespace(save, saveN));

            Eu4IntermediateSavegame s = Eu4IntermediateSavegame.fromSavegame(save);
            s.write(Paths.get("C:\\Users\\cschn\\Desktop\\test_eu4\\rawout"), false);
            //save.write("C:\\Users\\cschn\\Desktop\\test_eu4\\29.raw.zip", true);

            //SavegameCache.EU4_CACHE.importSavegame(Paths.get("C:\\Users\\cschn\\pdx_unlimiter\\savegames\\eu4\\backups\\swe1.eu4_79a6c3b2-edbc-404e-950f-c3effbc3c126.eu4"), save);

            SavegameCache.loadConfig();
            SavegameManagerApp.main(args);

            SavegameCache.saveConfig();
            Installation.saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Eu4IntermediateSavegame i = Eu4IntermediateSavegame.fromSavegame(save);
        //i.write("C:\\Users\\cschn\\Desktop\\test_eu4\\out1.3.zip", true);
        //Optional<Node> node = new Eu4NormalParser().parse(new FileInputStream(new File("C:\\Users\\cschn\\Documents\\Paradox Interactive\\Europa Universalis IV\\save games\\test1.3.eu4")));
    }
}
