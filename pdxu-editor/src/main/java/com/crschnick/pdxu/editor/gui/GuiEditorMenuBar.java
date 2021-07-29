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

        Menu file = new Menu("File");
        MenuItem c = new MenuItem("Save");
        c.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        c.setOnAction((a) -> {
            state.save();
        });
        file.getItems().add(c);
        if (!state.canSave()) {
            c.setDisable(true);
        }


        Menu editor = new Menu("Editor");
        MenuItem cte = new MenuItem("Editor Settings");
        cte.setOnAction((a) -> {
            GuiEditorSettings.showEditorSettings();
        });
        editor.getItems().add(cte);

        MenuItem guide = new MenuItem("Editor Guide");
        guide.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.EDITOR_GUIDE);
        });
        editor.getItems().add(guide);

        MenuItem is = new MenuItem(PdxuI18n.get("REPORT_ISSUE"));
        is.setOnAction((a) -> {
            ErrorHandler.reportIssue(null);
        });
        editor.getItems().add(is);


        Menu jump = new Menu("Jump to");
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

        Menu scriptsEntry = new Menu("Scripts");
        Runnable fillScripts = () -> {
            if (state.isSavegame()) {
                try {
                    var scripts = EditorSavegameAdapter.ALL.get(state.getFileContext().getGame()).createScripts(state);
                    scripts.forEach((k, v) -> {
                        MenuItem j = new MenuItem(k);
                        j.setOnAction((a) -> {
                            try {
                                v.run();
                            } catch (Exception ex) {
                                ErrorHandler.handleException(ex);
                            }
                        });
                        scriptsEntry.getItems().add(j);
                        j.setDisable(v == null);
                    });
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                }
            }
        };
        fillScripts.run();
        scriptsEntry.setOnShowing(e -> {
            scriptsEntry.getItems().clear();
            fillScripts.run();
        });

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(file);
        menuBar.getMenus().add(editor);
        menuBar.getMenus().add(jump);
        menuBar.getMenus().add(scriptsEntry);
        return menuBar;
    }
}
