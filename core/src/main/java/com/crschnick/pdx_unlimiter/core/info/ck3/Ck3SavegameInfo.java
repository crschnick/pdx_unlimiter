package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.War;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Ck3SavegameInfo extends SavegameInfo<Ck3Tag> {

    protected Ck3Tag tag;
    protected Set<Ck3Tag> allTags;
    private String playerName;
    private String houseName;
    private List<War<Ck3Tag>> wars;
    private List<Ck3Tag> allies;

    public static Ck3SavegameInfo fromSavegame(boolean melted, Node n) throws SavegameParseException {
        Ck3SavegameInfo i = new Ck3SavegameInfo();
        try {
            i.ironman = melted;
            i.binary = i.ironman;
            i.date = GameDateType.CK3.fromString(n.getNodeForKey("date").getString());

            long seed = n.getNodeForKey("random_seed").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            i.campaignHeuristic = UUID.nameUUIDFromBytes(b);

            i.allTags = Ck3Tag.fromNode(n);
            i.tag = Ck3Tag.getPlayerTag(n, i.allTags);

            i.mods = n.getNodeForKeyIfExistent("mods").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());
            i.dlcs = List.of();

            i.playerName = n.getNodeForKey("meta_data").getNodeForKey("meta_player_name").getString();
            i.houseName = n.getNodeForKey("meta_data").getNodeForKey("meta_house_name").getString();

            Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher m = p.matcher(n.getNodeForKey("meta_data").getNodeForKey("version").getString());
            m.matches();
            i.version = new GameVersion(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    0,
                    null);

            i.wars = fromActiveWarsNode(i.allTags, i.tag, n);

            i.allies = new ArrayList<>();
            for (Node rel : n.getNodeForKey("relations").getNodeForKey("active_relations").getNodeArray()) {
                if (!rel.hasKey("alliances")) {
                    continue;
                }

                var first = rel.getNodeForKey("first").getLong();
                var second = rel.getNodeForKey("second").getLong();
                if (first == i.tag.getId()) {
                    Ck3Tag.getTag(i.allTags, second).ifPresent(t -> i.allies.add(t));
                }
                if (second == i.tag.getId()) {
                    Ck3Tag.getTag(i.allTags, first).ifPresent(t -> i.allies.add(t));
                }
            }

        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    private static List<War<Ck3Tag>> fromActiveWarsNode(Set<Ck3Tag> tags, Ck3Tag tag, Node n) {
        List<War<Ck3Tag>> wars = new ArrayList<>();
        n.getNodeForKey("wars").getNodeForKey("active_wars").getNodeArray().forEach(v -> {
            if (v.isValue() && v.getString().equals("none")) {
                return;
            }

            var title = v.getNodeForKey("name").getString();

            List<Ck3Tag> attackers = new ArrayList<>();
            for (Node atk : v.getNodeForKey("attacker").getNodeForKey("participants").getNodeArray()) {
                var attacker = atk.getNodeForKey("character").getLong();
                Ck3Tag.getTag(tags, attacker).ifPresent(attackers::add);
            }

            List<Ck3Tag> defenders = new ArrayList<>();
            for (Node atk : v.getNodeForKey("defender").getNodeForKey("participants").getNodeArray()) {
                var defender = atk.getNodeForKey("character").getLong();
                Ck3Tag.getTag(tags, defender).ifPresent(defenders::add);
            }

            if (attackers.contains(tag) || defenders.contains(tag)) {
                wars.add(new War<>(title, attackers, defenders));
            }
        });
        return wars;
    }

    @Override
    public Ck3Tag getTag() {
        return tag;
    }

    @Override
    public Set<Ck3Tag> getAllTags() {
        return allTags;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getHouseName() {
        return houseName;
    }

    public List<War<Ck3Tag>> getWars() {
        return wars;
    }

    public List<Ck3Tag> getAllies() {
        return allies;
    }
}
