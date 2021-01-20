package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Ck3SavegameInfo extends SavegameInfo<Ck3Tag> {

    protected Ck3Tag tag;
    protected Set<Ck3Tag> allTags;

    public static Ck3SavegameInfo fromSavegame(boolean melted, Node n) throws SavegameParseException {
        Ck3SavegameInfo i = new Ck3SavegameInfo();
        try {
            i.ironman = melted;
            i.date = GameDateType.CK3.fromString(n.getNodeForKey("date").getString());

            long seed = n.getNodeForKey("random_seed").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            i.campaignUuid = UUID.nameUUIDFromBytes(b);

            i.allTags = Ck3Tag.fromNode(
                    n.getNodeForKey("living"),
                    n.getNodeForKey("landed_titles"),
                    n.getNodeForKey("coat_of_arms"));
            i.tag = Ck3Tag.getPlayerTag(n, i.allTags);

            i.mods = n.getNodeForKeyIfExistent("mods").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());
            i.dlcs = List.of();

            Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher m = p.matcher(n.getNodeForKey("meta_data").getNodeForKey("version").getString());
            m.matches();
            i.version = new GameVersion(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    0,
                    null);

        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    @Override
    public Ck3Tag getTag() {
        return tag;
    }

    @Override
    public Set<Ck3Tag> getAllTags() {
        return allTags;
    }
}
