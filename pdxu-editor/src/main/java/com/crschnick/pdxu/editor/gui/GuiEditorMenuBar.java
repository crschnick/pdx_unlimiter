package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.adapter.EditorSavegameAdapter;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

public class GuiEditorMenuBar {

    public static MenuBar createMenuBar(EditorState state) {

        Menu file = new Menu(AppI18n.get("editorMenuFile"));
        MenuItem c = new MenuItem(AppI18n.get("editorMenuFileSave"));
        c.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        c.setOnAction((a) -> {
            state.save();
        });
        file.getItems().add(c);
        if (!state.isEditable()) {
            c.setDisable(true);
        }


        Menu editor = new Menu(AppI18n.get("editorMenuEditor"));
        MenuItem cte = new MenuItem(AppI18n.get("editorMenuEditorSettings"));
        cte.setOnAction((a) -> {
            AppPrefs.get().selectCategory("editor");
        });
        editor.getItems().add(cte);

        MenuItem guide = new MenuItem(AppI18n.get("editorMenuEditorGuide"));
        guide.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.EDITOR_GUIDE);
        });
        editor.getItems().add(guide);

        Menu jump = new Menu(AppI18n.get("editorMenuJump"));
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
                    ErrorEventFactory.fromThrowable(ex).handle();
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
