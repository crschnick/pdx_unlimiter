package com.crschnick.pdxu.editor.gui;


import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.editor.EditorNavLocation;
import com.crschnick.pdxu.editor.EditorSettings;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.adapter.EditorSavegameAdapter;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

public class GuiEditorNavBar {

    static Region createNavigationBar(EditorState edState) {
        HBox bar = new HBox();
        bar.setFillHeight(true);

        var arrows = setupNavArrows(edState);
        bar.getChildren().add(arrows);

        var content = setupNavContent(edState);
        bar.getChildren().add(content);
        HBox.setHgrow(content, Priority.ALWAYS);

        bar.getChildren().add(setupEditButton(edState));

        return bar;
    }

    private static Node setupNavArrows(EditorState state) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setFillHeight(true);
        {
            var backButton = new JFXButton();
            GuiTooltips.install(backButton, "Go back");
            backButton.getStyleClass().add("nav-back-button");
            backButton.setGraphic(new FontIcon());
            backButton.setAlignment(Pos.CENTER);
            box.getChildren().add(backButton);
            backButton.disableProperty().bind(Bindings.not(state.getNavigation().canGoBackProperty()));
            backButton.setOnAction(e -> state.getNavigation().goBack());
            backButton.setFocusTraversable(false);
        }
        {
            var forwardButton = new JFXButton();
            GuiTooltips.install(forwardButton, "Go forward");
            forwardButton.getStyleClass().add("nav-forward-button");
            forwardButton.setGraphic(new FontIcon());
            forwardButton.setAlignment(Pos.CENTER);
            box.getChildren().add(forwardButton);
            forwardButton.disableProperty().bind(Bindings.not(state.getNavigation().canGoForwardProperty()));
            forwardButton.setOnAction(e -> state.getNavigation().goForward());
            forwardButton.setFocusTraversable(false);
        }
        return box;
    }

    private static HBox setupNavContent(EditorState edState) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add(GuiStyle.CLASS_EDITOR_NAVIGATION);

        Consumer<EditorNavLocation> updateBar = l -> {
            Platform.runLater(() -> {
                bar.getChildren().clear();
                {
                    var initBtn = new JFXButton("(root)");
                    initBtn.setFocusTraversable(false);
                    initBtn.setOnAction(e -> {
                        edState.getNavigation().navigateToParent(null);
                    });
                    bar.getChildren().add(initBtn);
                }

                l.path().getPath().subList(1, l.path().getPath().size()).forEach(en -> {
                    var btn = new JFXButton(en.getNavigationName());
                    btn.setFocusTraversable(false);
                    btn.setMnemonicParsing(false);
                    {
                        var sep = new Label("/");
                        sep.setAlignment(Pos.CENTER);
                        bar.getChildren().add(sep);
                    }
                    btn.setOnAction(e -> {
                        edState.getNavigation().navigateToParent(en);
                    });
                    bar.getChildren().add(btn);
                });
            });
        };
        updateBar.accept(edState.getNavigation().getCurrent());
        edState.getNavigation().currentProperty().addListener((c, o, n) -> {
            updateBar.accept(n);
        });
        return bar;
    }

    private static Node setupEditButton(EditorState edState) {
        HBox p = new HBox();
        p.setFillHeight(true);
        p.setAlignment(Pos.CENTER);

        edState.getNavigation().currentProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                if (p.getChildren().size() == 2) {
                    p.getChildren().remove(0);
                }

                if (n.getEditorNode() != null && n.getEditorNode().isReal() &&
                        EditorSettings.getInstance().enableNodeTags.getValue()) {
                    try {
                        var tag = EditorSavegameAdapter.ALL.get(edState.getFileContext().getGame())
                                .createNodeTag(edState, (EditorRealNode) n.getEditorNode(), null);
                        if (tag != null) {
                            p.getChildren().add(0, tag);
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handleException(ex);
                    }
                }
            });
        });

        {
            Button edit = new JFXButton();
            edit.setFocusTraversable(false);
            edit.setGraphic(new FontIcon());
            edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
            GuiTooltips.install(edit, "Open in external text editor");
            edit.setOnAction(e -> {
                edState.getExternalState().startEdit(edState, (EditorRealNode) edState.getNavigation().getCurrent().getEditorNode());
            });
            edit.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> !edState.isEditable() || edState.getNavigation().getCurrent().getEditorNode() == null ||
                            !edState.getNavigation().getCurrent().getEditorNode().isReal(),
                    edState.getNavigation().currentProperty()));
            edit.setPadding(new Insets(4, 4, 2, 4));
            p.getChildren().add(edit);
        }
        return p;
    }
}
