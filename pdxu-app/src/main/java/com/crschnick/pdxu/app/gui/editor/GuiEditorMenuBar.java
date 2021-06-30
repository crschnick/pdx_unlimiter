package com.crschnick.pdxu.app.gui.editor;

import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.dialog.GuiEditorSettings;
import com.crschnick.pdxu.app.util.Hyperlinks;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

public class GuiEditorMenuBar {

    public static MenuBar createMenuBar(EditorState state) {

        Menu file = new Menu("File");
        MenuItem c = new MenuItem("Save");
        c.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        c.setOnAction((a) -> {
            state.save();
        });
        file.getItems().add(c);


        Menu editor = new Menu("Editor");
        MenuItem cte = new MenuItem("Editor Settings");
        cte.setOnAction((a) -> {
            GuiEditorSettings.showEditorSettings();
        });
        editor.getItems().add(cte);

        Menu help = new Menu("Help");

        MenuItem guide = new MenuItem("Editor Guide");
        guide.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.EDITOR_GUIDE);
        });
        help.getItems().add(guide);


        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(file);
        menuBar.getMenus().add(editor);
        menuBar.getMenus().add(help);
        return menuBar;
    }
}
