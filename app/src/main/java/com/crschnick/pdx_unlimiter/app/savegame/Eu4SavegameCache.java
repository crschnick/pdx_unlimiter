package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class Eu4SavegameCache extends SavegameCache<Eu4SavegameInfo, Eu4CampaignEntry, Eu4Campaign> {

    public Eu4SavegameCache() {
        super("eu4");
    }

    @Override
    protected void updateCampaignProperties(Eu4Campaign c) {
        c.getSavegames().stream()
                .filter(s -> s.getInfo().isPresent())
                .map(s -> s.getInfo().get().getDate()).min(Comparator.naturalOrder())
                .ifPresent(d -> c.dateProperty().setValue(d));

        c.getSavegames().stream()
                .filter(s -> s.getInfo().isPresent())
                .min(Comparator.comparingLong(ca -> GameDate.toLong(c.getDate())))
                .ifPresent(e -> e.getInfo().ifPresent(i -> c.tagProperty().setValue(i.getCurrentTag().getTag())));
    }

    @Override
    protected Eu4Campaign createCampaign(Optional<String> name, Eu4SavegameInfo info) {
        return new Eu4Campaign(Instant.now(),
                info.getCurrentTag().getTag()
                , info.getCampaignUuid(), name.orElse(GameInstallation.EU4.getCountryName(info.getCurrentTag())),
                info.getDate());
    }

    @Override
    protected Eu4CampaignEntry createEntry(UUID uuid, Eu4SavegameInfo info) {
        return new Eu4CampaignEntry(
                null, uuid,
                info, info.getCurrentTag().getTag(), info.getDate());
    }

    @Override
    protected boolean needsUpdate(Eu4CampaignEntry eu4CampaignEntry) throws Exception {
        Path p = getPath(eu4CampaignEntry);
        int v = 0;
        try {
            v = p.toFile().exists() ? Eu4IntermediateSavegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < Eu4IntermediateSavegame.VERSION;
    }

    @Override
    protected void loadEntryData(Eu4CampaignEntry eu4CampaignEntry) throws Exception {
        Eu4IntermediateSavegame i = Eu4IntermediateSavegame.fromFile(
                p.resolve("data.zip")
        );

        Eu4SavegameInfo info = Eu4SavegameInfo.fromSavegame(i);
        eu4CampaignEntry.infoProperty().setValue(Optional.of(info));
    }
}
