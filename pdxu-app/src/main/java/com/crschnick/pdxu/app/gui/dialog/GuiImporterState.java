package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.savegame.FileImportTarget;
import com.crschnick.pdxu.app.savegame.SavegameWatcher;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class GuiImporterState {

    private final BooleanProperty selectAll;
    private final StringProperty filter;
    private final List<ImportEntry> allTargets;
    private final ListProperty<ImportEntry> shownTargets;

    public GuiImporterState() {
        selectAll = new SimpleBooleanProperty();
        filter = new SimpleStringProperty("");
        allTargets = new CopyOnWriteArrayList<>();
        shownTargets = new SimpleListProperty<>(FXCollections.observableArrayList());
        setupListeners();

        allTargets.addAll(SavegameWatcher.ALL.get(SavegameManagerState.get().current()).getSavegames().stream()
                .map(t -> new ImportEntry(t, new SimpleBooleanProperty(false)))
                .collect(Collectors.toList()));
        updateShownTargets();
    }

    private void setupListeners() {
        filter.addListener((c, o, n) -> {
            updateShownTargets();
        });

        SavegameWatcher.ALL.get(SavegameManagerState.get().current()).savegamesProperty().addListener((c, o, n) -> {
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
        // Work on copy to reduce list updates
        var newShownTargets = FXCollections.observableArrayList(shownTargets.get());

        // Remove not contained entries
        newShownTargets.removeIf(entry -> !allTargets.contains(entry));

        allTargets.forEach(entry -> {
            if (!newShownTargets.contains(entry) && shouldShow(entry)) {
                newShownTargets.add(entry);
                return;
            }

            if (newShownTargets.contains(entry) && !shouldShow(entry)) {
                // Unselect when not showing it anymore
                entry.selected.set(false);
                newShownTargets.remove(entry);
            }
        });
        newShownTargets.sort(Comparator.comparing(e -> e.target(), Comparator.reverseOrder()));

        shownTargets.set(newShownTargets);
    }

    public Collection<FileImportTarget.StandardImportTarget> getSelectedTargets() {
        return allTargets.stream()
                .filter(e -> e.selected.get())
                .map(e -> e.target).collect(Collectors.toList());
    }

    private boolean shouldShow(ImportEntry entry) {
        return entry.target().getName().toLowerCase().contains(filter.get().toLowerCase()) &&
                !entry.target.hasImportedSourceFile();
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

    public ObservableList<ImportEntry> getShownTargets() {
        return shownTargets.get();
    }

    public ListProperty<ImportEntry> shownTargetsProperty() {
        return shownTargets;
    }

    record ImportEntry(FileImportTarget.StandardImportTarget target, BooleanProperty selected) {
    }
}
