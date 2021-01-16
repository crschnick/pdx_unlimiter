package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class GuiEditor {

    public static Region create(EditorState state) {
        BorderPane layout = new BorderPane();
        layout.setTop(createNavigationBar(state));
        layout.setCenter(createNodeList(state));
        layout.setBottom(createFilterBar(state.getFilter()));
        return layout;
    }


    private static Region createNavigationBar(EditorState edState) {
        HBox bar = new HBox();

        edState.getNodePath().forEach(en -> {
            bar.getChildren().add(new JFXButton(en.getKeyName().orElse("<root>")));
        });
        edState.nodePathProperty().addListener((c, o, n) -> {
            bar.getChildren().clear();
            n.forEach(en -> {
                var btn = new JFXButton(en.getKeyName().orElse("<root>"));
                btn.setOnAction(e -> {
                    edState.navigateTo(en);
                });
                bar.getChildren().add(btn);
            });
        });
        return bar;
    }


    private static Region createNodeList(EditorState edState) {
        VBox nodes = new VBox();
        nodes.getChildren().setAll(createNodeList(edState, edState.getContent()));
        edState.contentProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                nodes.getChildren().setAll(createNodeList(edState, n));
            });
        });

        ScrollPane sp = new ScrollPane(nodes);
        return sp;
    }

    private static Region createNodeList(EditorState state, List<EditorNode> nodes) {
        GridPane grid = new GridPane();

        for (int i = 0; i < nodes.size(); i++) {
            var n = nodes.get(i);
            HBox k = new HBox();
            HBox eq = new HBox();
            if (n.getKeyName().isPresent()) {
                k.getChildren().add(new Label(n.getKeyName().get()));
                eq.getChildren().add(new Label("="));
                grid.add(k, 0, i);
                grid.add(eq, 1, i);
            }

            Region valueDisplay = null;
            if (n.getNode() instanceof ValueNode) {
                valueDisplay = new JFXTextField(n.getNode().getString());
            }
            if (n.getNode() instanceof ArrayNode) {
                var btn = new JFXButton((n.isSynthetic() ?
                        "(s) " : "") + "[... " + n.getNode().getNodeArray().size() + " ...]");
                btn.setOnAction(e -> {
                    state.navigateTo(n);
                });
                valueDisplay = btn;
            }

            HBox v = new HBox(valueDisplay);
            grid.add(v, 2, i);

            HBox btns = new HBox();
            Button edit = new JFXButton("EDIT");
            edit.setOnAction(e -> {
                state.getExternalState().startEdit(state, n);
            });
            Button del = new JFXButton("DEl");
            btns.getChildren().add(edit);
            btns.getChildren().add(del);
            grid.add(btns, 3, i);

            var binding = Bindings.createBooleanBinding(() -> {
                return k.isHover() || eq.isHover() || v.isHover() || btns.isHover();
            }, k.hoverProperty(), eq.hoverProperty(), v.hoverProperty(), btns.hoverProperty());
            edit.visibleProperty().bind(binding);
            del.visibleProperty().bind(binding);
        }

        return grid;
    }

    private static Region createFilterBar(EditorFilter edFilter) {
        HBox box = new HBox();
        box.setSpacing(8);
        box.setAlignment(Pos.CENTER);

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
            cs.getStyleClass().add(GuiStyle.CLASS_RECURSIVE);
            cs.setGraphic(new FontIcon());
            cs.selectedProperty().bindBidirectional(edFilter.caseSensitiveProperty());
            GuiTooltips.install(cs, "Case sensitive");
            box.getChildren().add(cs);
        }

        {
            ToggleButton deepSearch = new ToggleButton();
            deepSearch.getStyleClass().add(GuiStyle.CLASS_RECURSIVE);
            deepSearch.setGraphic(new FontIcon());
            deepSearch.selectedProperty().bindBidirectional(edFilter.deepProperty());
            GuiTooltips.install(deepSearch, "Deep search");
            box.getChildren().add(deepSearch);
        }

        return box;
    }
}
