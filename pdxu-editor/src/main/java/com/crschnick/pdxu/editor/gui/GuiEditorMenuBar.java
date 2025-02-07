package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.adapter.EditorSavegameAdapter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

public class GuiEditorMenuBar {

    public static MenuBar createMenuBar(EditorState state) {

        Menu file = new Menu(PdxuI18n.get("EDITOR_MENU_FILE"));
        MenuItem c = new MenuItem(PdxuI18n.get("EDITOR_MENU_FILE_SAVE"));
        c.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        c.setOnAction((a) -> {
            state.save();
        });
        file.getItems().add(c);
        if (!state.isEditable()) {
            c.setDisable(true);
        }


        Menu editor = new Menu(PdxuI18n.get("EDITOR_MENU_EDITOR"));
        MenuItem cte = new MenuItem(PdxuI18n.get("EDITOR_MENU_EDITOR_SETTINGS"));
        cte.setOnAction((a) -> {
            GuiEditorSettings.showEditorSettings();
        });
        editor.getItems().add(cte);

        MenuItem guide = new MenuItem(PdxuI18n.get("EDITOR_MENU_EDITOR_GUIDE"));
        guide.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.EDITOR_GUIDE);
        });
        editor.getItems().add(guide);

        Menu jump = new Menu(PdxuI18n.get("EDITOR_MENU_JUMP"));
        Runnable fillJumps = () -> {
            if (state.isSavegame()) {
                try {
                    var jumps = EditorSavegameAdapter.ALL.get(state.getFileContext().getGame()).createCommonJumps(state);
                    jumps.forEach((k, v) -> {
                        MenuItem j = new MenuItem(k);
                        j.setOnAction((a) -> {
                            state.getNavigation().navigateTo(v);
                        });
                        jump.getItems().add(j);
                        j.setDisable(v == null);
                    });
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                }
            }
        };
        fillJumps.run();
        jump.setOnShowing(e -> {
            jump.getItems().clear();
            fillJumps.run();
        });

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(false);
        menuBar.getMenus().add(file);
        menuBar.getMenus().add(editor);
        menuBar.getMenus().add(jump);
        return menuBar;
    }
}
