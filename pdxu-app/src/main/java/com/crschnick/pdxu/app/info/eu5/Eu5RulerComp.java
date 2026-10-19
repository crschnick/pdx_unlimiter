package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.info.InfoNodes;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Eu5RulerComp extends SavegameInfoComp {

    private Ruler ruler;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var countryId =
                NodePointer.builder().name("played_country").name("country").build();
        var country = NodePointer.builder()
                .name("countries")
                .name("database")
                .pointerEvaluation(countryId)
                .build();
        for (String key : getKeys()) {
            var found = Ruler.fromCountryNode(key, content.get(), country.get(content.get()));
            if (found.isPresent()) {
                ruler = found.get();
                break;
            }
        }
        if (ruler == null) {
            ruler = getDefault();
        }
    }

    protected List<String> getKeys() {
        return List.of("ruler", "active_regent");
    }

    private static Region createRulerStatsNode(Ruler ruler) {
        return createPowersNode((int) ruler.adm(), (int) ruler.dip(), (int) ruler.mil());
    }

    private static Region createPowersNode(int admP, int dipP, int milP) {
        return InfoNodes.createValueRow(List.of(
                new InfoNodes.ValueEntry(String.valueOf(admP), EU5_ICON_ADM),
                new InfoNodes.ValueEntry(String.valueOf(dipP), EU5_ICON_DIP),
                new InfoNodes.ValueEntry(String.valueOf(milP), EU5_ICON_MIL)));
    }

    protected Image getIcon() {
        return EU5_ICON_RULER;
    }

    protected Ruler getDefault() {
        return new Ruler("MISSING", null, -1, -1, -1);
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (ruler == null) {
            return null;
        }

        var firstNames = new ArrayList<String>();
        for (String s : ruler.firstName().split("\\.")) {
            firstNames.add(GameLocalisation.getLocalisedValue(s, data));
        }

        var name = String.join(" ", firstNames)
                + (ruler.nickname() != null
                        ? " (" + GameLocalisation.getLocalisedValue(ruler.nickname(), data) + ")"
                        : "");
        return InfoNodes.createRulerNode(getIcon(), name, name, createRulerStatsNode(ruler));
    }

    public static record Ruler(String firstName, String nickname, double adm, double dip, double mil) {

        public static Optional<Ruler> fromCountryNode(String key, Node root, Node n) {
            var rulerIdPointer =
                    NodePointer.builder().name("government").name(key).build();
            var rulerPointer = NodePointer.builder()
                    .name("character_db")
                    .name("database")
                    .supplier(() -> {
                        var idNode = rulerIdPointer.get(n);
                        return idNode != null ? idNode.getString() : null;
                    })
                    .build();
            var rulerNode = rulerPointer.get(root);
            if (rulerNode == null) {
                return Optional.empty();
            }

            var adm = rulerNode
                    .getNodeForKeyIfExistent("adm")
                    .map(Node::getDouble)
                    .orElse(0.0);
            var dip = rulerNode
                    .getNodeForKeyIfExistent("dip")
                    .map(Node::getDouble)
                    .orElse(0.0);
            var mil = rulerNode
                    .getNodeForKeyIfExistent("mil")
                    .map(Node::getDouble)
                    .orElse(0.0);

            var nameNode = rulerNode.getNodeForKey("first_name");
            var name = nameNode.isArray()
                    ? nameNode.getNodeForKeyIfExistent("custom_name")
                            .orElse(nameNode.getNodeForKey("name"))
                            .getString()
                    : nameNode.getString();
            var nickname = rulerNode
                    .getNodeForKeyIfExistent("nickname")
                    .map(Node::getString)
                    .orElse(null);
            return Optional.of(new Ruler(name, nickname, adm, dip, mil));
        }
    }
}
