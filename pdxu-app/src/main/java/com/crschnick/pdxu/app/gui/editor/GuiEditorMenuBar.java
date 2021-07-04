package com.crschnick.pdxu.app.gui.editor;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.editor.adapter.EditorSavegameAdapter;
import com.crschnick.pdxu.app.gui.dialog.GuiEditorSettings;
import com.crschnick.pdxu.app.lang.PdxuI18n;
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

        MenuItem is = new MenuItem(PdxuI18n.get("REPORT_ISSUE"));
        is.setOnAction((a) -> {
            ErrorHandler.reportIssue(null);
        });
        help.getItems().add(is);


        Menu jump = new Menu("Jump to");
        Runnable fillJumps = () -> {
            if (state.isSavegame()) {
                try {
                    var jumps = EditorSavegameAdapter.ALL.get(state.getFileContext().getGame()).createCommonJumps(state);
                    jumps.forEach((k, v) -> {
                        MenuItem j = new MenuItem(k);
                        j.setOnAction((a) -> {
                            state.getNavHistory().navigateTo(v);
                        });
                        jump.getItems().add(j);
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
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(file);
        menuBar.getMenus().add(editor);
        menuBar.getMenus().add(help);
        menuBar.getMenus().add(jump);
        return menuBar;
    }
}
