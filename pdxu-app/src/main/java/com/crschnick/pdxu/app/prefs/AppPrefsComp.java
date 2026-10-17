package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.comp.base.VerticalComp;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.util.BooleanScope;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public class AppPrefsComp extends SimpleComp {

    private final Map<AppPrefsCategory, Node> categoryNodes = new HashMap<>();

    @Override
    protected Region createSimple() {
        var categories = AppPrefs.get().getCategories().stream()
                .filter(appPrefsCategory -> appPrefsCategory.show())
                .toList();
        var list = categories.stream()
                .<Comp<?>>map(appPrefsCategory -> {
                    var r = appPrefsCategory
                            .create()
                            .styleClass("prefs-container")
                            .styleClass(appPrefsCategory.getId());
                    return r;
                })
                .toList();
        var box = new VerticalComp(list)
                .maxWidth(850)
                .styleClass("prefs-box")
                .createStructure()
                .get();
        var scrollPane = new ScrollPane(box);

        var externalUpdate = new SimpleBooleanProperty();

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (externalUpdate.get()) {
                return;
            }

            BooleanScope.executeExclusive(externalUpdate, () -> {
                var offset = newValue.doubleValue();
                if (offset == 1.0) {
                    AppPrefs.get().getSelectedCategory().setValue(categories.getLast());
                    return;
                }

                for (int i = categories.size() - 1; i >= 0; i--) {
                    var category = categories.get(i);
                    var min = computeCategoryOffset(box, scrollPane, category);
                    if (offset + (100.0 / box.getHeight()) > min) {
                        AppPrefs.get().getSelectedCategory().setValue(category);
                        return;
                    }
                }
            });
        });

        AppPrefs.get().getSelectedCategory().addListener((observable, oldValue, val) -> {
            if (val == null) {
                return;
            }

            PlatformThread.runLaterIfNeeded(() -> {
                if (externalUpdate.get()) {
                    return;
                }

                BooleanScope.executeExclusive(externalUpdate, () -> {
                    // This value is off initially if we haven't opened the settings before
                    // Perhaps it's the layout that is not done yet?
                    var off = computeCategoryOffset(box, scrollPane, val);
                    scrollPane.setVvalue(off);
                });
            });
        });
        scrollPane.setFitToWidth(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        var sidebar = new AppPrefsSidebarComp().createRegion();
        sidebar.setMinWidth(210);
        sidebar.setPrefWidth(210);
        sidebar.setMaxWidth(210);
        sidebar.setMinHeight(0);

        var split = new HBox(sidebar, scrollPane);
        HBox.setMargin(sidebar, new Insets(4));
        split.setFillHeight(true);
        split.getStyleClass().add("prefs");
        var stack = new StackPane(split);
        return stack;
    }

    private Node findCategoryNode(Parent box, AppPrefsCategory val) {
        // This is called for every category on every scroll change, so avoid a full lookup each time
        var cached = categoryNodes.get(val);
        if (cached != null && cached.getScene() != null) {
            return cached;
        }

        var found = box.lookup("." + val.getId());
        if (found != null) {
            categoryNodes.put(val, found);
        } else {
            categoryNodes.remove(val);
        }
        return found;
    }

    private double computeCategoryOffset(VBox box, ScrollPane scrollPane, AppPrefsCategory val) {
        var node = val != null ? findCategoryNode(box, val) : null;
        if (node != null && scrollPane.getHeight() > 0.0) {
            var minY = node.getBoundsInParent().getMinY();
            if (minY <= 40.0) {
                minY = 0.0;
            }
            var s = Math.min(box.getHeight(), minY > 0.0 ? minY + 20 : 0.0) / box.getHeight();
            var off = (scrollPane.getHeight() * s * 1.02) / box.getHeight();
            return s + off;
        } else {
            return 0;
        }
    }
}
