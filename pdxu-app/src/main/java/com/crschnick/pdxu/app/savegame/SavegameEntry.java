package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.model.GameDate;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SavegameEntry<T, I extends SavegameInfo<T>> implements Comparable<SavegameEntry<T, I>> {

    private final ObjectProperty<State> state;
    private final StringProperty name;
    private final UUID uuid;
    private final ObjectProperty<I> info;
    private final String contentChecksum;
    private final GameDate date;
    private final SavegameNotes notes;
    private final List<String> sourceFileChecksums;

    public SavegameEntry(String name, UUID uuid,
                         String contentChecksum, GameDate date, SavegameNotes notes,
                         List<String> sourceFileChecksums) {
        this.state = new SimpleObjectProperty<>(State.INACTIVE);
        this.contentChecksum = contentChecksum;
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
        this.info = new SimpleObjectProperty<>(null);
        this.date = date;
        this.notes = notes;
        this.sourceFileChecksums = new ArrayList<>(sourceFileChecksums);
        this.state.addListener((c,o,n) -> {
            TrackEvent.debug("Changing state of " + this.name.get() + " from " + o + " to " + n);
        });
    }

    public void setActive() {
        if (state.get() != State.INACTIVE) {
            return;
        }

        state.set(State.UNLOADED);
    }

    public void setInactive() {
        if (info.get() != null) {
            unload();
        }
        state.set(State.INACTIVE);
    }

    public void startLoading() {
        if (state.get() == State.INACTIVE) {
            return;
        }

        state.set(State.LOADING);
    }

    public void fail() {
        if (state.get() == State.INACTIVE) {
            return;
        }

        state.set(State.LOAD_FAILED);
    }

    public void load(I newInfo) {
        if (state.get() == State.INACTIVE) {
            return;
        }

        info.set(newInfo);
        state.set(State.LOADED);
    }

    public void unload() {
        info.set(null);
        if (state.get() == State.INACTIVE) {
            return;
        }

        state.set(State.UNLOADED);
    }

    public boolean isLoaded() {
        return state.get() == State.LOADED;
    }

    public boolean canLoad() {
        return state.get().equals(State.UNLOADED);
    }

    @Override
    public int compareTo(SavegameEntry<T, I> o) {
        return o.getDate().compareTo(getDate());
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public I getInfo() {
        return info.get();
    }

    public ReadOnlyObjectProperty<I> infoProperty() {
        return info;
    }

    public String getContentChecksum() {
        return contentChecksum;
    }

    public GameDate getDate() {
        return date;
    }

    public List<String> getSourceFileChecksums() {
        return sourceFileChecksums;
    }

    public void addSourceFileChecksum(String sourceFileChecksum) {
        this.sourceFileChecksums.add(sourceFileChecksum);
    }

    public SavegameNotes getNotes() {
        return notes;
    }

    public State getState() {
        return state.get();
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return state;
    }

    public enum State {
        INACTIVE,
        UNLOADED,
        LOADING,
        LOADED,
        LOAD_FAILED
    }
}
