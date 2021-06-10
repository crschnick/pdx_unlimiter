package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.gui.game.ImageLoader;
import com.crschnick.pdxu.app.util.ConfigHelper;
import com.crschnick.pdxu.app.util.JsonHelper;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class SavegameCampaign<T, I extends SavegameInfo<T>> extends SavegameCollection<T, I> {

    private final Integer branchId;
    private final ObjectProperty<GameDate> date;
    private final ObjectProperty<Image> image;

    public SavegameCampaign(SavegameStorage<T, I> storage, Instant lastPlayed, String name, UUID campaignId, Integer branchId, GameDate date, Image image) {
        super(storage, lastPlayed, name, campaignId);
        if (branchId != null && branchId < 0) {
            throw new IllegalArgumentException();
        }
        this.branchId = branchId;
        this.date = new SimpleObjectProperty<>(date);
        this.image = new SimpleObjectProperty<>(image);
    }

    @Override
    public void deserializeEntries() throws Exception {
        var colFile = getDirectory().resolve("campaign.json");
        if (!Files.exists(colFile)) {
            colFile = getDirectory().resolve("folder.json");
        }

        JsonNode campaignNode = JsonHelper.read(colFile);
        StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
            UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
            String name = Optional.ofNullable(entryNode.get("name")).map(JsonNode::textValue).orElse(null);
            GameDate date = storage.getDateType().fromString(entryNode.required("date").textValue());
            String checksum = entryNode.required("checksum").textValue();
            SavegameNotes notes = SavegameNotes.fromNode(entryNode.get("notes"));
            List<String> sourceFileChecksums = Optional.ofNullable(entryNode.get("sourceFileChecksums"))
                    .map(n -> StreamSupport.stream(n.spliterator(), false)
                            .map(sfc -> sfc.textValue())
                            .collect(Collectors.toList()))
                    .orElse(List.of());
            savegames.add(new SavegameEntry<>(name, eId, checksum, date, notes, sourceFileChecksums));
        });
    }

    @Override
    public void saveData() {
        ObjectNode campaignFileNode = JsonNodeFactory.instance.objectNode();
        ArrayNode entries = campaignFileNode.putArray("entries");
        getSavegames().stream()
                .map(entry -> JsonNodeFactory.instance.objectNode()
                        .put("name", entry.getName())
                        .put("date", entry.getDate().toString())
                        .put("checksum", entry.getContentChecksum())
                        .put("uuid", entry.getUuid().toString())
                        .<ObjectNode>set("sourceFileChecksums", JsonNodeFactory.instance.arrayNode().addAll(
                                entry.getSourceFileChecksums().stream()
                                        .map(s -> new TextNode(s))
                                        .collect(Collectors.toList())))
                        .<ObjectNode>set("notes", SavegameNotes.toNode(entry.getNotes())))
                .forEach(entries::add);

        ConfigHelper.writeConfig(getDirectory().resolve("campaign.json"), campaignFileNode);

        try {
            ImageLoader.writePng(getImage(), getDirectory().resolve("campaign.png"));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public JsonNode serialize() {
        return JsonNodeFactory.instance.objectNode()
                .put("name", getName())
                .put("date", getDate().toString())
                .put("lastPlayed", getLastPlayed().toString())
                .put("uuid", getUuid().toString());
    }

    @Override
    public void copyTo(SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
        var info = entry.getInfo();
        if (info == null) {
            return;
        }

        if (branchId == null) {
            GuiErrorReporter.showSimpleErrorMessage("Can't move savegame to campaign, since the campaign is not a branch.\nPlease create a new branch to allow for moving.");
            return;
        }

        if (info.isIronman()) {
            var srcDir = storage.getSavegameDataDirectory(entry).toFile();
            try {
                FileUtils.copyDirectory(
                        srcDir,
                        storage.getSavegameDataDirectory().resolve(getUuid().toString())
                                .resolve(entry.getUuid().toString()).toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
                return;
            }
        } else if (!info.isBinary()) {
            try {
                Files.createDirectory(storage.getSavegameDataDirectory()
                        .resolve(getUuid().toString()).resolve(entry.getUuid().toString()));

                FileUtils.copyDirectory(
                        srcDir,
                        getSavegameDataDirectory().resolve(to.getUuid().toString()).resolve(entry.getUuid().toString()).toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
                return;
            }
        } else {
            GuiErrorReporter.showSimpleErrorMessage("Can't move a binary non-ironman savegame.");
        }

        getSavegames().add(entry);
        onSavegamesChange();
    }

    public synchronized void addNewEntryn(
            String checksum,
            I info,
            String name,
            String sourceFileChecksum) {
        SavegameEntry<T, I> e = new SavegameEntry<>(
                name != null ? name : storage.getDefaultEntryName(info),
                UUID.randomUUID(),
                checksum,
                info.getDate(),
                SavegameNotes.empty(),
                sourceFileChecksum != null ? List.of(sourceFileChecksum) : List.of());
        this.savegames.add(e);
        onSavegamesChange();
    }

    @Override
    public String getOutputName(String fileName, String entryName) {
        return fileName + (isBranch() ? ".branch-" + getBranchId() : "");
    }

    @Override
    public void onSavegameLoad(SavegameEntry<T, I> entry) {
        if (entry == getLatestEntry()) {
            imageProperty().set(SavegameActions.createImageForEntry(entry));
            updateDate();
        }
    }

    @Override
    public void onSavegamesChange() {
        updateDate();
    }

    private void updateDate() {
        getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.naturalOrder())
                .map(s -> s.getInfo().getDate())
                .ifPresent(d -> dateProperty().setValue(d));
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public SavegameEntry<T, I> getLatestEntry() {
        return entryStream().findFirst().get();
    }

    public Stream<SavegameEntry<T, I>> entryStream() {
        var list = new ArrayList<>(getSavegames());
        list.sort(Comparator.comparing(SavegameEntry::getDate));
        Collections.reverse(list);
        return list.stream();
    }

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
    }

    public boolean isBranch() {
        return branchId != null;
    }

    public int getBranchId() {
        return branchId;
    }
}
