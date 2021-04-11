package com.crschnick.pdx_unlimiter.app.gui.editor.coa;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.editor.GuiEditorMenuBar;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuiCk3CoaViewer {

    public static Stage createStage(EditorState state) {
        Stage stage = new Stage();

        var icon = PdxuApp.getApp().getIcon();
        stage.getIcons().add(icon);

        stage.setTitle("Coat of arms preview");
        stage.setScene(new Scene(createLayout(state), 720, 600));
        GuiStyle.addStylesheets(stage.getScene());
        stage.show();
        return stage;
    }

    private static Region createLayout(EditorState state) {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("editor");
        var v = new VBox();
        v.setFillWidth(true);
        v.setPadding(new Insets(20, 20, 20, 20));
        v.getStyleClass().add("editor-nav-bar-container");
        var topBars = new VBox(
                GuiEditorMenuBar.createMenuBar(state),
                v);
        topBars.setFillWidth(true);
        layout.setTop(topBars);

        // Disable focus on startup
        layout.requestFocus();

        return layout;
    }
}
