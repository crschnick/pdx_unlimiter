package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.SavegameManagerApp;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.SavegameParseException;
import com.crschnick.pdx_unlimiter.eu4.parser.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class SavegameCache {

    public static final Path ROOT_DIR = Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "savegames");
    public static final SavegameCache EU4_CACHE = new SavegameCache("eu4");

    public static final Set<SavegameCache> CACHES = Set.of(EU4_CACHE);

    public static void loadData() throws IOException {
        for (SavegameCache cache : CACHES) {
            if (cache.getDataFilePath().toFile().exists()) {
                InputStream in = Files.newInputStream(cache.getDataFilePath());
                cache.importDataFromConfig(in);
                in.close();
            }
        }
    }

    public static void saveData() throws IOException {
        for (SavegameCache cache : CACHES) {
            OutputStream out = Files.newOutputStream(cache.getDataFilePath());
            cache.exportDataToConfig(out);
            out.close();
        }
    }

    public static class Status {
        public static enum Type {
            IMPORTING,
            LOADING,
            UPDATING,
            DELETING,
            IMPORTING_ARCHIVE,
            EXPORTING_ARCHIVE
        }

        private Type type;
        private String path;

        public Status(Type type, String path) {
            this.type = type;
            this.path = path;
        }

        public Type getType() {
            return type;
        }

        public String getPath() {
            return path;
        }
    }

    private String name;

    private Path path;

    private ObjectProperty<Optional<Status>> status = new SimpleObjectProperty<>(Optional.empty());

    private volatile ObservableSet<Eu4Campaign> campaigns = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));

    public SavegameCache(String name) {
        this.name = name;
        this.path = ROOT_DIR.resolve(name);
        addChangeListeners();
    }

    private void addChangeListeners() {
        this.campaigns.addListener((SetChangeListener<? super Eu4Campaign>) cc -> {
            if (cc.wasRemoved()) {
                return;
            }

            Eu4Campaign c = cc.getElementAdded();
            c.getSavegames().addListener((SetChangeListener<? super Eu4Campaign.Entry>) (change) -> {
                boolean isNewEntry = change.wasAdded() && change.getElementAdded().getInfo().isPresent();
                boolean wasRemoved = change.wasRemoved();
                if (isNewEntry || wasRemoved) updateCampaignProperties(c);
            });
        });
    }

    private void updateCampaignProperties(Eu4Campaign c) {
        c.getSavegames().stream()
                .filter(s -> s.getInfo().isPresent())
                .map(s -> s.getInfo().get().getDate()).min(Comparator.naturalOrder())
                .ifPresent(d -> c.dateProperty().setValue(d));

        c.getSavegames().stream()
                .filter(s -> s.getInfo().isPresent())
                .min(Comparator.comparingLong(ca -> GameDate.toLong(c.getDate())))
                .ifPresent(e -> e.getInfo().ifPresent(i -> c.tagProperty().setValue(i.getCurrentTag().getTag())));
    }

    public void importDataFromConfig(InputStream in) throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(in.readAllBytes());

        Map<UUID, String> names = new HashMap<>();
        JsonNode n = node.get("names");
        for (int i = 0; i < n.size(); i++) {
            String name = n.get(i).get("name").textValue();
            String id = n.get(i).get("uuid").textValue();
            names.put(UUID.fromString(id), name);
        }

        JsonNode c = node.get("campaigns");
        for (int i = 0; i < c.size(); i++) {
            String id = c.get(i).get("uuid").textValue();
            String tag = c.get(i).get("tag").textValue();
            String name = names.get(UUID.fromString(id));
            String lastDate = c.get(i).get("date").textValue();
            String lastPlayed = c.get(i).get("lastPlayed").textValue();
            Eu4Campaign campaign = addFromConfig(tag, name, id, GameDate.fromString(lastDate), Timestamp.valueOf(lastPlayed));
            for (File entry : getPath().resolve(id).toFile().listFiles()) {
                UUID eId = UUID.fromString(entry.getName());
                campaign.add(new Eu4Campaign.Entry(new SimpleStringProperty(names.get(eId)), eId, campaign));
            }
        }
    }

    public void exportDataToConfig(OutputStream out) throws IOException {
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
        for (Eu4Campaign campaign : campaigns) {
            names.add(mapper.createObjectNode().put("uuid", campaign.getCampaignId().toString()).put("name", campaign.getName()));
            for (Eu4Campaign.Entry entry : campaign.getSavegames()) {
                names.add(mapper.createObjectNode().put("uuid", entry.getUuid().toString()).put("name", entry.getName()));
            }
        }
        n.set("names", names);
        mapper.writeTree(generator, n);
        out.close();
    }

    private synchronized Eu4Campaign addFromConfig(String tag, String name, String uuid, GameDate date, Timestamp lastPlayed) {
        Eu4Campaign c = new Eu4Campaign(new SimpleObjectProperty<>(lastPlayed), new SimpleStringProperty(tag), new SimpleStringProperty(name),
                new SimpleObjectProperty<>(date), UUID.fromString(uuid));
        this.campaigns.add(c);
        return c;
    }


    public static void importSavegameCache(Path in) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(in.toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
            return;
        }

        for (SavegameCache cache : SavegameCache.CACHES) {
            cache.importSavegameCache(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
        }
    }

    public static void exportSavegameCache(Path out)  {
        ZipOutputStream zipFile = null;
        try {
            zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        } catch (FileNotFoundException e) {
            ErrorHandler.handleException(e, false);
            return;
        }

        for (SavegameCache cache : SavegameCache.CACHES) {
            cache.exportSavegameCache(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
        }
    }

    public static void exportSavegameDirectory(Path out) {
        ZipOutputStream zipFile = null;
        try {
            zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        } catch (FileNotFoundException e) {
            ErrorHandler.handleException(e, false);
            return;
        }

        for (SavegameCache cache : SavegameCache.CACHES) {
            cache.exportSavegameDirectory(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
        }
    }

    public void importSavegameCache(ZipFile zipFile) {
        String config = SavegameCache.ROOT_DIR.relativize(getDataFilePath()).toString();
        ZipEntry e = zipFile.getEntry(config);
        try {
            importDataFromConfig(zipFile.getInputStream(e));
        } catch (IOException ioException) {
            ErrorHandler.handleException(ioException, false);
            return;
        }

        statusProperty().setValue(Optional.of(new Status(Status.Type.IMPORTING_ARCHIVE, zipFile.toString())));
        zipFile.stream().filter(en -> !en.getName().equals(config)).forEach(en -> {
            try {
                Path p = SavegameCache.ROOT_DIR.resolve(Paths.get(en.getName()));
                if (p.toFile().exists()) {
                    return;
                }
                FileUtils.copyToFile(zipFile.getInputStream(en), p.toFile());
            } catch (IOException fileNotFoundException) {
                ErrorHandler.handleException(fileNotFoundException, false);
            }
        });
        statusProperty().setValue(Optional.empty());
    }

    private void exportSavegameDirectory(ZipOutputStream out) {
        statusProperty().setValue(Optional.of(new Status(Status.Type.EXPORTING_ARCHIVE, out.toString())));
        Set<String> names = new HashSet<>();
        for (Eu4Campaign c : getCampaigns()) {
            for (Eu4Campaign.Entry e : c.getSavegames()) {
                String name = getEntryName(e);
                if (names.contains(name)) {
                    name += "_" + UUID.randomUUID().toString();
                }
                names.add(name);
                try {
                    compressFileToZipfile(
                            getPath(e).resolve("savegame.eu4").toFile(),
                            SavegameCache.ROOT_DIR.relativize(getPath()).resolve(name + ".eu4").toString(),
                            out);
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException, false);
                }
            }
        }
        statusProperty().setValue(Optional.empty());
    }

    private void exportSavegameCache(ZipOutputStream out) {
        ZipEntry entry = new ZipEntry(SavegameCache.ROOT_DIR.relativize(getDataFilePath()).toString());
        try {
            out.putNextEntry(entry);
            exportDataToConfig(out);
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
            return;
        }

        statusProperty().setValue(Optional.of(new Status(Status.Type.EXPORTING_ARCHIVE, out.toString())));
        for (Eu4Campaign c : getCampaigns()) {
            for (Eu4Campaign.Entry e : c.getSavegames()) {
                Path file = getPath(e).resolve("savegame.eu4");
                String name = SavegameCache.ROOT_DIR.relativize(file).toString();
                try {
                    compressFileToZipfile(file.toFile(), name, out);
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException, false);
                }
            }
        }
        statusProperty().setValue(Optional.empty());
    }

    private void compressFileToZipfile(File file, String name, ZipOutputStream out) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        out.putNextEntry(entry);

        FileInputStream in = new FileInputStream(file);
        IOUtils.copy(in, out);
        in.close();
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
        this.campaigns.remove(c);
    }

    public synchronized void addNewEntry(UUID campainUuid, UUID entryUuid, Eu4SavegameInfo i) {
        if (!this.getCampaign(campainUuid).isPresent()) {
            Eu4Campaign c = new Eu4Campaign(new SimpleObjectProperty<>(new Timestamp(System.currentTimeMillis())),
                    new SimpleStringProperty(i.getCurrentTag().getTag()),
                    new SimpleStringProperty(Installation.EU4.get().getCountryName(i.getCurrentTag())),
                    new SimpleObjectProperty<>(i.getDate()), campainUuid);
            this.campaigns.add(c);
        }

        Eu4Campaign c = this.getCampaign(campainUuid).get();
        Eu4Campaign.Entry e = new Eu4Campaign.Entry(new SimpleStringProperty(i.getDate().toDisplayString()), entryUuid, c, i);
        c.add(e);
    }

    public synchronized void updateAllData() {
        Thread t = new Thread(() -> {
            for (Eu4Campaign c : campaigns) {
                for (Eu4Campaign.Entry e : c.getSavegames()) {
                    if (!SavegameManagerApp.getAPP().isRunning()) {
                        return;
                    }

                    updateSavegameData(e);
                }
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public synchronized void updateSavegameData(Eu4Campaign.Entry e) {
        status.setValue(Optional.of(new Status(Status.Type.UPDATING, getEntryName(e))));

        Path p = getPath(e);
        Path s = p.resolve("savegame.eu4");
        Eu4Savegame save = null;
        try {
            save = Eu4Savegame.fromFile(s);
        } catch (IOException ex) {
            ErrorHandler.handleException(ex, false);
            status.setValue(Optional.empty());
            return;
        }

        Eu4IntermediateSavegame is = null;
        try {
            is = Eu4IntermediateSavegame.fromSavegame(save);
        } catch (SavegameParseException ex) {
            ErrorHandler.handleException(ex, false);
            status.setValue(Optional.empty());
            return;
        }

        for (File f : p.toFile().listFiles((f) -> !f.toPath().equals(s))) {
            if (!f.delete()) {
                try {
                    throw new IOException("Couldn't delete file " + f.toString());
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException, false);
                    status.setValue(Optional.empty());
                    return;
                }
            }
        }

        try {
            is.write(p.resolve("data.zip"), true);
        } catch (IOException ex) {
            ErrorHandler.handleException(ex, false);
            status.setValue(Optional.empty());
            return;
        }

        status.setValue(Optional.empty());
    }

    public synchronized void delete(Eu4Campaign.Entry e) {
        Eu4Campaign c = e.getCampaign();
        if (!this.campaigns.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        status.setValue(Optional.of(new Status(Status.Type.DELETING, getEntryName(e))));

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.resolve(e.getUuid().toString()).toFile());
        } catch (IOException ex) {
            ErrorHandler.handleException(ex, false);
        }

        c.getSavegames().remove(e);
        if (c.getSavegames().size() == 0) {
            delete(c);
        }

        status.setValue(Optional.empty());
    }

    private synchronized void loadEntry(Eu4Campaign.Entry e) {
        if (e.getInfo().isPresent()) {
            return;
        }

        Path p = getPath(e);
        int v = 0;
        try {
            v = p.toFile().exists() ? Eu4IntermediateSavegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (IOException ioException) {
            ErrorHandler.handleException(ioException, false);
            return;
        }

        if (v < Eu4IntermediateSavegame.VERSION) {
            updateSavegameData(e);
        }

        status.setValue(Optional.of(new Status(Status.Type.LOADING, getEntryName(e))));
        Eu4IntermediateSavegame i = null;
        try {
            i = Eu4IntermediateSavegame.fromFile(
                    p.resolve("data.zip"),
                    "countries", "meta", "countries_history", "diplomacy", "active_wars");

            Eu4SavegameInfo info = Eu4SavegameInfo.fromSavegame(i);
            e.infoProperty().setValue(Optional.of(info));
        } catch (Exception ex) {
            ErrorHandler.handleException(ex, false);
            status.setValue(Optional.empty());
            return;
        }

        status.setValue(Optional.empty());
    }

    public synchronized void loadAsync(Eu4Campaign c) {
        if (c.isIsLoaded()) {
            return;
        }

        new Thread(() -> {
            for (Eu4Campaign.Entry e : c.getSavegames()) {
                loadEntry(e);
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

    public synchronized void importSavegame(Path file) {
        status.setValue(Optional.of(new Status(Status.Type.LOADING,
                Installation.EU4.get().getUserDirectory().relativize(file).toString())));

        Eu4IntermediateSavegame is = null;
        try {
            Eu4Savegame save = Eu4Savegame.fromFile(file);
            is = Eu4IntermediateSavegame.fromSavegame(save);
        } catch (Exception e) {
            ErrorHandler.handleException(e, false);
            status.setValue(Optional.empty());
            return;
        }

        UUID saveUuid = UUID.randomUUID();
        String id = Node.getString(Node.getNodeForKey(is.getNodes().get("meta"), "campaign_id"));
        Path campaignPath = path.resolve(id);
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        try {
            if (!entryPath.toFile().mkdirs()) {
                throw new IOException("Couldn't create savegame directory");
            }
            is.write(entryPath.resolve("data.zip"), true);
            FileUtils.copyFile(file.toFile(), getBackupDir().resolve(file.getFileName() + "_" + saveUuid.toString() + ".eu4").toFile());
            FileUtils.moveFile(file.toFile(), entryPath.resolve("savegame.eu4").toFile());


            UUID uuid = UUID.fromString(id);
            Eu4SavegameInfo e = Eu4SavegameInfo.fromSavegame(is);
            this.addNewEntry(uuid, saveUuid, e);
        } catch (Exception e) {
            ErrorHandler.handleException(e, false);
            status.setValue(Optional.empty());
            return;
        }

        status.setValue(Optional.empty());
    }

    public String getEntryName(Eu4Campaign.Entry e) {
        String cn = e.getCampaign().getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public Optional<Status> getStatus() {
        return status.get();
    }

    public ObjectProperty<Optional<Status>> statusProperty() {
        return status;
    }

    public Path getPath() {
        return path;
    }

    public Path getDataFilePath() {
        return getPath().resolve("campaign.json");
    }

    public Path getBackupDir() {
        return ROOT_DIR.resolve("backups");
    }

    public ObservableSet<Eu4Campaign> getCampaigns() {
        return campaigns;
    }
}
