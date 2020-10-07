package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

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

    public void init() throws IOException {
        File dir = path.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

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
        String id = Node.getString(Node.getNodeForKey(is.getNodes().get("meta"), "campaign_id"));
        Path campaignPath = path.resolve(id);
        campaignPath.toFile().mkdirs();
        String file = campaignPath.resolve(saveUuid.toString() + ".eu4i").toString();
        is.write(file, true);

        UUID uuid = UUID.fromString(id);
        Optional<Eu4Campaign> c = getCampaign(uuid);
        if (!c.isPresent()) {
            campaigns.add(new Eu4Campaign(uuid, new ArrayList<>(List.of(Eu4Campaign.Entry.fromSavegame(is, saveUuid)))));
        } else {
            c.get().add(Eu4Campaign.Entry.fromSavegame(is, saveUuid));
        }
    }

    public Path getPath() {
        return path;
    }

    public List<Eu4Campaign> getCampaigns() {
        return campaigns;
    }
}
