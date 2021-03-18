package com.crschnick.pdx_unlimiter.app.gui.editor;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.util.OsHelper;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

import java.io.IOException;
import java.nio.file.Files;

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
        MenuItem cte = new MenuItem("Change text editor");
        cte.setOnAction((a) -> {
            try {
                var f = Files.createTempFile(null, ".pdxt");
                OsHelper.openFileAssociationDialog(f);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
        editor.getItems().add(cte);

        Menu help = new Menu("Help");

        MenuItem guide = new MenuItem("Editor Guide");
        guide.setOnAction((a) -> {
            ThreadHelper.browse("https://github.com/crschnick/pdx_unlimiter/blob/master/EDITOR-GUIDE.md");
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
