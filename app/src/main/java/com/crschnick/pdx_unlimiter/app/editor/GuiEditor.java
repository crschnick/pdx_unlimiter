package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class GuiEditor {

    static Stage createStage(EditorState state) {
        Stage stage = new Stage();

        var icon = PdxuApp.getApp().getIcon();
        stage.getIcons().add(icon);
        var title = state.getFileName() + " - " + "Pdx-Unlimiter Editor";
        stage.setTitle(title);
        state.dirtyProperty().addListener((c,o,n) -> {
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
        var grid = createNodeList(state);
        layout.setCenter(grid);
        layout.setBottom(createFilterBar(state.getFilter()));
        return layout;
    }


    private static Region createNavigationBar(EditorState edState) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add(GuiStyle.CLASS_EDITOR_NAVIGATION);

        Consumer<List<EditorNode>> updateBar = l -> {
            Platform.runLater(() -> {
                bar.getChildren().clear();

                {
                    Button save = new Button();
                    save.setDisable(!edState.dirtyProperty().get());
                    edState.dirtyProperty().addListener((c,o,n) -> {
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
                    var btn = new JFXButton(en.navigationName());
                    btn.setMnemonicParsing(false);
                    {
                        var sep = new Label(">");
                        sep.setAlignment(Pos.CENTER);
                        bar.getChildren().add(sep);
                    }
                    btn.setOnAction(e -> {
                        edState.navigateTo(en);
                    });
                    bar.getChildren().add(btn);
                });

                if (l.size() > 0) {
                    Button edit = new JFXButton();
                    edit.setGraphic(new FontIcon());
                    edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
                    edit.setOnAction(e -> {
                        edState.getExternalState().startEdit(edState, l.get(l.size() - 1));
                    });
                    edit.setPadding(new Insets(4, 4, 2, 4));
                    bar.getChildren().add(edit);
                }
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
                sp.setVvalue(0);
                createNodeList(grid, edState, n);
            });
        });

        return sp;
    }

    private static void createNodeList(GridPane grid, EditorState state, List<EditorNode> nodes) {
        grid.getChildren().clear();

        int nodeCount = Math.min(nodes.size(), 300);
        for (int i = 0; i < nodeCount; i++) {
            var n = nodes.get(i);
            grid.add(new Label(n.displayKeyName()), 0, i);
            grid.add(new Label("="), 1, i);


            Region valueDisplay = createValueDisplay(n, state);
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

    private static Region createValueDisplay(EditorNode n, EditorState state) {
        boolean isText = n.isReal() && ((SimpleNode) n).getBackingNode() instanceof ValueNode;
        if (isText) {
            var tf = new TextField(((SimpleNode) n).getBackingNode().getString());
            tf.textProperty().addListener((c, o, ne) -> {
                ((SimpleNode) n).updateText(ne);
                state.onTextChanged();
            });
            return tf;
        } else {
            var box = new HBox();
            box.setAlignment(Pos.CENTER);
            box.setSpacing(5);

            int length = n.isReal() ? ((SimpleNode) n).getBackingNode().getNodeArray().size() :
                    ((CollectorNode) n).getNodes().size();
            int stringSize = String.valueOf(length).length();
            int spaceSize = 7 - stringSize;
            var lengthString =
                    " ".repeat(spaceSize / 2) + String.valueOf(length) +
                    " ".repeat(spaceSize - (spaceSize / 2));
            var btn = new JFXButton("[... " + lengthString + " ...]");
            btn.setAlignment(Pos.CENTER);
            btn.setOnAction(e -> {
                state.navigateTo(n);
            });
            box.getChildren().add(btn);

            var preview = new Label();
            preview.getStyleClass().add("preview");
            preview.setGraphic(new FontIcon());
            preview.setOnMouseEntered(e -> {
                var tt = new Tooltip(
                        TextFormatWriter.writeToString(n.toWritableNode(), 10, "  "));
                tt.setShowDelay(javafx.util.Duration.ZERO);
                Tooltip.install(preview, tt);
            });
            box.getChildren().add(preview);

            return box;
        }
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
                        filter.selectAll();
                    }
                });
                filter.setOnAction(e -> {
                    edFilter.filterStringProperty().set(filter.getText());
                });
                textBar.getChildren().add(filter);
            }

            {
                Button search = new Button();
                search.setOnAction(e -> {
                    edFilter.filterStringProperty().set(filter.getText());
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
