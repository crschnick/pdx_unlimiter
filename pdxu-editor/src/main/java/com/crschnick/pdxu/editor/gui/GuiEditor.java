package com.crschnick.pdxu.editor.gui;

import atlantafx.base.controls.Spacer;
import atlantafx.base.layout.InputGroup;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.core.window.AppModifiedStage;
import com.crschnick.pdxu.app.core.window.AppSideWindow;
import com.crschnick.pdxu.app.core.window.AppWindowStyle;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.editor.EditorFilter;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.adapter.EditorSavegameAdapter;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.SepiaTone;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class GuiEditor {

    public static Stage createStage(EditorState state) {
        Stage stage = new Stage();
        var title = state.getFileName() + " - " + "Pdx-Unlimiter " + AppI18n.get("editor");
        stage.setTitle(title);
        state.dirtyProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> stage.setTitle((n ? "*" : "") + title));
        });
        var node = GuiEditor.create(state);
        // Disable focus on startup
        node.requestFocus();
        Scene scene = new Scene(node, 720, 600);
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        if (AppMainWindow.get() != null) {
            stage.initOwner(AppMainWindow.get().getStage());
        }
        AppModifiedStage.prepareStage(stage);
        AppWindowStyle.addIcons(stage);
        AppWindowStyle.addStylesheets(scene);
        AppWindowStyle.addNavigationPseudoClasses(scene);

        showMissingGameWarning(state);
        stage.show();
        return stage;
    }

    public static boolean showCloseConfirmAlert(Stage s) {
        var r = AppSideWindow.showBlockingAlert(alert -> {
            alert.setTitle(AppI18n.get("unsavedChanges"));
            alert.setHeaderText(AppI18n.get("unsavedChangesConfirm"));
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.YES);
            alert.getButtonTypes().add(ButtonType.NO);
        }).filter(b -> b.getButtonData().isDefaultButton());

        r.ifPresent(t -> {
            s.close();
        });

        return r.isPresent();
    }

    private static void showMissingGameWarning(EditorState state) {
        if (!state.isContextGameEnabled()) {
            AppSideWindow.showBlockingAlert(alert -> {
                alert.setAlertType(Alert.AlertType.WARNING);
                alert.getDialogPane().setMaxWidth(500);
                alert.setTitle(AppI18n.get("missingGameInstallation"));
                String installationName = state.getFileContext().getGame().getInstallationName();
                alert.setHeaderText(AppI18n.get("missingGameInstallationTips", installationName, installationName));
            });
        }
    }

    private static Region create(EditorState state) {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("editor");
        var v = new VBox();
        v.setFillWidth(true);
        v.setPadding(new Insets(20, 20, 20, 20));
        v.getStyleClass().add("editor-nav-bar-container");
        v.getChildren().add(GuiEditorNavBar.createNavigationBar(state));

        var graphic = new StackPane(new FontIcon("mdi-information-outline"));
        var melterInformation = new Label(
                AppI18n.get("savegameEditorTips"),
                graphic
        );
        melterInformation.setGraphicTextGap(8);
        melterInformation.setAlignment(Pos.CENTER);
        melterInformation.getStyleClass().add("melter-information");
        var topBars = new VBox(
                GuiEditorMenuBar.createMenuBar(state),
                v
        );
        if (!state.isEditable()) {
            topBars.getChildren().add(melterInformation);
        }
        topBars.setFillWidth(true);
        melterInformation.prefWidthProperty().bind(topBars.widthProperty());
        layout.setTop(topBars);
        createNodeList(layout, state);
        layout.setBottom(createFilterBar(state.getFilter()));
        return layout;
    }

    private static void createNodeList(BorderPane pane, EditorState edState) {
        edState.getContent().shownNodesProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var grid = createNodeList(edState);
                ScrollPane sp = new ScrollPane(grid);
                sp.sceneProperty().addListener((ch, ol, ne) -> {
                    if (ne == null) {
                        return;
                    }

                    if (edState.getNavigation().getCurrent().path().getPath().size() > 0) {
                        sp.setVvalue(edState.getContent().getScroll());
                        sp.vvalueProperty().addListener((sc, so, sn) -> {
                            edState.getContent().changeScroll(sn.doubleValue());
                        });
                    }
                });
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                grid.prefWidthProperty().bind(sp.widthProperty());
                pane.setCenter(sp);
            });
        });
    }

    private static StackPane createGridElement(Node child, int row, boolean highlight) {
        var s = new StackPane();
        s.getStyleClass().add("pane");
        if (row % 2 != 0) {
            s.getStyleClass().add("odd");
        }
        s.setAlignment(Pos.CENTER);
        if (child != null) {
            s.getChildren().add(child);
            s.setOpacity(highlight ? 1.0 : 0.6);
        }

        return s;
    }

    public static String getFormattedName(String s) {
        if (s.length() == 0) {
            return s;
        }

        // Ignore formatting for variables
        if (s.startsWith("@")) {
            return s;
        }

        return Arrays.stream(s.split("_"))
                .filter(p -> p.length() > 0)
                .map(p -> p.substring(0, 1).toUpperCase() + p.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static GridPane createNodeList(EditorState state) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add(GuiStyle.CLASS_EDITOR_GRID);
        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), cc,
                new ColumnConstraints(), new ColumnConstraints()
        );

        var alwaysHighlight = true;
        var keyHighlight = state.getFilter().filterKeysProperty().get();
        var valueHighlight = state.getFilter().filterValuesProperty().get();
        var keyValueHighlight = keyHighlight && valueHighlight;

        // Execute everything while the content is fixed
        // to avoid multithreading issues
        state.getContent().withFixedContent(c -> {
            int offset;
            if (c.canGoToPreviousPage()) {
                Button next = new Button("Go to previous page (" + (c.getPage()) + ")");
                next.setOnAction(e -> {
                    c.previousPage();
                });
                HBox box = new HBox(new FontIcon("mdi-arrow-up"), next, new FontIcon("mdi-arrow-up"));
                box.setAlignment(Pos.CENTER);
                grid.add(createGridElement(box, 0, alwaysHighlight), 0, 0, 5, 1);
                offset = 1;
            } else {
                offset = 0;
            }

            var nodes = c.getShownNodes();
            int nodeCount = Math.min(nodes.size(), AppPrefs.get().editorPageSize().getValue());
            for (int i = offset; i < nodeCount + offset; i++) {
                var n = nodes.get(i - offset);
                var label = new Label(getFormattedName(n.getNavigationName()));
                label.setFocusTraversable(true);
                label.setAccessibleRole(AccessibleRole.TEXT);
                label.setAccessibleText(getFormattedName(n.getNavigationName()));
                var kn = createGridElement(label, i, keyHighlight);
                kn.setAlignment(Pos.CENTER_LEFT);

                grid.add(createGridElement(GuiEditorTypes.createTypeNode(n), i, keyHighlight), 0, i);
                grid.add(kn, 1, i);
                grid.add(createGridElement(new Label("="), i, keyValueHighlight), 2, i);

                Region valueDisplay = GuiEditorNode.createValueDisplay(n, state);
                Node tag = null;
                if (n.isReal() && AppPrefs.get().editorEnableNodeTags().getValue()) {
                    try {
                        tag = EditorSavegameAdapter.ALL.get(state.getFileContext().getGame())
                                .createNodeTag(state, (EditorRealNode) n, valueDisplay);
                    } catch (Exception ex) {
                        ErrorEventFactory.fromThrowable(ex).handle();
                    }
                }
                grid.add(createGridElement(Objects.requireNonNullElseGet(tag, Region::new), i, valueHighlight), 3, i);
                grid.add(createGridElement(valueDisplay, i, valueHighlight), 4, i);

                HBox actions = new HBox();
                actions.setFillHeight(true);
                actions.setAlignment(Pos.CENTER_RIGHT);
                if (n.isReal()) {
                    if (AppPrefs.get().editorEnableNodeJumps().getValue()) {
                        try {
                            var pointers = EditorSavegameAdapter.ALL.get(state.getFileContext().getGame())
                                    .createNodeJumps(state, (EditorRealNode) n);
                            if (pointers.size() > 0) {
                                var b = new Button(null, new FontIcon());
                                b.setGraphic(new FontIcon());
                                b.getStyleClass().add("jump-to-def-button");
                                GuiTooltips.install(b, "Jump to " + pointers.stream()
                                        .map(NodePointer::toString)
                                        .collect(Collectors.joining("\nor      ")));
                                b.setOnAction(e -> {
                                    for (var p : pointers) {
                                        if (state.getNavigation().navigateTo(p)) {
                                            break;
                                        }
                                    }
                                });
                                actions.getChildren().add(b);
                                b.prefHeightProperty().bind(actions.heightProperty());
                            }
                        } catch (Exception ex) {
                            ErrorEventFactory.fromThrowable(ex).handle();
                        }
                    }

                    Button edit = new Button(null, new FontIcon());
                    edit.setGraphic(new FontIcon());
                    edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
                    edit.setDisable(!state.isEditable());
                    edit.setOnAction(e -> {
                        state.getExternalState().startEdit(state, (EditorRealNode) n);
                    });
                    GuiTooltips.install(edit, AppI18n.get("editorOpenInExternalEditor"));
                    actions.getChildren().add(edit);
                    edit.prefHeightProperty().bind(actions.heightProperty());
                }
                grid.add(createGridElement(actions, i, valueHighlight), 5, i);

                grid.add(createGridElement(new Region(), i, valueHighlight), 6, i);
            }

            if (c.canGoToNextPage()) {
                Button next = new Button("Go to next page (" + (c.getPage() + 2) + ")");
                next.setOnAction(e -> {
                    c.nextPage();
                });
                HBox box = new HBox(new FontIcon("mdi-arrow-down"), next, new FontIcon("mdi-arrow-down"));
                box.setAlignment(Pos.CENTER);
                grid.add(createGridElement(box, nodeCount + offset, alwaysHighlight), 0, nodeCount + offset, 6, 1);
            }
        });

        return grid;
    }

    private static Region createFilterBar(EditorFilter edFilter) {
        var box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add(GuiStyle.CLASS_EDITOR_FILTER);

        var toggleBar = new InputGroup();
        {
            ToggleButton filterKeys = new ToggleButton();
            filterKeys.setAccessibleText("Include keys in search");
            filterKeys.getStyleClass().add(GuiStyle.CLASS_KEY);
            filterKeys.setGraphic(new FontIcon());
            filterKeys.selectedProperty().bindBidirectional(edFilter.filterKeysProperty());
            GuiTooltips.install(filterKeys, AppI18n.get("editorSearchFilterKeys"));
            toggleBar.getChildren().add(filterKeys);
        }

        {
            ToggleButton filterValues = new ToggleButton();
            filterValues.setAccessibleText("Include values in search");
            filterValues.getStyleClass().add(GuiStyle.CLASS_VALUE);
            filterValues.setGraphic(new FontIcon());
            filterValues.selectedProperty().bindBidirectional(edFilter.filterValuesProperty());
            edFilter.filterValuesProperty().addListener((c, o, n) -> {
                filterValues.setSelected(n);
            });
            GuiTooltips.install(filterValues, AppI18n.get("editorSearchFilterValues"));
            toggleBar.getChildren().add(filterValues);
        }
        box.getChildren().add(toggleBar);

        box.getChildren().add(new Separator(Orientation.VERTICAL));

        var textBar = new InputGroup();
        {
            TextField filter = new TextField();
            {
                filter.setAccessibleText("Filter");
                filter.focusedProperty().addListener((c, o, n) -> {
                    if (n) {
                        Platform.runLater(filter::selectAll);
                    }
                });
                filter.textProperty().addListener((c, o, n) -> {
                    filter.setEffect(new SepiaTone(0.7));
                });
                filter.setOnAction(e -> {
                    edFilter.filterStringProperty().set(filter.getText());
                    filter.setEffect(null);
                });
                textBar.getChildren().add(filter);
            }

            {
                Button search = new Button();
                search.setAccessibleText("Apply filter");
                search.setOnAction(e -> {
                    edFilter.filterStringProperty().set(filter.getText());
                    filter.setEffect(null);
                });
                search.setGraphic(new FontIcon());
                search.getStyleClass().add(GuiStyle.CLASS_FILTER);
                textBar.getChildren().add(search);
                filter.prefHeightProperty().bind(search.heightProperty());
                filter.minHeightProperty().bind(search.heightProperty());
                filter.maxHeightProperty().bind(search.heightProperty());
            }

            {
                Button clear = new Button();
                clear.setAccessibleText("Reset filter");
                clear.setOnAction(e -> {
                    filter.setText("");
                    edFilter.filterStringProperty().set("");
                    filter.setEffect(null);
                });
                clear.setGraphic(new FontIcon());
                clear.getStyleClass().add(GuiStyle.CLASS_CLEAR);
                textBar.getChildren().add(clear);
            }
        }
        box.getChildren().add(textBar);

        box.getChildren().add(new Separator(Orientation.VERTICAL));

        {
            ToggleButton cs = new ToggleButton();
            cs.setAccessibleText("Case sensitive");
            cs.getStyleClass().add(GuiStyle.CLASS_CASE_SENSITIVE);
            cs.setGraphic(new FontIcon());
            cs.selectedProperty().bindBidirectional(edFilter.caseSensitiveProperty());
            GuiTooltips.install(cs, AppI18n.get("editorSearchCaseSensitive"));
            box.getChildren().add(cs);
        }

        box.getChildren().add(new Spacer());

        {
            Label filterDisplay = new Label(null, new FontIcon());
            filterDisplay.setMnemonicParsing(false);
            edFilter.filterStringProperty().addListener((c, o, n) -> {
                Platform.runLater(() -> filterDisplay.setText(
                        n.equals("") ? "" : AppI18n.get("editorSearchResult",n)));
            });
            filterDisplay.setAlignment(Pos.CENTER);
            box.getChildren().add(filterDisplay);
        }

        return box;
    }
}
