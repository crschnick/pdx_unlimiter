package com.crschnick.pdxu.app.gui;

import atlantafx.base.controls.CustomTextField;
import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.platform.PlatformThread;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import lombok.AllArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

@AllArgsConstructor
public class GuiFilterComp extends Comp<CompStructure<CustomTextField>> {

    private final Property<String> filterText;

    @Override
    public CompStructure<CustomTextField> createBase() {
        var fi = new FontIcon("mdi2m-magnify");
        var clear = new FontIcon("mdi2c-close");
        clear.setCursor(Cursor.DEFAULT);
        clear.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                filterText.setValue(null);
                event.consume();
            }
        });
        var filter = new CustomTextField();
        filter.setMinHeight(0);
        filter.setMaxHeight(20000);
        filter.getStyleClass().add("filter-comp");
        filter.promptTextProperty().bind(AppI18n.observable("searchFilter"));
        filter.rightProperty()
                .bind(Bindings.createObjectBinding(
                        () -> {
                            return filter.isFocused()
                                            || (filter.getText() != null
                                                    && !filter.getText().isEmpty())
                                    ? clear
                                    : fi;
                        },
                        filter.focusedProperty()));
        filter.setAccessibleText("Filter");

        filter.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.ESCAPE).match(event)) {
                filter.clear();
                filter.getScene().getRoot().requestFocus();
                event.consume();
            }
        });

        filterText.subscribe(val -> {
            PlatformThread.runLaterIfNeeded(() -> {
                clear.setVisible(val != null);
                if (!Objects.equals(filter.getText(), val) && !(val == null && "".equals(filter.getText()))) {
                    filter.setText(val);
                }
            });
        });

        filter.textProperty().addListener((observable, oldValue, n) -> {
            filterText.setValue(n != null && n.length() > 0 ? n : null);
        });

        return new SimpleCompStructure<>(filter);
    }
}
