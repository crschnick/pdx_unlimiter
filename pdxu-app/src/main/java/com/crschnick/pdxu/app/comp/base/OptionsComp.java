package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import com.crschnick.pdxu.app.platform.BindingsHelper;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import atlantafx.base.controls.Spacer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OptionsComp extends Comp<CompStructure<VBox>> {

    private final List<Entry> entries;

    public OptionsComp(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public CompStructure<VBox> createBase() {
        VBox pane = new VBox();
        pane.getStyleClass().add("options-comp");

        var nameRegions = new ArrayList<Region>();

        Region firstComp = null;
        for (var entry : getEntries()) {
            Region compRegion = entry.comp() != null ? entry.comp().createRegion() : new Region();

            if (firstComp == null) {
                compRegion.getStyleClass().add("first");
                firstComp = compRegion;
            }

            var showVertical = (entry.name() != null && (entry.description() != null));
            if (showVertical) {
                var line = new VBox();
                line.prefWidthProperty().bind(pane.widthProperty());

                var name = new Label();
                name.getStyleClass().add("name");
                name.textProperty().bind(entry.name());
                name.setMinWidth(Region.USE_PREF_SIZE);
                name.setMinHeight(Region.USE_PREF_SIZE);
                name.setAlignment(Pos.CENTER_LEFT);
                VBox.setVgrow(line, VBox.getVgrow(compRegion));
                line.spacingProperty()
                        .bind(Bindings.createDoubleBinding(
                                () -> {
                                    return name.isManaged() ? 2.0 : 0.0;
                                },
                                name.managedProperty()));
                name.visibleProperty().bind(compRegion.visibleProperty());
                name.managedProperty().bind(compRegion.managedProperty());
                line.getChildren().add(name);
                VBox.setMargin(name, new Insets(0, 0, 0, 1));

                var description = new Label();
                description.setWrapText(true);
                description.getStyleClass().add("description");
                description.textProperty().bind(entry.description());
                description.setAlignment(Pos.CENTER_LEFT);
                description.setMinHeight(Region.USE_PREF_SIZE);
                description.visibleProperty().bind(compRegion.visibleProperty());
                description.managedProperty().bind(compRegion.managedProperty());

                line.getChildren().add(description);
                line.getChildren().add(new Spacer(2, Orientation.VERTICAL));
                VBox.setMargin(description, new Insets(0, 0, 0, 1));

                compRegion.accessibleTextProperty().bind(name.textProperty());
                compRegion.accessibleHelpProperty().bind(entry.description());
                line.getChildren().add(compRegion);
                compRegion.getStyleClass().add("options-content");

                pane.getChildren().add(line);
            } else if (entry.name() != null) {
                var line = new HBox();
                line.setFillHeight(true);
                line.prefWidthProperty().bind(pane.widthProperty());
                line.setSpacing(8);

                var name = new Label();
                name.textProperty().bind(entry.name());
                name.prefHeightProperty().bind(line.heightProperty());
                name.setMinWidth(Region.USE_PREF_SIZE);
                name.setAlignment(Pos.CENTER_LEFT);
                if (compRegion != null) {
                    name.visibleProperty().bind(compRegion.visibleProperty());
                    name.managedProperty().bind(compRegion.managedProperty());
                }
                nameRegions.add(name);
                line.getChildren().add(name);

                if (compRegion != null) {
                    compRegion.accessibleTextProperty().bind(name.textProperty());
                    line.getChildren().add(compRegion);
                    HBox.setHgrow(compRegion, Priority.ALWAYS);
                }

                pane.getChildren().add(line);
            } else {
                if (compRegion != null) {
                    pane.getChildren().add(compRegion);
                }
            }

            var last = entry.equals(entries.getLast());
            if (!last) {
                Spacer spacer = new Spacer(7, Orientation.VERTICAL);
                pane.getChildren().add(spacer);
                if (compRegion != null) {
                    spacer.visibleProperty().bind(compRegion.visibleProperty());
                    spacer.managedProperty().bind(compRegion.managedProperty());
                }
            }
        }

        if (entries.size() == 1 && firstComp != null) {
            firstComp.visibleProperty().subscribe(v -> {
                pane.setVisible(v);
            });
            firstComp.managedProperty().subscribe(v -> {
                pane.setManaged(v);
            });
        }

        for (Region nameRegion : nameRegions) {
            nameRegion.setPrefWidth(Region.USE_COMPUTED_SIZE);
        }

        if (entries.stream().anyMatch(entry -> entry.name() != null && entry.description() == null)) {
            var nameWidthBinding = Bindings.createDoubleBinding(
                    () -> {
                        return nameRegions.stream()
                                .map(Region::getWidth)
                                .filter(aDouble -> aDouble > 0.0)
                                .max(Double::compareTo)
                                .orElse(Region.USE_COMPUTED_SIZE);
                    },
                    nameRegions.stream().map(Region::widthProperty).toList().toArray(new Observable[0]));
            BindingsHelper.preserve(pane, nameWidthBinding);
            nameWidthBinding.addListener((observableValue, number, t1) -> {
                Platform.runLater(() -> {
                    for (Region nameRegion : nameRegions) {
                        nameRegion.setPrefWidth(t1.doubleValue());
                    }
                });
            });
        }

        Region finalFirstComp = firstComp;
        pane.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                return;
            }

            if (finalFirstComp != null) {
                finalFirstComp.requestFocus();
            }
        });

        return new SimpleCompStructure<>(pane);
    }

    public record Entry(String key, ObservableValue<String> description, ObservableValue<String> name, Comp<?> comp) {}
}
