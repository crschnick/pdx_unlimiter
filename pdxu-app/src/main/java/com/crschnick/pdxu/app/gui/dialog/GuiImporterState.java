package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.FileImportTarget;
import com.crschnick.pdxu.app.savegame.SavegameWatcher;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class GuiImporterState {

    private final Game game;
    private final BooleanProperty selectAll;
    private final StringProperty filter;
    private final List<ImportEntry> allTargets;
    private final ListProperty<ImportEntry> shownTargets;

    public GuiImporterState(Game game) {
        this.game = game;
        selectAll = new SimpleBooleanProperty();
        filter = new SimpleStringProperty("");
        allTargets = new CopyOnWriteArrayList<>();
        shownTargets = new SimpleListProperty<>(FXCollections.observableArrayList());
        setupListeners();

        allTargets.addAll(SavegameWatcher.ALL.get(game).getSavegames().stream()
                .map(t -> new ImportEntry(t, new SimpleBooleanProperty(false)))
                .collect(Collectors.toList()));
        updateShownTargets();
    }

    private void setupListeners() {
        filter.addListener((c, o, n) -> {
            updateShownTargets();
        });

        SavegameWatcher.ALL.get(game).savegamesProperty().addListener((c, o, n) -> {
            allTargets.clear();
            allTargets.addAll(n.stream()
                    .map(t -> new ImportEntry(t, new SimpleBooleanProperty(false)))
                    .collect(Collectors.toList()));
            updateShownTargets();
        });

        selectAll.addListener((c, o, n) -> {
            allTargets.forEach(e -> e.selected.set(n));
        });
    }

    private void updateShownTargets() {
        var lowerFilter = filter.get().toLowerCase();
        var previouslyShown = new HashSet<>(shownTargets.get());
        var newShownTargets = new ArrayList<ImportEntry>();
        for (var entry : allTargets) {
            if (shouldShow(entry, lowerFilter)) {
                newShownTargets.add(entry);
            } else if (previouslyShown.contains(entry)) {
                // Unselect when not showing it anymore
                entry.selected.set(false);
            }
        }
        newShownTargets.sort(Comparator.comparing(e -> e.target(), Comparator.reverseOrder()));

        if (!newShownTargets.equals(shownTargets.get())) {
            shownTargets.set(FXCollections.observableArrayList(newShownTargets));
        }
    }

    public Collection<FileImportTarget.StandardImportTarget> getSelectedTargets() {
        return allTargets.stream()
                .filter(e -> e.selected.get())
                .map(e -> e.target)
                .collect(Collectors.toList());
    }

    private boolean shouldShow(ImportEntry entry, String lowerFilter) {
        return entry.target().getName().toLowerCase().contains(lowerFilter)
                && !entry.target.hasImportedSourceFile();
    }

    public BooleanProperty selectAllProperty() {
        return selectAll;
    }

    public String getFilter() {
        return filter.get();
    }

    public StringProperty filterProperty() {
        return filter;
    }

    public List<ImportEntry> getAllTargets() {
        return allTargets;
    }

    public ObservableList<ImportEntry> getShownTargets() {
        return shownTargets.get();
    }

    public ListProperty<ImportEntry> shownTargetsProperty() {
        return shownTargets;
    }

    record ImportEntry(FileImportTarget.StandardImportTarget target, BooleanProperty selected) {}
}
