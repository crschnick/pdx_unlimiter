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
import java.util.*;
import java.util.function.Consumer;

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

        JsonNode n = node.get("names");
        for (int i = 0; i < n.size(); i++) {
            String name = n.get(i).get("name").textValue();
            String id = n.get(i).get("uuid").textValue();
            EU4_CACHE.addCustomName(UUID.fromString(id), name);
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


        ArrayNode names = mapper.createArrayNode();
        for (var e : EU4_CACHE.getNames().entrySet()) {
            names.add(mapper.createObjectNode().put("uuid", e.getKey().toString()).put("name", e.getValue()));
        }
        n.set("names", names);
        mapper.writeTree(generator, n);
    }

    private Path path;

    private Map<UUID,String> names = new HashMap<>();

    private List<Eu4Campaign> campaigns = new ArrayList<>();

    private Consumer<Eu4Campaign> campaignAdded;
    private Consumer<Eu4Campaign> campaignRemoved;
    private Consumer<Eu4Campaign.Entry> entryAdded;
    private Consumer<Eu4Campaign.Entry> entryRemoved;

    public SavegameCache(Path path) {
        this.path = path;
    }

    public void setCampaignAdded(Consumer<Eu4Campaign> campaignAdded) {
        this.campaignAdded = campaignAdded;
    }

    public void setCampaignRemoved(Consumer<Eu4Campaign> campaignRemoved) {
        this.campaignRemoved = campaignRemoved;
    }

    public void setEntryAdded(Consumer<Eu4Campaign.Entry> entryAdded) {
        this.entryAdded = entryAdded;
    }

    public void setEntryRemoved(Consumer<Eu4Campaign.Entry> entryRemoved) {
        this.entryRemoved = entryRemoved;
    }

    public void addCustomName(UUID id, String name) {
        names.put(id, name);
    }

    public synchronized void add(String tag, String name, String uuid, String date) {
        Eu4Campaign c = new Eu4Campaign(tag, name, GameDate.fromString(date), UUID.fromString(uuid));
        this.campaigns.add(c);
        campaignAdded.accept(c);
    }

    public synchronized void delete(Eu4Campaign c) {
        if (!this.campaigns.contains(c)) {
            return;
        }

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        campaignPath.toFile().delete();
        this.names.remove(c.getCampaignId());
        this.campaigns.remove(c);
        this.campaignRemoved.accept(c);
    }

    public synchronized void delete(Eu4Campaign c, Eu4Campaign.Entry e) {
        if (!this.campaigns.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        c.getSavegames().remove(e);
        Path campaignPath = path.resolve(c.getCampaignId().toString());
        campaignPath.resolve(e.getSaveId().toString()).toFile().delete();
        this.names.remove(e.getSaveId());
        this.entryRemoved.accept(e);

        if (c.getSavegames().size() == 0) {
            delete(c);
        }
    }

    public void loadAsync(Eu4Campaign c) {
        if (c.getSavegames().size() > 0) {
            return;
        }

        new Thread(() -> {
            Path campaignPath = path.resolve(c.getCampaignId().toString());
            for (File f : campaignPath.toFile().listFiles()) {
                Eu4IntermediateSavegame i = null;
                try {
                    i = Eu4IntermediateSavegame.fromFile(f.toPath(), "meta", "countries", "diplomacy");
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                UUID id = UUID.fromString(f.getName().split("\\.")[0]);
                Eu4Campaign.Entry entry = Eu4Campaign.Entry.fromSavegame(i, id);
                c.add(entry);
                this.entryAdded.accept(entry);

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public synchronized Optional<Eu4Campaign> getCampaign(UUID uuid) {
        for (Eu4Campaign c : campaigns) {
            if (c.getCampaignId().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized void importSavegame(Eu4Savegame save) throws IOException {
        Eu4IntermediateSavegame is = Eu4IntermediateSavegame.fromSavegame(save);
        UUID saveUuid = UUID.randomUUID();
        String id = Node.getString(Node.getNodeForKey(is.getNodes().get("meta"), "campaign_id"));
        Path campaignPath = path.resolve(id);
        campaignPath.toFile().mkdirs();
        is.write(campaignPath.resolve(saveUuid.toString()), false);

        UUID uuid = UUID.fromString(id);
        Optional<Eu4Campaign> c = getCampaign(uuid);
        if (!c.isPresent()) {
            Eu4Campaign.Entry e = Eu4Campaign.Entry.fromSavegame(is, saveUuid);
            Eu4Campaign newC = new Eu4Campaign(e.getCurrentTag(), "name", e.getDate(), uuid);
            newC.add(e);
            campaigns.add(new Eu4Campaign(e.getCurrentTag(), "name", e.getDate(), uuid));
        } else {
            c.get().add(Eu4Campaign.Entry.fromSavegame(is, saveUuid));
        }
    }

    public Map<UUID, String> getNames() {
        return names;
    }

    public Path getPath() {
        return path;
    }

    public List<Eu4Campaign> getCampaigns() {
        return campaigns;
    }
}
