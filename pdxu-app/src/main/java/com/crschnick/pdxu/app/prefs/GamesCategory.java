package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.GameDistChoiceComp;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.dist.GameDist;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.nio.file.Path;

public class GamesCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "games";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2a-animation-play-outline");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("gameInstallations")
                .sub(new OptionsBuilder()
                        .nameAndDescription("gameDirs")
                        .addComp(Comp.empty())
                        .name("eu4")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.EU4, prefs.eu4Directory))
                        .name("ck3")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.CK3, prefs.ck3Directory))
                        .name("hoi4")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.HOI4, prefs.hoi4Directory))
                        .name("vic3")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.VIC3, prefs.vic3Directory))
                        .name("stellaris")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.STELLARIS, prefs.stellarisDirectory))
                        .name("ck2")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.CK2, prefs.ck2Directory))
                        .name("vic2")
                        .description("installationDirectory")
                        .addComp(gameChoice(Game.VIC2, prefs.vic2Directory))
                );
        return builder.buildComp();
    }

    private Comp<?> gameChoice(Game g, Property<Path> property) {
        return new GameDistChoiceComp(g.getId() + "Abbreviation", g, wrap(g, property)).maxWidth(600);
    }

    private Property<GameDist> wrap(Game game, Property<Path> p) {
        var initial = GameInstallation.ALL.get(game);
        var prop = new SimpleObjectProperty<>(initial != null ? initial.getDist() : null);
        prop.subscribe(gameDist -> {
           p.setValue(gameDist != null ? gameDist.getInstallLocation() : null);
        });
        return prop;
    }
}
