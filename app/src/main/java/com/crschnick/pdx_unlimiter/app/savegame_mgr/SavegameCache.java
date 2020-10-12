package com.crschnick.pdx_unlimiter.app.savegame_mgr;

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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class SavegameCache {

    public static final SavegameCache EU4_CACHE = new SavegameCache(Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames", "eu4"));

    public static final Path FILE = Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames", "eu4", "campaigns.json");
    public static final Path BACKUP_DIR = Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames", "eu4", "backups");

    public static void loadConfig() throws IOException {
        if (!FILE.toFile().exists()) {
            return;
        }

        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(FILE));

        JsonNode n = node.get("names");
        for (int i = 0; i < n.size(); i++) {
            String name = n.get(i).get("name").textValue();
            String id = n.get(i).get("uuid").textValue();
            EU4_CACHE.addCustomName(UUID.fromString(id), name);
        }

        JsonNode c = node.get("campaigns");
        for (int i = 0; i < c.size(); i++) {
            String id = c.get(i).get("uuid").textValue();
            String tag = c.get(i).get("tag").textValue();
            String name = EU4_CACHE.getNames().getOrDefault(UUID.fromString(id), Installation.EU4.get().getCountryName(tag));
            String lastDate = c.get(i).get("date").textValue();
            String lastPlayed = c.get(i).get("lastPlayed").textValue();
            EU4_CACHE.addFromConfig(tag, name, id, GameDate.fromString(lastDate), Timestamp.valueOf(lastPlayed));
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
            c.add(mapper.createObjectNode()
                    .put("lastPlayed", campaign.getLastPlayed().toString())
                    .put("tag", campaign.getTag())
                    .put("uuid", campaign.getCampaignId().toString())
                    .put("date", campaign.getDate().toString()));
        }
        n.set("campaigns", c);


        ArrayNode names = mapper.createArrayNode();
        for (var e : EU4_CACHE.getNames().entrySet()) {
            names.add(mapper.createObjectNode().put("uuid", e.getKey().toString()).put("name", e.getValue()));
        }
        n.set("names", names);
        mapper.writeTree(generator, n);
        out.close();
    }

    private Path path;

    private Map<UUID,String> names = new HashMap<>();

    private volatile ObservableSet<Eu4Campaign> campaigns = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new TreeSet<>()));

    public SavegameCache(Path path) {
        this.path = path;
    }

    public void addCustomName(UUID id, String name) {
        names.put(id, name);
    }

    public synchronized void addFromConfig(String tag, String name, String uuid, GameDate date, Timestamp lastPlayed) {
        Eu4Campaign c = new Eu4Campaign(new SimpleObjectProperty<>(lastPlayed), new SimpleStringProperty(tag), new SimpleStringProperty(name),
                new SimpleObjectProperty<>(date), UUID.fromString(uuid));
        c.nameProperty().addListener((x, o, n) -> names.put(c.getCampaignId(), n));
        this.campaigns.add(c);
    }

    public synchronized void delete(Eu4Campaign c) {
        if (!this.campaigns.contains(c)) {
            return;
        }

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.names.remove(c.getCampaignId());
        this.campaigns.remove(c);
    }

    public synchronized void add(UUID campainUuid, UUID entryUuid, Eu4SavegameInfo i) throws IOException {
        if (!this.getCampaign(campainUuid).isPresent()) {
            Eu4Campaign c = new Eu4Campaign(new SimpleObjectProperty<>(new Timestamp(System.currentTimeMillis())),
                    new SimpleStringProperty(i.getCurrentTag()),
                    new SimpleStringProperty(Installation.EU4.get().getCountryName(i.getCurrentTag())),
                    new SimpleObjectProperty<>(i.getDate()), campainUuid);
            c.nameProperty().addListener((x, o, n) -> names.put(c.getCampaignId(), n));
            c.getSavegames().addListener((SetChangeListener<? super Eu4Campaign.Entry>) (change) -> {

                    GameDate newValue = change.getSet().stream().map(s -> s.getInfo().getDate()).min(Comparator.naturalOrder()).get();
                    c.dateProperty().setValue(newValue);

                    String newTag = change.getSet().stream().min(Comparator.naturalOrder()).get().getInfo().getCurrentTag();
                    c.tagProperty().setValue(newTag);
            });
            saveConfig();

            this.campaigns.add(c);
        }

        Eu4Campaign c = this.getCampaign(campainUuid).get();
        Eu4Campaign.Entry e = new Eu4Campaign.Entry(new SimpleStringProperty(i.getDate().toDisplayString()), entryUuid, c, i);
        e.nameProperty().addListener((ch, o, n) -> names.put(e.getUuid(), n));
        c.add(e);
    }

    public synchronized void delete(Eu4Campaign.Entry e) {
        Eu4Campaign c = e.getCampaign();
        if (!this.campaigns.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        c.getSavegames().remove(e);
        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.resolve(e.getUuid().toString()).toFile());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.names.remove(e.getUuid());

        if (c.getSavegames().size() == 0) {
            delete(c);
        }
    }

    private synchronized void loadEntry(Path p) {
        try {
            Eu4IntermediateSavegame i = Eu4IntermediateSavegame.fromFile(p, "meta", "countries", "diplomacy");
            UUID id = UUID.fromString(p.getName(p.getNameCount() - 1).toString().split("\\.")[0]);
            this.add(UUID.fromString(p.getName(p.getNameCount() - 2).toString()), id, Eu4SavegameInfo.fromSavegame(i));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void loadAsync(Eu4Campaign c) {
        if (c.isIsLoaded()) {
            return;
        }
        Path campaignPath = path.resolve(c.getCampaignId().toString());
        var files = Arrays.stream(campaignPath.toFile().listFiles())
                .sorted(Comparator.comparingLong(File::lastModified))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        new Thread(() -> {
            for (File f : files) {
                loadEntry(f.toPath());

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            c.isLoadedProperty().setValue(true);
        }).start();
    }

    public synchronized Path getPath(Eu4Campaign.Entry e) {
        Path campaignPath = path.resolve(e.getCampaign().getCampaignId().toString());
        return campaignPath.resolve(e.getUuid().toString());
    }

    public synchronized Optional<Eu4Campaign> getCampaign(UUID uuid) {
        for (Eu4Campaign c : campaigns) {
            if (c.getCampaignId().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized void importSavegame(Path file, Eu4Savegame save) throws IOException {
        Eu4IntermediateSavegame is = Eu4IntermediateSavegame.fromSavegame(save);
        UUID saveUuid = UUID.randomUUID();
        String id = Node.getString(Node.getNodeForKey(is.getNodes().get("meta"), "campaign_id"));
        Path campaignPath = path.resolve(id);
        campaignPath.toFile().mkdirs();
        is.write(campaignPath.resolve(saveUuid.toString()), false);

        FileUtils.copyFile(file.toFile(), BACKUP_DIR.resolve(file.getFileName() + "_" + saveUuid.toString() + ".eu4").toFile());
        FileUtils.moveFile(file.toFile(), campaignPath.resolve(saveUuid.toString()).resolve("savegame.eu4").toFile());

        UUID uuid = UUID.fromString(id);
        Eu4SavegameInfo e = Eu4SavegameInfo.fromSavegame(is);
        this.add(uuid, saveUuid, e);
    }

    public Map<UUID, String> getNames() {
        return names;
    }

    public Path getPath() {
        return path;
    }

    public ObservableSet<Eu4Campaign> getCampaigns() {
        return campaigns;
    }
}
