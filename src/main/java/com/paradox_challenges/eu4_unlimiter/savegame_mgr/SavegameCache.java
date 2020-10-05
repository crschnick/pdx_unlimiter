package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4IntermediateSavegame;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4Savegame;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SavegameCache {

    public static final SavegameCache EU4_CACHE = new SavegameCache(Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames", "eu4"));

    private Path path;

    private List<Eu4Campaign> campaigns = new ArrayList<>();

    public SavegameCache(Path path) {
        this.path = path;
    }

    public void init() {
        File dir = path.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        List<Eu4Campaign.Entry> savegames = new ArrayList<>();
        for (File f : dir.listFiles(fileFilter)) {
            Eu4Campaign c = Eu4Campaign.parse(f.toPath());
            campaigns.add(c);
        }
    }

    public Optional<Eu4Campaign> getCampaign(UUID uuid) {
        for (Eu4Campaign c : campaigns) {
            if (c.getCampaignId().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public void importSavegame(Eu4Savegame save) throws IOException {
        Eu4IntermediateSavegame is = Eu4IntermediateSavegame.fromSavegame(save);
        UUID saveUuid = UUID.randomUUID();
        String id = ((ValueNode<String>)Node.getNodeForKey(is.getNodes().get("meta"), "campaign_id")).getValue();
        String file = path.resolve(id).resolve(save.toString() + ".eu4i").toString();
        is.write(file, true);

        UUID uuid = UUID.fromString(id);
        Optional<Eu4Campaign> c = getCampaign(uuid);
        if (!c.isPresent()) {
            campaigns.add(new Eu4Campaign(uuid, new ArrayList<>(List.of(Eu4Campaign.Entry.fromSavegame(is)))));
        } else {
            c.get().add(Eu4Campaign.Entry.fromSavegame(is));
        }
    }
}
