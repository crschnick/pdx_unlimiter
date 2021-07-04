package com.crschnick.pdxu.app.gui.editor;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.ImageLoader;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.model.ck3.Ck3CoatOfArms;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Map;
import java.util.function.Consumer;

public abstract class GuiCk3CoaDisplayType {

    private static <T> Node createChoices(String name, T defValue, Map<String,T> choices, Consumer<T> con) {
        var list = FXCollections.observableArrayList(choices.values());
        var cb = new ChoiceBox<>(list);
        cb.setValue(defValue);
        cb.valueProperty().addListener((c,o,n) -> {
            con.accept(n);
        });
        cb.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return new DualHashBidiMap<>(choices).inverseBidiMap().get(object);
            }

            @Override
            public T fromString(String string) {
                return choices.get(string);
            }
        });

        HBox b = new HBox(new Label(name + ":"), cb);
        b.setAlignment(Pos.CENTER);
        b.setSpacing(3);
        return b;
    }

    public static GuiCk3CoaDisplayType NONE = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(Ck3CoatOfArms coa, GameFileContext ctx) {
            return ImageLoader.toFXImage(
                    Ck3TagRenderer.renderImage(coa, ctx, size.get(), clothPattern.get()));
        }
    };

    public static GuiCk3CoaDisplayType HOUSE = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(Ck3CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderHouseImage(coa, ctx, size.get(), clothPattern.get());
        }
    };

    public static GuiCk3CoaDisplayType DYNASTY = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(Ck3CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderRealmImage(coa, "clan_government", ctx, size.get(), clothPattern.get());
        }
    };

    public static GuiCk3CoaDisplayType TITLE = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(Ck3CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderTitleImage(coa, ctx, size.get(), clothPattern.get());
        }
    };

    public static GuiCk3CoaDisplayType REALM = new GuiCk3CoaDisplayType() {

        private final StringProperty governmentType = new SimpleStringProperty();

        @Override
        public void addOptions(GuiCk3CoaViewerState state, HBox box) {
            super.addOptions(state, box);
            addChoice(state, box, "Government", "clan_government", Map.of(
                    "Clan", "clan_government",
                    "Republic", "republic_government",
                    "Theocracy", "theocracy_government",
                    "Tribal", "tribal_government"),
                    governmentType);
        }

        @Override
        public Image render(Ck3CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderRealmImage(coa, governmentType.get(), ctx, size.get(), clothPattern.get());
        }
    };

    public static void init(GuiCk3CoaViewerState state, HBox box) {
        HBox options = new HBox();
        options.setSpacing(10);
        box.getChildren().add(createChoices("Type", REALM, Map.of(
                "None", NONE,
                "Realm", REALM,
                "House", HOUSE,
                "Dynasty", DYNASTY,
                "Title", TITLE), t -> {
            state.displayTypeProperty().set(t);
            options.getChildren().clear();
            t.addOptions(state, options);
            state.updateImage();
        }));
        REALM.addOptions(state, options);
        box.getChildren().add(options);
    }

    protected final IntegerProperty size = new SimpleIntegerProperty();
    protected final BooleanProperty clothPattern = new SimpleBooleanProperty();

    protected <T> void addChoice(GuiCk3CoaViewerState state, HBox box, String type, T defValue, Map<String,T> choices, Property<T> prop) {
        prop.setValue(defValue);
        box.getChildren().add(createChoices(type, defValue, choices, t -> {
            prop.setValue(t);
            state.updateImage();
        }));
    }

    public void addOptions(GuiCk3CoaViewerState state, HBox box) {
        addChoice(state, box, "Size", 256, Map.of(
                "256 x 256", 256,
                "512 x 512", 512), size);
        addChoice(state, box, "Cloth pattern", true, Map.of(
                "enable", true,
                "disable", false), clothPattern);
    }

    public abstract Image render(Ck3CoatOfArms coa, GameFileContext ctx);
}
