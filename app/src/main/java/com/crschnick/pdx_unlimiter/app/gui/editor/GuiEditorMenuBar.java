package com.crschnick.pdx_unlimiter.app.gui.editor;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.GuiSavegameIO;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.dialog.*;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorageIO;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.Optional;

public class GuiEditorMenuBar {

    public static MenuBar createMenuBar(EditorState state) {

        Menu file = new Menu("File");
        MenuItem c = new MenuItem("Save");
        c.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        c.setOnAction((a) -> {
            state.save();
        });
        file.getItems().add(c);

        Menu help = new Menu("Help");

        MenuItem guide = new MenuItem("Editor Guide");
        guide.setOnAction((a) -> {
            ThreadHelper.browse("https://github.com/crschnick/pdx_unlimiter/blob/master/EDITOR-GUIDE.md");
        });
        help.getItems().add(guide);


        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(file);
        menuBar.getMenus().add(help);
        return menuBar;
    }
}
