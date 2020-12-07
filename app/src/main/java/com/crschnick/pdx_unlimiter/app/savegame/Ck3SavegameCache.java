package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameLocalisation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.data.*;
import com.crschnick.pdx_unlimiter.eu4.savegame.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Ck3SavegameCache extends SavegameCache<Ck3RawSavegame, Ck3Savegame, Ck3Tag, Ck3SavegameInfo> {

    public Ck3SavegameCache() {
        super("ck3", "ck3", GameDateType.CK3);
    }

    private Ck3Tag.CoatOfArms readCoa(JsonNode node) {
        Ck3Tag.CoatOfArms coa = new Ck3Tag.CoatOfArms(
                0,
                node.required("pattern").textValue(),
                StreamSupport.stream(node.required("patternColors").spliterator(), false)
                        .map(JsonNode::textValue).collect(Collectors.toList()),

                node.required("emblem").textValue(),
                StreamSupport.stream(node.required("emblemColors").spliterator(), false)
                        .map(JsonNode::textValue).collect(Collectors.toList()));
        return coa;
    }

    @Override
    protected GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date) {
        Ck3Tag.Title title = new Ck3Tag.Title(0, "", readCoa(node));
        return new GameCampaignEntry<>(name, uuid, null, checksum, date, new Ck3Tag(null, List.of(title)));
    }

    @Override
    protected GameCampaign<Ck3Tag, Ck3SavegameInfo> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date) {
        Ck3Tag.Title title = new Ck3Tag.Title(0, "", readCoa(node));
        return new GameCampaign<>(lastPlayed, name, uuid, date, new Ck3Tag(null, List.of(title)));
    }

    private void writeCoa(ObjectNode node, Ck3Tag.CoatOfArms coa) {
        node.put("pattern", coa.getPatternFile());
        node.putArray("patternColors").addAll(coa.getColors().stream()
                .filter(Objects::nonNull)
                .map(TextNode::new)
                .collect(Collectors.toList()));

        node.put("emblem", coa.getEmblemFile());
        node.putArray("emblemColors").addAll(coa.getEmblemColors().stream()
                .filter(Objects::nonNull)
                .map(TextNode::new)
                .collect(Collectors.toList()));
    }

    @Override
    protected void writeEntry(ObjectNode node, GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> e) {
        Ck3Tag tag = e.getTag();
        Ck3Tag.CoatOfArms coa = tag.getPrimaryTitle().getCoatOfArms();
        writeCoa(node, coa);
    }

    @Override
    protected void writeCampaign(ObjectNode node, GameCampaign<Ck3Tag, Ck3SavegameInfo> c) {
        Ck3Tag tag = c.getTag();
        Ck3Tag.CoatOfArms coa = tag.getPrimaryTitle().getCoatOfArms();
        writeCoa(node, coa);
    }

    @Override
    protected GameCampaign<Ck3Tag, Ck3SavegameInfo> createNewCampaignForEntry(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        return new GameCampaign<>(
                Instant.now(),
                entry.getInfo().getTag().getPrimaryTitle().getName(),
                entry.getInfo().getCampaignUuid(),
                entry.getInfo().getDate(),
                entry.getInfo().getTag());
    }

    @Override
    protected GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> createEntry(UUID uuid, String checksum, Ck3SavegameInfo info) {
        return new GameCampaignEntry<Ck3Tag, Ck3SavegameInfo>(
                info.getDate().toDisplayString(),
                uuid,
                info,
                checksum,
                info.getDate(),
                info.getTag());
    }

    @Override
    protected boolean needsUpdate(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> e) {
        Path p = getPath(e);
        int v = 0;
        try {
            v = p.toFile().exists() ? Ck3Savegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < Ck3Savegame.VERSION;
    }

    @Override
    protected Ck3SavegameInfo loadInfo(Ck3Savegame data) throws Exception {
        return Ck3SavegameInfo.fromSavegame(data);
    }

    @Override
    protected Ck3RawSavegame loadRaw(Path p) throws Exception {
        return Ck3RawSavegame.fromFile(p);
    }

    @Override
    protected Ck3Savegame loadDataFromFile(Path p) throws Exception {
        return Ck3Savegame.fromFile(p);
    }

    @Override
    protected Ck3Savegame loadDataFromRaw(Ck3RawSavegame raw) throws Exception {
        return Ck3Savegame.fromSavegame(raw);
    }
}
