package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.PlatformThread;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.AllArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
public class PathChoiceComp extends SimpleComp {

    private final Property<Path> pathProperty;
    private final String chooserTitleKey;
    private final boolean directory;

    @Override
    protected Region createSimple() {
        var prop = new SimpleStringProperty();
        pathProperty.subscribe(s -> PlatformThread.runLaterIfNeeded(() -> {
            prop.set(s != null ? s.toString() : null);
        }));
        prop.addListener((observable, oldValue, newValue) -> {
            try {
                pathProperty.setValue(newValue != null && !newValue.isBlank() ? Path.of(newValue.strip()) : null);
            } catch (InvalidPathException e) {
                pathProperty.setValue(null);
            }
        });

        var fileNameComp = new TextFieldComp(prop);
        fileNameComp.hgrow();

        var browseButton = new ButtonComp(null, new LabelGraphic.IconGraphic("mdi2f-folder-open-outline"), () -> {
            var prev = pathProperty.getValue();
            if (directory) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                if (prev != null && Files.exists(prev)) {
                    directoryChooser.setInitialDirectory(prev.toFile());
                }

                directoryChooser.setTitle(AppI18n.get(chooserTitleKey));
                File file = directoryChooser.showDialog(AppMainWindow.get().getStage());
                if (file != null) {
                    pathProperty.setValue(file.toPath());
                } else {
                    pathProperty.setValue(null);
                }
            } else {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(AppI18n.get(chooserTitleKey));
                File file = fileChooser.showOpenDialog(AppMainWindow.get().getStage());
                if (file != null) {
                    pathProperty.setValue(file.toPath());
                } else {
                    pathProperty.setValue(null);
                }
            }
        });

        var hbox = new InputGroupComp(List.of(fileNameComp, browseButton));
        hbox.setHeightReference(fileNameComp);
        return hbox.createRegion();
    }
}
