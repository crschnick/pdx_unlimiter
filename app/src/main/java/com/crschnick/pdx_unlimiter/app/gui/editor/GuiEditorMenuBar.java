package com.crschnick.pdx_unlimiter.app.gui.editor;

import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdx_unlimiter.app.util.Hyperlinks;
import javafx.scene.control.Alert;
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
        MenuItem cte = new MenuItem("Change text editor");
        cte.setOnAction((a) -> {
            GuiDialogHelper.showBlockingAlert(alert -> {
                alert.setAlertType(Alert.AlertType.INFORMATION);
                alert.setTitle("Change text editor");
                alert.setHeaderText("If you want to change the text editor used when clicking on the edit button,\n" +
                        "create a new .pdxt file and choose the default program that should be used to open it.");
            });
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
