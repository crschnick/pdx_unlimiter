package com.crschnick.pdx_unlimiter.app.gui.editor.coa;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.editor.EditorSimpleNode;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuiCk3CoaViewer {

    private GuiCk3CoaViewerState state;

    public GuiCk3CoaViewer(EditorState edState, EditorSimpleNode editorNode) {
        this.state = new GuiCk3CoaViewerState(edState, editorNode);
    }

    public void createStage() {
        Stage stage = new Stage();

        var icon = PdxuApp.getApp().getIcon();
        stage.getIcons().add(icon);

        stage.setTitle("Coat of arms preview");
        stage.setScene(new Scene(createLayout(), 720, 600));
        GuiStyle.addStylesheets(stage.getScene());
        stage.show();
    }

    private Region createLayout() {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("editor");
        var v = new VBox();
        v.setFillWidth(true);
        v.setPadding(new Insets(20, 20, 20, 20));
        v.getStyleClass().add("editor-nav-bar-container");
        var topBars = new VBox(v);
        topBars.setFillWidth(true);
        layout.setTop(topBars);

        // Disable focus on startup
        layout.requestFocus();

        HBox topBar = new HBox();
        topBar.getStyleClass().add("coa-options");
        topBar.setSpacing(10);
        layout.setTop(topBar);
        state.init(topBar);

        layout.setCenter(new ImageView(state.getImage()));
        state.imageProperty().addListener((c,o,n) -> {
            layout.setCenter(new ImageView(n));
        });

        return layout;
    }
}
