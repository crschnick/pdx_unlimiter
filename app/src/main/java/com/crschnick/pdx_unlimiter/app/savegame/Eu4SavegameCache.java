package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameLocalisation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4RawSavegame;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4Savegame;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class Eu4SavegameCache extends SavegameCache<
        Eu4RawSavegame,
        Eu4Savegame,
        Eu4Tag,
        Eu4SavegameInfo> {

    public Eu4SavegameCache() {
        super("eu4", "eu4", GameDateType.EU4);
    }

    @Override
    protected GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date) {
        String tag = node.required("tag").textValue();
        return new GameCampaignEntry<>(name, uuid, null, checksum, date, new Eu4Tag(tag, 0, 0, Optional.empty()));
    }

    @Override
    protected GameCampaign<Eu4Tag, Eu4SavegameInfo> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date) {
        String tag = node.required("tag").textValue();
        return new GameCampaign<>(lastPlayed, name, uuid, date, new Eu4Tag(tag, 0, 0, Optional.empty()));
    }

    @Override
    protected void writeEntry(ObjectNode node, GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> e) {
        node.put("tag", e.getTag().getTag());
    }

    @Override
    protected void writeCampaign(ObjectNode node, GameCampaign<Eu4Tag, Eu4SavegameInfo> c) {
        node.put("tag", c.getTag().getTag());
    }

    @Override
    protected GameCampaign<Eu4Tag, Eu4SavegameInfo> createNewCampaignForEntry(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry) {
        return new GameCampaign<>(
                Instant.now(),
                GameLocalisation.getTagNameForEntry(entry, entry.getInfo().getTag()),
                entry.getInfo().getCampaignUuid(),
                entry.getInfo().getDate(),
                entry.getInfo().getTag());
    }

    @Override
    protected GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> createEntry(UUID uuid, String checksum, Eu4SavegameInfo info) {
        return new GameCampaignEntry<Eu4Tag, Eu4SavegameInfo>(
                info.getDate().toDisplayString(),
                uuid,
                info,
                checksum,
                info.getDate(),
                info.getTag());
    }

    @Override
    protected boolean needsUpdate(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> e) {
        Path p = getPath(e);
        int v = 0;
        try {
            v = p.toFile().exists() ? Eu4Savegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < Eu4Savegame.VERSION;
    }

    @Override
    protected Eu4SavegameInfo loadInfo(Eu4Savegame data) throws Exception {
        return Eu4SavegameInfo.fromSavegame(data);
    }

    @Override
    protected Eu4RawSavegame loadRaw(Path p) throws Exception {
        return Eu4RawSavegame.fromFile(p);
    }

    @Override
    protected Eu4Savegame loadDataFromFile(Path p) throws Exception {
        return Eu4Savegame.fromFile(p);
    }

    @Override
    protected Eu4Savegame loadDataFromRaw(Eu4RawSavegame raw) throws Exception {
        return Eu4Savegame.fromSavegame(raw);
    }
}
