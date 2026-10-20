package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.info.InfoNodes;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Ck2RulerComp extends SavegameInfoComp {

    private String name;
    private Integer age;
    private List<Integer> skills;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        name = content.get()
                .getNodeForKeyIfExistent("player_name")
                .map(Node::getString)
                .orElse(null);
        age = content.get()
                .getNodeForKeyIfExistent("player_age")
                .map(Node::getInteger)
                .orElse(null);
        skills = data.ck2()
                .getPlayerCharacter(content)
                .flatMap(c -> c.getNodeForKeyIfExistent("att"))
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getInteger)
                .toList();
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (name == null) {
            return null;
        }

        var icons = List.of(
                CK2_SKILL_DIPLOMACY, CK2_SKILL_MARTIAL, CK2_SKILL_STEWARDSHIP, CK2_SKILL_INTRIGUE, CK2_SKILL_LEARNING);
        var entries = new ArrayList<InfoNodes.ValueEntry>();
        for (int i = 0; i < icons.size(); i++) {
            int skill = i < skills.size() ? skills.get(i) : 0;
            entries.add(new InfoNodes.ValueEntry(String.valueOf(skill), icons.get(i)));
        }
        var title = age != null ? name + ", " + age : name;
        return InfoNodes.createRulerNode(CK2_ICON_RULER, title, null, InfoNodes.createValueRow(entries));
    }
}
