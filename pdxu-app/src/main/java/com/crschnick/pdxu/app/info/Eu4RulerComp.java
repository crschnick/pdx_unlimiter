package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.model.GameDateType;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Eu4RulerComp extends SavegameInfoComp {

    private Ruler ruler;

    public Eu4RulerComp(ArrayNode node, SavegameData data) {
        super(node, data);
    }

    @Override
    protected void init(ArrayNode node, SavegameData data) {
        var rulerNode = NodePointer.builder()
                .name("countries")
                .name(data.eu4().getTag().getTag())
                .build().getIfPresent(node);
        ruler = rulerNode.flatMap(rn -> Ruler.fromCountryNode(rn, getRulerKey())).orElse(getDefault());
    }

    protected String getRulerKey() {
        return "monarch";
    }

    private static Region createRulerStatsNode(Ruler ruler) {
        return createPowersNode(ruler.adm(), ruler.dip(), ruler.mil());
    }

    private static Region createPowersNode(int admP, int dipP, int milP) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Label adm = new Label(admP + "  ", imageNode(EU4_ICON_ADM, CLASS_POWER_ICON));
        box.getChildren().add(adm);

        Label dip = new Label(dipP + "  ", imageNode(EU4_ICON_DIP, CLASS_POWER_ICON));
        box.getChildren().add(dip);

        Label mil = new Label(String.valueOf(milP), imageNode(EU4_ICON_MIL, CLASS_POWER_ICON));
        box.getChildren().add(mil);
        return box;
    }

    protected Image getIcon() {
        return EU4_ICON_RULER;
    }

    protected Ruler getDefault() {
        return new Ruler("MISSING", "MISSING RULER", -1, -1, -1);
    }

    @Override
    public Region create() {
        VBox box = new VBox();
        var img = getIcon();

        var label = new Label(ruler.name());
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var hb = new HBox(imageNode(img, CLASS_RULER_ICON), label);
        hb.setAlignment(Pos.CENTER);
        hb.setSpacing(5);
        box.getChildren().add(hb);

        box.alignmentProperty().set(Pos.CENTER);
        box.getChildren().add(createRulerStatsNode(ruler));
        box.getStyleClass().add(CLASS_RULER);
        GuiTooltips.install(box, ruler.fullName());
        return box;
    }

    public static record Ruler(
            String name,
            String fullName,
            int adm,
            int dip,
            int mil) {

        public static Optional<Ruler> fromCountryNode(Node n, String t) {
            if (!n.hasKey(t)) {
                return Optional.empty();
            }

            int personId = n.getNodeForKey(t).getNodeForKey("id").getInteger();
            AtomicReference<Optional<Ruler>> current = new AtomicReference<>(Optional.empty());
            n.getNodeForKey("history").forEach((k, v) -> {
                for (String type : new String[] {"monarch_heir", "monarch", "queen", "heir"}) {
                    if (GameDateType.EU4.isDate(k) && v.hasKey(type)) {
                        // Sometimes there are multiple monarchs in one event Node ... wtf?
                        for (Node r : v.getNodesForKey(type)) {
                            if (!r.hasKey("id")) {
                                continue;
                            }

                            int rId = r.getNodeForKey("id").getNodeForKey("id").getInteger();
                            if (rId == personId) {
                                String name = r.getNodeForKey("name").getString();
                                String fullName = name;
                                if (r.hasKey("dynasty")) {
                                    fullName = name + " " + r.getNodeForKey("dynasty").getString();
                                }
                                current.set(Optional.of(new Ruler(
                                        name,
                                        fullName,
                                        r.getNodeForKey("ADM").getInteger(),
                                        r.getNodeForKey("DIP").getInteger(),
                                        r.getNodeForKey("MIL").getInteger())));
                                return;
                            }
                        }
                    }
                }
            });
            return current.get();
        }
    }

}
