package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.gui.game.Vic3CoatOfArmsCache;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

import javafx.scene.layout.Region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public abstract class Vic3DiplomacyRowComp extends DiplomacyRowComp<Vic3DiplomacyRowComp.Country> {

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class Country {
        private Vic3Tag tag;
        private String name;
    }

    public static Optional<Node> getCountries(SavegameContent content) {
        return content.get()
                .getNodeForKeyIfExistent("country_manager")
                .flatMap(cm -> cm.getNodeForKeyIfExistent("database"));
    }

    public static Optional<Country> getCountry(SavegameData<?> data, Node countries, String id) {
        var country = countries.getNodeForKeyIfExistent(id).filter(Node::isArray);
        if (country.isEmpty()) {
            return Optional.empty();
        }

        var definition = country.get()
                .getNodeForKeyIfExistent("definition")
                .filter(Node::isValue)
                .map(Node::getString);
        if (definition.isEmpty()) {
            return Optional.empty();
        }

        var government = country.get()
                .getNodeForKeyIfExistent("government")
                .filter(Node::isValue)
                .map(Node::getString)
                .orElse(null);
        var tag = new Vic3Tag(id, definition.get(), government);
        return Optional.of(new Country(tag, Vic3Names.getCountryName(data, country.get(), definition.get())));
    }

    @Override
    protected Region mapTag(SavegameData<?> data, Country country) {
        return GameImage.imageNode(Vic3CoatOfArmsCache.tagFlag(data.vic3(), country.tag), CLASS_TAG_ICON);
    }

    @Override
    protected String mapTagTooltip(SavegameData<?> data, Country country) {
        return country.name;
    }
}
