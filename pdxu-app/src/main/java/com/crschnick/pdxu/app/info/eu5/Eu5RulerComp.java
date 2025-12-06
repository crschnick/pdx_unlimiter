package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Label adm = new Label(admP + "  ", imageNode(EU5_ICON_ADM, CLASS_POWER_ICON));
        adm.setEllipsisString("");
        box.getChildren().add(adm);

        Label dip = new Label(dipP + "  ", imageNode(EU5_ICON_DIP, CLASS_POWER_ICON));
        dip.setEllipsisString("");
        box.getChildren().add(dip);

        Label mil = new Label(String.valueOf(milP), imageNode(EU5_ICON_MIL, CLASS_POWER_ICON));
        mil.setEllipsisString("");
        box.getChildren().add(mil);
        return box;
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

        VBox box = new VBox();
        var img = getIcon();

        var firstNames = new ArrayList<String>();
        for (String s : ruler.firstName().split("\\.")) {
            firstNames.add(GameLocalisation.getLocalisedValue(s, data));
        }

        var name = String.join(" ", firstNames) + (ruler.nickname() != null ? " (" + ruler.nickname() + ")" : "");
        var label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var hb = new HBox(imageNode(img, CLASS_RULER_ICON), label);
        hb.setAlignment(Pos.CENTER);
        hb.setSpacing(5);
        box.getChildren().add(hb);

        box.alignmentProperty().set(Pos.CENTER);
        box.getChildren().add(createRulerStatsNode(ruler));
        box.getStyleClass().add(CLASS_RULER);
        GuiTooltips.install(box, ruler.firstName());
        return box;
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
            var name = nameNode.isArray() ?
                    nameNode.getNodeForKeyIfExistent("custom_name").orElse(nameNode.getNodeForKey("name")).getString() :
                    nameNode.getString();
            var nickname = rulerNode
                    .getNodeForKeyIfExistent("nickname")
                    .map(Node::getString)
                    .orElse(null);
            return Optional.of(new Ruler(name, nickname, adm, dip, mil));
        }
    }
}
