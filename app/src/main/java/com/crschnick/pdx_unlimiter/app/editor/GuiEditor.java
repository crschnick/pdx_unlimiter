package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class GuiEditor {

    static Stage createStage(EditorState state) {
        Stage stage = new Stage();

        var icon = PdxuApp.getApp().getIcon();
        stage.getIcons().add(icon);
        var title = state.getFileName() + " - " + "Pdx-Unlimiter Editor";
        stage.setTitle(title);
        state.dirtyProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> stage.setTitle((n ? "*" : "") + title));
        });

        stage.setScene(new Scene(GuiEditor.create(state), 720, 600));
        GuiStyle.addStylesheets(stage.getScene());
        stage.show();
        return stage;
    }

    private static Region create(EditorState state) {
        BorderPane layout = new BorderPane();
        layout.setTop(createNavigationBar(state));
        createNodeList(layout, state);
        layout.setBottom(createFilterBar(state.getFilter()));
        return layout;
    }

    private static Region createNavigationBar(EditorState edState) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add(GuiStyle.CLASS_EDITOR_NAVIGATION);

        Consumer<List<EditorState.NavEntry>> updateBar = l -> {
            Platform.runLater(() -> {
                bar.getChildren().clear();

                {
                    Button save = new Button();
                    save.setDisable(!edState.dirtyProperty().get());
                    edState.dirtyProperty().addListener((c, o, n) -> {
                        save.setDisable(!n);
                    });
                    save.setGraphic(new FontIcon());
                    save.getStyleClass().add(GuiStyle.CLASS_SAVE);
                    save.setOnAction(e -> {
                        edState.save();
                    });
                    bar.getChildren().add(save);
                }

                {
                    var initBtn = new JFXButton(edState.getFileName());
                    initBtn.setOnAction(e -> {
                        edState.navigateTo(null);
                    });
                    bar.getChildren().add(initBtn);
                }

                l.forEach(en -> {
                    var btn = new JFXButton(en.getEditorNode().navigationName());
                    btn.setMnemonicParsing(false);
                    {
                        var sep = new Label(">");
                        sep.setAlignment(Pos.CENTER);
                        bar.getChildren().add(sep);
                    }
                    btn.setOnAction(e -> {
                        edState.navigateTo(en.getEditorNode());
                    });
                    bar.getChildren().add(btn);
                });

                if (l.size() > 0) {
                    Button edit = new JFXButton();
                    edit.setGraphic(new FontIcon());
                    edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
                    edit.setOnAction(e -> {
                        edState.getExternalState().startEdit(edState, l.get(l.size() - 1).getEditorNode());
                    });
                    edit.setPadding(new Insets(4, 4, 2, 4));
                    bar.getChildren().add(edit);
                }
            });
        };
        updateBar.accept(edState.getNavPath());
        edState.navPathProperty().addListener((c, o, n) -> {
            updateBar.accept(n);
        });

        return bar;
    }


    private static void createNodeList(BorderPane pane, EditorState edState) {
        edState.contentProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var grid = createNodeList(edState, n);
                ScrollPane sp = new ScrollPane(grid);
                sp.sceneProperty().addListener((ch, ol, ne) -> {
                    if (ne == null) {
                        return;
                    }
                    if (edState.getNavPath().size() > 0) {
                        sp.setVvalue(edState.getNavPath().get(edState.getNavPath().size() - 1).getScroll());
                        edState.getNavPath().get(edState.getNavPath().size() - 1).scrollProperty().bind(sp.vvalueProperty());
                    }
                });
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                grid.prefWidthProperty().bind(sp.widthProperty());
                pane.setCenter(sp);
            });
        });
    }

    private static StackPane createGridElement(Region child, int row) {
        var s = new StackPane();
        s.getStyleClass().add("pane");
        if (row % 2 != 0) {
            s.getStyleClass().add("odd");
        }
        s.setAlignment(Pos.CENTER);
        if (child != null) {
            s.getChildren().add(child);
        }
        return s;
    }

    private static GridPane createNodeList(EditorState state, List<EditorNode> nodes) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add(GuiStyle.CLASS_EDITOR_GRID);
        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(), new ColumnConstraints(), new ColumnConstraints(), cc,
                new ColumnConstraints(), new ColumnConstraints());

        int nodeCount = Math.min(nodes.size(), 100);
        for (int i = 0; i < nodeCount; i++) {
            boolean odd = i % 2 != 0;

            var n = nodes.get(i);
            var kn = createGridElement(new Label(n.displayKeyName()), i);
            kn.setAlignment(Pos.CENTER_LEFT);

            grid.add(kn, 0, i);
            grid.add(createGridElement(new Label("="), i), 1, i);
            grid.add(createGridElement(GuiEditorNode.createTypeNode(n).orElse(null), i), 2, i);
            grid.add(createGridElement(GuiEditorNode.createValueDisplay(n, state), i), 3, i);

            if (n.getDirectParent() != null) {
                HBox actions = new HBox();
                actions.setFillHeight(true);

                Button edit = new JFXButton();
                edit.setGraphic(new FontIcon());
                edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
                edit.setOnAction(e -> {
                    state.getExternalState().startEdit(state, n);
                });
                actions.getChildren().add(edit);
                edit.prefHeightProperty().bind(actions.heightProperty());

                Button del = new JFXButton();
                del.setGraphic(new FontIcon());
                del.getStyleClass().add(GuiStyle.CLASS_DELETE);
                del.setOnAction(e -> {
                    n.delete();
                    state.onDelete();
                });
                actions.getChildren().add(del);
                del.prefHeightProperty().bind(actions.heightProperty());

                grid.add(createGridElement(actions, i), 4, i);
            }

            grid.add(createGridElement(new Region(), i), 5, i);
        }
        return grid;
    }

    private static Region createFilterBar(EditorFilter edFilter) {
        HBox box = new HBox();
        box.getStyleClass().add(GuiStyle.CLASS_EDITOR_FILTER);
        box.setSpacing(8);

        {
            ToggleButton filterKeys = new ToggleButton();
            filterKeys.getStyleClass().add(GuiStyle.CLASS_KEY);
            filterKeys.setGraphic(new FontIcon());
            filterKeys.selectedProperty().bindBidirectional(edFilter.filterKeysProperty());
            GuiTooltips.install(filterKeys, "Include keys in search");
            box.getChildren().add(filterKeys);
        }

        {
            ToggleButton filterValues = new ToggleButton();
            filterValues.getStyleClass().add(GuiStyle.CLASS_VALUE);
            filterValues.setGraphic(new FontIcon());
            filterValues.selectedProperty().bindBidirectional(edFilter.filterValuesProperty());
            edFilter.filterValuesProperty().addListener((c, o, n) -> {
                filterValues.setSelected(n);
            });
            GuiTooltips.install(filterValues, "Include values in search");
            box.getChildren().add(filterValues);
        }

        box.getChildren().add(new Separator(Orientation.VERTICAL));

        HBox textBar = new HBox();
        {
            TextField filter = new TextField();
            {
                filter.focusedProperty().addListener((c, o, n) -> {
                    if (n) {
                        Platform.runLater(filter::selectAll);
                    }
                });
                filter.textProperty().addListener((c,o,n) -> {
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
                search.setOnAction(e -> {
                    edFilter.filterStringProperty().set(filter.getText());
                    filter.setEffect(null);
                });
                search.setGraphic(new FontIcon());
                search.getStyleClass().add(GuiStyle.CLASS_FILTER);
                textBar.getChildren().add(search);
            }

            {
                Button clear = new Button();
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
            cs.getStyleClass().add(GuiStyle.CLASS_CASE_SENSITIVE);
            cs.setGraphic(new FontIcon());
            cs.selectedProperty().bindBidirectional(edFilter.caseSensitiveProperty());
            GuiTooltips.install(cs, "Case sensitive");
            box.getChildren().add(cs);
        }

        Region spacer = new Region();
        box.getChildren().add(spacer);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        {
            Button filterDisplay = new JFXButton();
            edFilter.filterStringProperty().addListener((c, o, n) -> {
                Platform.runLater(() -> filterDisplay.setText(
                        n.equals("") ? "" : "Showing results for \"" + n + "\""));
            });
            filterDisplay.setAlignment(Pos.CENTER);
            box.getChildren().add(filterDisplay);
            //filterDisplay.prefHeightProperty().bind(box.prefHeightProperty());
        }

        return box;
    }
}
