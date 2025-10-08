package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.comp.base.ButtonComp;
import com.crschnick.pdxu.app.core.AppFontSizes;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.util.BooleanScope;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import lombok.Getter;

import static atlantafx.base.theme.Styles.ACCENT;
import static atlantafx.base.theme.Styles.BUTTON_OUTLINED;

public class ErrorHandlerComp extends SimpleComp {

    private final ErrorEvent event;
    private final Runnable closeDialogAction;

    @Getter
    private final Property<ErrorAction> takenAction = new SimpleObjectProperty<>();

    public ErrorHandlerComp(ErrorEvent event, Runnable closeDialogAction) {
        this.event = event;
        this.closeDialogAction = closeDialogAction;
    }

    private Region createActionButtonGraphic(String nameString, String descString) {
        var header = new Label(nameString);
        AppFontSizes.xl(header);
        var desc = new Label(descString);
        AppFontSizes.xs(desc);
        var text = new VBox(header, desc);
        text.setSpacing(2);
        return text;
    }

    private Region createActionComp(ErrorAction a, BooleanProperty busy) {
        var graphic = createActionButtonGraphic(a.getName(), a.getDescription());
        var b = new ButtonComp(null, graphic, () -> {
            takenAction.setValue(a);
            ThreadHelper.runAsync(() -> {
                try (var ignored = new BooleanScope(busy).start()) {
                    var r = a.handle(event);
                    if (r) {
                        closeDialogAction.run();
                    }
                } catch (Exception ignored) {
                    closeDialogAction.run();
                }
            });
        });
        b.disable(busy);
        b.maxWidth(2000);
        return b.createRegion();
    }

    private Region createTop() {
        var desc = getEventDescription();

        // Account for line wrapping of long lines
        var estimatedLineCount = desc.lines()
                .mapToInt(s -> Math.max(1, (int) Math.ceil(s.length() / 80.0)))
                .sum();

        var descriptionField = new TextArea(desc);
        descriptionField.setPrefRowCount(Math.max(5, Math.min(estimatedLineCount + 2, 14)));
        descriptionField.setWrapText(true);
        descriptionField.setEditable(false);
        descriptionField.setPadding(Insets.EMPTY);
        descriptionField.getStyleClass().add("description");
        AppFontSizes.base(descriptionField);
        var text = new VBox(descriptionField);
        text.setFillWidth(true);
        text.setSpacing(8);
        return text;
    }

    private String getEventDescription() {
        var desc = event.getDescription();

        Throwable t = event.getThrowable();
        while (t != null) {
            var toAppend = t.getMessage() != null
                    ? t.getMessage()
                    : AppI18n.get("errorTypeOccured", t.getClass().getSimpleName());
            desc = desc != null ? desc + "\n\n" + toAppend : toAppend;
            t = t.getCause();
        }

        if (desc == null) {
            desc = AppI18n.get("errorNoDetail");
        }

        desc = desc.strip();

        if (event.isTerminal()) {
            desc = desc + "\n\n" + AppI18n.get("terminalErrorDescription");
        }

        return desc;
    }

    @Override
    protected Region createSimple() {
        var top = createTop();
        var content = new VBox(top);
        var header = new Label(AppI18n.get("possibleActions"));
        header.setPadding(new Insets(0, 0, 2, 3));
        AppFontSizes.xl(header);
        var actionBox = new VBox();
        actionBox.getStyleClass().add("actions");
        actionBox.setFillWidth(true);

        var custom = event.getCustomActions();
        var busy = new SimpleBooleanProperty();
        for (var c : custom) {
            var ac = createActionComp(c, busy);
            ac.getStyleClass().addAll(BUTTON_OUTLINED, ACCENT);
            actionBox.getChildren().add(ac);
        }

        if (event.getLink() != null) {
            var ac = createActionComp(ErrorAction.openDocumentation(event.getLink()), busy);
            ac.getStyleClass().addAll(BUTTON_OUTLINED, ACCENT);
            actionBox.getChildren().add(ac);
        }

        var hasCustomActions = event.getCustomActions().size() > 0 || event.getLink() != null;
        if (hasCustomActions) {
            actionBox.getChildren().add(createActionComp(ErrorAction.ignore(), busy));
        }

        if (actionBox.getChildren().size() > 0) {
            actionBox.getChildren().addFirst(header);
            content.getChildren().add(new Separator(Orientation.HORIZONTAL));
            actionBox.getChildren().get(1).getStyleClass().addAll(BUTTON_OUTLINED);
            content.getChildren().addAll(actionBox);
        }

        content.getStyleClass().add("top");
        content.setFillWidth(true);
        content.setMinHeight(Region.USE_PREF_SIZE);

        var layout = new VBox();
        layout.getChildren().add(content);
        layout.getStyleClass().add("error-handler-comp");

        return layout;
    }
}
