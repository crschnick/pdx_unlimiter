package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.eu4.parser.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SavegameCache {

    public static final SavegameCache EU4_CACHE = new SavegameCache(Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames", "eu4"));

    public static final Path FILE = Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames", "eu4", "campaigns.json");

    public static void loadConfig() throws IOException {
        if (!FILE.toFile().exists()) {
            return;
        }

        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(FILE));
        JsonNode c = node.get("campaigns");
        for (int i = 0; i < c.size(); i++) {
            String name = c.get(i).get("name").textValue();
            String id = c.get(i).get("uuid").textValue();
            String lastDate = c.get(i).get("date").textValue();
            String tag = c.get(i).get("tag").textValue();
            EU4_CACHE.add(tag, name, id, lastDate);
        }
    }

    public static void saveConfig() throws IOException {
        FILE.toFile().getParentFile().mkdirs();
        var out = Files.newOutputStream(FILE);
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonGenerator generator = factory.createGenerator(out);
        generator.setPrettyPrinter(new DefaultPrettyPrinter());
        ObjectNode n = mapper.createObjectNode();
        ArrayNode c = mapper.createArrayNode();
        for (Eu4Campaign campaign : EU4_CACHE.getCampaigns()) {
            c.add(mapper.createObjectNode().put("tag", campaign.getTag()).put("name", campaign.getName()).put("uuid", campaign.getCampaignId().toString()).put("date", campaign.getDate().toString()));
        }
        n.set("campaigns", c);
        mapper.writeTree(generator, n);
    }

    private Path path;

    private List<Eu4Campaign> campaigns = new ArrayList<>();

    public SavegameCache(Path path) {
        this.path = path;
    }

    public void add(String tag, String name, String uuid, String date) {
        Eu4Campaign c = new Eu4Campaign(tag, name, GameDate.fromString(date), UUID.fromString(uuid));
        this.campaigns.add(c);
    }

    public void load(Eu4Campaign c) {
        if (c.isLoaded()) {
            return;
        }

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            c.parse(campaignPath);
        } catch (IOException e) {
            e.printStackTrace();
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
            Eu4Campaign.Entry e = Eu4Campaign.Entry.fromSavegame(is, saveUuid);
            Eu4Campaign newC = new Eu4Campaign(e.getCurrentTag(), "name", e.getDate(), uuid);
            newC.add(e);
            campaigns.add(new Eu4Campaign(e.getCurrentTag(), "name", e.getDate(), uuid));
        } else {
            if (!c.get().isLoaded()) {
                c.get().parse(campaignPath);
            }
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
