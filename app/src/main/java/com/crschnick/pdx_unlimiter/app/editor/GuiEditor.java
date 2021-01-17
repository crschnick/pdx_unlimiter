package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;

public class GuiEditor {

    public static Region create(EditorState state) {
        BorderPane layout = new BorderPane();
        layout.setTop(createNavigationBar(state));
        var grid = createNodeList(state);
        layout.setCenter(grid);
        layout.setBottom(createFilterBar(state.getFilter()));
        return layout;
    }


    private static Region createNavigationBar(EditorState edState) {
        HBox bar = new HBox();
        bar.getStyleClass().add(GuiStyle.CLASS_EDITOR_NAVIGATION);

        Consumer<List<EditorNode>> updateBar = l -> {
            bar.getChildren().clear();
            l.forEach(en -> {
                var btn = new JFXButton(en.navigationName());
                if (!en.equals(l.get(0))) {
                    var sep = new Label(">");
                    sep.setAlignment(Pos.CENTER);
                    bar.getChildren().add(sep);
                }
                btn.setOnAction(e -> {
                    edState.navigateTo(en);
                });
                bar.getChildren().add(btn);
            });
        };
        updateBar.accept(edState.getNodePath());
        edState.nodePathProperty().addListener((c, o, n) -> {
            updateBar.accept(n);
        });

        return bar;
    }


    private static Region createNodeList(EditorState edState) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add(GuiStyle.CLASS_EDITOR_GRID);
        var cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(), new ColumnConstraints(), cc, new ColumnConstraints());

        ScrollPane sp = new ScrollPane(grid);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        grid.prefWidthProperty().bind(sp.widthProperty());
        createNodeList(grid, edState, edState.getContent());
        edState.contentProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                createNodeList(grid, edState, n);
            });
        });

        return sp;
    }

    private static void createNodeList(GridPane grid, EditorState state, List<EditorNode> nodes) {
        grid.getChildren().clear();

        for (int i = 0; i < nodes.size(); i++) {
            var n = nodes.get(i);
            grid.add(new Label(n.displayKeyName()), 0, i);
            grid.add(new Label("="), 1, i);


            Region valueDisplay = null;
            if (n.isReal() && ((SimpleNode) n).getBackingNode() instanceof ValueNode) {
                valueDisplay = new JFXTextField(((SimpleNode) n).getBackingNode().getString());
            } else {
                int length = n.isReal() ? ((SimpleNode) n).getBackingNode().getNodeArray().size() :
                        ((CollectorNode) n).getNodes().size();
                var btn = new JFXButton("[... " + length + " ...]");
                btn.setAlignment(Pos.CENTER);
                btn.setOnAction(e -> {
                    state.navigateTo(n);
                });
                valueDisplay = btn;
            }
            grid.add(valueDisplay, 2, i);

            HBox btns = new HBox();
            Button edit = new JFXButton();
            edit.setGraphic(new FontIcon());
            edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
            edit.setOnAction(e -> {
                state.getExternalState().startEdit(state, n);
            });

            Button del = new JFXButton();
            del.setGraphic(new FontIcon());
            del.getStyleClass().add(GuiStyle.CLASS_DELETE);
            btns.getChildren().add(edit);
            btns.getChildren().add(del);
            grid.add(btns, 3, i);
        }
    }

    private static Region createFilterBar(EditorFilter edFilter) {
        HBox box = new HBox();
        box.getStyleClass().add(GuiStyle.CLASS_EDITOR_FILTER);
        box.setSpacing(8);

        {
            ToggleButton filterKeys = new ToggleButton();
            filterKeys.getStyleClass().add(GuiStyle.CLASS_NEW);
            filterKeys.setGraphic(new FontIcon());
            filterKeys.selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    if (edFilter.getScope() == EditorFilter.Scope.VALUE) {
                        edFilter.scopeProperty().set(EditorFilter.Scope.BOTH);
                    } else {
                        edFilter.scopeProperty().set(EditorFilter.Scope.KEY);
                    }
                } else {
                    if (edFilter.getScope() == EditorFilter.Scope.BOTH) {
                        edFilter.scopeProperty().set(EditorFilter.Scope.VALUE);
                    }
                }
            });
            GuiTooltips.install(filterKeys, "Include keys in search");
            box.getChildren().add(filterKeys);
        }

        {
            ToggleButton filterValues = new ToggleButton();
            filterValues.getStyleClass().add(GuiStyle.CLASS_NEW);
            filterValues.setGraphic(new FontIcon());
            filterValues.selectedProperty().addListener((c, o, n) -> {
                if (n) {
                    if (edFilter.getScope() == EditorFilter.Scope.KEY) {
                        edFilter.scopeProperty().set(EditorFilter.Scope.BOTH);
                    } else {
                        edFilter.scopeProperty().set(EditorFilter.Scope.VALUE);
                    }
                } else {
                    if (edFilter.getScope() == EditorFilter.Scope.BOTH) {
                        edFilter.scopeProperty().set(EditorFilter.Scope.KEY);
                    }
                }
            });
            GuiTooltips.install(filterValues, "Include values in search");
            box.getChildren().add(filterValues);
        }

        box.getChildren().add(new Separator(Orientation.VERTICAL));

        {
            TextField filter = new JFXTextField();
            filter.focusedProperty().addListener((c, o, n) -> {
                if (n) {
                    filter.selectAll();
                }
            });
            filter.textProperty().bindBidirectional(edFilter.filterStringProperty());
            box.getChildren().add(filter);
        }

        {
            ToggleButton cs = new ToggleButton();
            cs.getStyleClass().add(GuiStyle.CLASS_CASE_SENSITIVE);
            cs.setGraphic(new FontIcon());
            cs.selectedProperty().bindBidirectional(edFilter.caseSensitiveProperty());
            GuiTooltips.install(cs, "Case sensitive");
            box.getChildren().add(cs);
        }

        {
            ToggleButton deepSearch = new ToggleButton();
            deepSearch.getStyleClass().add(GuiStyle.CLASS_DEEP_SEARCH);
            deepSearch.setGraphic(new FontIcon());
            deepSearch.selectedProperty().bindBidirectional(edFilter.deepProperty());
            GuiTooltips.install(deepSearch, "Deep search");
            box.getChildren().add(deepSearch);
        }

        return box;
    }
}
