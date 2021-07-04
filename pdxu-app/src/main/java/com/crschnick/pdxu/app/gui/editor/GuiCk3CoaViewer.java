package com.crschnick.pdxu.app.gui.editor;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.editor.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;


public class GuiCk3CoaViewer {

    private final GuiCk3CoaViewerState state;

    public GuiCk3CoaViewer(EditorState edState, EditorSimpleNode editorNode) {
        this.state = new GuiCk3CoaViewerState(edState, editorNode);
    }

    public void createStage() {
        Stage stage = new Stage();

        var icon = PdxuApp.getApp().getIcon();
        stage.getIcons().add(icon);

        stage.setTitle("Coat of arms preview");
        var scene = new Scene(createLayout(), 720, 600);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), () -> state.refresh());
        stage.setScene(scene);
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
        topBar.setAlignment(Pos.CENTER);
        topBar.setFillHeight(true);
        topBar.getStyleClass().add("coa-options");

        Button refresh = new Button();
        refresh.setOnAction(e -> {
            state.refresh();
        });
        refresh.setGraphic(new FontIcon("mdi-refresh"));
        topBar.getChildren().add(refresh);

        var spacer = new Region();
        topBar.getChildren().add(spacer);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox coaOptionsBar = new HBox();
        coaOptionsBar.setSpacing(10);
        state.init(coaOptionsBar);
        topBar.getChildren().add(coaOptionsBar);

        layout.setTop(topBar);

        layout.setCenter(new ImageView(state.getImage()));
        state.imageProperty().addListener((c,o,n) -> {
            layout.setCenter(new ImageView(n));
        });

        return layout;
    }
}
