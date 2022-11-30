package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GuiCk3CoaDisplayType extends GuiCoaDisplayType {

    public static GuiCk3CoaDisplayType NONE = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(CoatOfArms coa, GameFileContext ctx) {
            return ImageHelper.toFXImage(
                    Ck3TagRenderer.renderImage(coa, ctx, size.get(), clothPattern.get()));
        }
    };

    public static GuiCk3CoaDisplayType HOUSE = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderHouseImage(coa, ctx, size.get(), clothPattern.get());
        }
    };

    public static GuiCk3CoaDisplayType DYNASTY = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderRealmImage(coa, "clan_government", ctx, size.get(), clothPattern.get());
        }
    };

    public static GuiCk3CoaDisplayType TITLE = new GuiCk3CoaDisplayType() {

        @Override
        public Image render(CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderTitleImage(coa, ctx, size.get(), clothPattern.get());
        }
    };

    public static GuiCk3CoaDisplayType REALM = new GuiCk3CoaDisplayType() {

        private final StringProperty governmentType = new SimpleStringProperty();

        @Override
        public void addOptions(GuiCoaViewerState<?> state, HBox box) {
            super.addOptions(state, box);
            addChoice(state, box, "Government", "clan_government", Map.of(
                    "Clan", "clan_government",
                    "Republic", "republic_government",
                    "Theocracy", "theocracy_government",
                    "Tribal", "tribal_government"),
                    governmentType);
        }

        @Override
        public Image render(CoatOfArms coa, GameFileContext ctx) {
            return Ck3TagRenderer.renderRealmImage(coa, governmentType.get(), ctx, size.get(), clothPattern.get());
        }
    };

    public static void init(GuiCoaViewerState.Ck3GuiCoaViewerState state, HBox box) {
        HBox options = new HBox();
        options.setSpacing(10);

        var sizes = new LinkedHashMap<String, Number>();
        sizes.put("64 x 64", 64);
        sizes.put("128 x 128", 128);
        sizes.put("256 x 256", 256);
        sizes.put("512 x 512", 512);
        sizes.put("1024 x 1024", 1024);
        box.getChildren().add(createChoices("Size", 256, sizes, t -> {
            state.getDisplayType().size.set(t.intValue());
            state.updateImage();
        }));
        state.getDisplayType().size.set(256);

        var cloths = new LinkedHashMap<String, Boolean>();
        cloths.put("enable", true);
        cloths.put("disable", false);
        box.getChildren().add(createChoices("Cloth pattern", true, cloths, t -> {
            state.getDisplayType().clothPattern.set(t);
            state.updateImage();
        }));
        state.getDisplayType().clothPattern.set(true);

        var types = new LinkedHashMap<String, GuiCk3CoaDisplayType>();
        types.put("None", NONE);
        types.put("Realm", REALM);
        types.put("House", HOUSE);
        types.put("Dynasty", DYNASTY);
        types.put("Title", TITLE);
        box.getChildren().add(createChoices("Type", REALM, types, t -> {
            t.size.set(state.getDisplayType().size.get());
            t.clothPattern.set(state.getDisplayType().clothPattern.get());
            state.displayTypeProperty().set(t);
            options.getChildren().clear();
            t.addOptions(state, options);
            state.updateImage();
        }));

        REALM.addOptions(state, options);
        box.getChildren().add(options);
    }

    protected final BooleanProperty clothPattern = new SimpleBooleanProperty();

}
