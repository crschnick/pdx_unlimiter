package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.comp.base.ScrollComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.installation.GameLanguage;
import com.crschnick.pdxu.app.installation.GameLocalisationHelper;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ConverterSupport;
import com.crschnick.pdxu.app.util.DesktopHelper;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.Value;

import java.util.Map;

@Value
public class GuiConverterConfig {

    ConverterSupport converterSupport;

    public boolean showConfirmConversionDialog() {
        var modal = ModalOverlay.of("converterConfigureTitle", AppDialog.dialogText(
                AppI18n.observable("converterConfigureContent", converterSupport.getFromName(), converterSupport.getToName())));
        var ok = new SimpleBooleanProperty();
        modal.addButton(new ModalButton("converterConfigure", () -> {
            ok.set(true);
        }, true, true));
        modal.showAndWait();
        return ok.get();
    }

    public void showUsageDialog() {
        var modal = ModalOverlay.of("converterUsageTitle", AppDialog.dialogTextKey("converterUsageContent"));
        modal.addButton(new ModalButton("showDownloads", () -> {
            Hyperlinks.open(converterSupport.getDownloadLink());
            AppPrefs.get().selectCategory("paradoxConverters");
        }, true, true));
        modal.show();
    }

    public void showConversionSuccessDialog() {
        var modal = ModalOverlay.of("converterSuccessTitle", AppDialog.dialogTextKey("converterSuccessContent"));
        modal.addButton(ModalButton.ok());
        modal.show();
    }

    public void showConversionErrorDialog() {
        var modal = ModalOverlay.of("converterFailedTitle", AppDialog.dialogTextKey("converterFailedContent"));
        modal.addButton(new ModalButton("converterFailedLogs", () -> {
            DesktopHelper.openInDefaultApplication(converterSupport.getWorkingDir().resolve("log.txt"));
        }, true, true));
        modal.show();
    }

    public boolean showConfig(Map<String, String> values) {
        Node configNode;
        Map<String, String> translations;
        try {
            configNode = TextFormatParser.text().parse(converterSupport.getBaseDir()
                            .resolve("Configuration").resolve("fronter-options.txt"));
            translations = GameLocalisationHelper.loadTranslations(converterSupport.getBaseDir()
                    .resolve("Configuration").resolve("options.yml"), GameLanguage.ENGLISH);
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
            return false;
        }

        var options = new OptionsBuilder();
        for (var node : configNode.getNodesForKey("option")) {
            if (node.getNodeForKeyIfExistent("radioSelector").isEmpty()) {
                continue;
            }

            options.fixedName(translations.get(node.getNodeForKey("displayName").getString()));
            options.fixedDescription(translations.get(node.getNodeForKey("tooltip").getString()));
            options.addComp(Comp.empty());

            ToggleGroup tg = new ToggleGroup();
            String oName = node.getNodeForKey("name").getString();
            var col = new VBox();
            for (var ro : node.getNodeForKey("radioSelector").getNodesForKey("radioOption")) {
                String roValue = ro.getNodeForKey("name").getString();
                var btnHelp = ro.getNodeForKeyIfExistent("tooltip").filter(t -> !t.getString().startsWith("e_")).orElse(null);
                var btnText = translations.get(ro.getNodeForKey("displayName").getString()) + (btnHelp != null ? "- " + translations.get(btnHelp.getString()) : "");
                var btn = new RadioButton(btnText);
                btn.setAlignment(Pos.CENTER_LEFT);
                btn.setToggleGroup(tg);
                btn.selectedProperty().addListener((c, o, ne) -> {
                    values.put(oName, roValue);
                });
                if (values.get(oName) != null && values.get(oName).equals(roValue)) {
                    btn.setSelected(true);
                }
                if (values.get(oName) == null && ro.getNodeForKey("default").getString().equals("true")) {
                    btn.setSelected(true);
                }

                col.getChildren().add(btn);
            }

            options.addComp(Comp.of(() -> col));
            options.addComp(Comp.hseparator().padding(new Insets(3, 3, 3, 0)));
        }

        var ok = new SimpleBooleanProperty();
        var modal = ModalOverlay.of("converterSettings", new ScrollComp(options.buildComp()).prefWidth(600));
        modal.addButton(new ModalButton("openConfigs", () -> {
            DesktopHelper.browsePath(converterSupport.getBackendDir().resolve("configurables"));
        }, false, false));
        modal.addButton(ModalButton.cancel());
        modal.addButton(ModalButton.ok(() -> {
            ok.setValue(true);
        }));
        modal.showAndWait();
        return ok.get();
    }
}
