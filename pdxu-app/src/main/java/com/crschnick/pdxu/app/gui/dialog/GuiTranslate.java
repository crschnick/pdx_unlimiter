package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class GuiTranslate {

    public static void showTranslatationAlert() {
        ButtonType translate = new ButtonType("Translate", ButtonBar.ButtonData.OK_DONE);
        GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setTitle("Help translating the Pdx-Unlimiter interface");
            alert.setHeaderText("""
                    You can switch the language in the settings menu. However, many languages are not supported yet.
                    If you want to help at translating the user interface into your language, you can easily do so.""");

            alert.getButtonTypes().add(ButtonType.CANCEL);
            alert.getButtonTypes().add(translate);
            Button val = (Button) alert.getDialogPane().lookupButton(translate);
            val.addEventHandler(
                    ActionEvent.ACTION,
                    e -> {
                        ThreadHelper.open(PdxuInstallation.getInstance().getLanguageLocation());
                        ThreadHelper.browse(Hyperlinks.TRANSLATION_ISSUE);
                        e.consume();
                    }
            );
        });
    }
}
