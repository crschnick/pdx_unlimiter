package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.page.PrefsPageComp;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.prefs.AppPrefs;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppLayoutComp extends Comp<AppLayoutComp.Structure> {

    @Override
    public Structure createBase() {
        var model = AppLayoutModel.get();
        Map<Comp<?>, ObservableValue<Boolean>> map = model.getEntries().stream()
                .filter(entry -> entry.comp() != null)
                .collect(Collectors.toMap(
                        entry -> entry.comp(),
                        entry -> Bindings.createBooleanBinding(
                                () -> {
                                    return model.getSelected().getValue().equals(entry);
                                },
                                model.getSelected()),
                        (v1, v2) -> v2,
                        LinkedHashMap::new));
        var multi = new MultiContentComp(map);
        multi.styleClass("background");

        var pane = new BorderPane();
        var sidebar = new SideMenuBarComp(model.getSelected(), model.getEntries(), model.getQueueEntries());
        StackPane multiR = (StackPane) multi.createRegion();
        pane.setCenter(multiR);
        var sidebarR = sidebar.createRegion();
        pane.setLeft(sidebarR);
        model.getSelected().addListener((c, o, n) -> {
            var wasPrefs = o != null && o.comp() instanceof PrefsPageComp;
            if (wasPrefs) {
                AppPrefs.get().save();
            }
        });
        pane.getStyleClass().add("layout");
        return new Structure(pane, multiR, sidebarR, new ArrayList<>(multiR.getChildren()));
    }

    public record Structure(BorderPane pane, StackPane stack, Region sidebar, List<Node> children)
            implements CompStructure<BorderPane> {

        public void prepareAddition() {
            stack.getChildren().clear();
            sidebar.setDisable(true);
        }

        public void show() {
            stack.getChildren().add(children.getFirst());
            for (int i = 1; i < children.size(); i++) {
                children.get(i).setVisible(false);
                children.get(i).setManaged(false);
                stack.getChildren().add(children.get(i));
            }
            PlatformThread.runNestedLoopIteration();
            sidebar.setDisable(false);
        }

        @Override
        public BorderPane get() {
            return pane;
        }
    }
}
