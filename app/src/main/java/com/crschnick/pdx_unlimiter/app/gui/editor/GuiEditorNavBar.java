package com.crschnick.pdx_unlimiter.app.gui.editor;

import com.crschnick.pdx_unlimiter.app.editor.EditorNode;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
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
        }
        {
            var forwardButton = new JFXButton();
            GuiTooltips.install(forwardButton, "Go forward");
            forwardButton.getStyleClass().add("nav-forward-button");
            forwardButton.setGraphic(new FontIcon());
            forwardButton.setAlignment(Pos.CENTER);
            box.getChildren().add(forwardButton);
        }
        return box;
    }

    private static HBox setupNavContent(EditorState edState) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add(GuiStyle.CLASS_EDITOR_NAVIGATION);

        Consumer<List<EditorState.NavEntry>> updateBar = l -> {
            Platform.runLater(() -> {
                bar.getChildren().clear();
                {
                    var initBtn = new JFXButton("(root)");
                    initBtn.setOnAction(e -> {
                        edState.navigateTo((EditorNode) null);
                    });
                    bar.getChildren().add(initBtn);
                }

                l.forEach(en -> {
                    var btn = new JFXButton(en.getEditorNode().getNavigationName());
                    btn.setMnemonicParsing(false);
                    {
                        var sep = new Label("/");
                        sep.setAlignment(Pos.CENTER);
                        bar.getChildren().add(sep);
                    }
                    btn.setOnAction(e -> {
                        edState.navigateTo(en.getEditorNode());
                    });
                    bar.getChildren().add(btn);
                });
            });
        };
        updateBar.accept(edState.getNavPath());
        edState.navPathProperty().addListener((c, o, n) -> {
            updateBar.accept(n);
        });
        return bar;
    }

    private static Node setupEditButton(EditorState edState) {
        HBox p = new HBox();
        p.setFillHeight(true);
        p.setAlignment(Pos.CENTER);

        Button edit = new JFXButton();
        edit.setGraphic(new FontIcon());
        edit.getStyleClass().add(GuiStyle.CLASS_EDIT);
        edit.setOnAction(e -> {
            //edState.getExternalState().startEdit(edState, l.get(l.size() - 1).getEditorNode());
        });
        edit.setPadding(new Insets(4, 4, 2, 4));
        p.getChildren().setAll(edit);

        Consumer<List<EditorState.NavEntry>> updateBar = l -> {
            Platform.runLater(() -> {
                edit.setDisable(l.size() == 0);
            });
        };
        updateBar.accept(edState.getNavPath());
        edState.navPathProperty().addListener((c, o, n) -> {
            updateBar.accept(n);
        });
        return p;
    }
}
