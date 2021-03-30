package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Ck3Tag {

    private long id;
    private Ck3Person ruler;
    private List<Ck3Title> titles;
    private List<Ck3Title> claims;
    private String governmentName;
    private Ck3CoatOfArms coatOfArms;
    private String name;

    public Ck3Tag() {
    }

    public Ck3Tag(long id, Ck3Person ruler, List<Ck3Title> titles, List<Ck3Title> claims, String governmentName, Ck3CoatOfArms coatOfArms, String name) {
        this.id = id;
        this.ruler = ruler;
        this.titles = titles;
        this.claims = claims;
        this.governmentName = governmentName;
        this.coatOfArms = coatOfArms;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ck3Tag ck3Tag = (Ck3Tag) o;
        return id == ck3Tag.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Optional<Ck3Tag> getTag(List<Ck3Tag> tags, long id) {
        return tags.stream().filter(t -> t.id == id).findFirst();
    }

    public static Ck3Tag getPlayerTag(Node n, List<Ck3Tag> allTags) {
        long id = n.getNodeForKey("currently_played_characters").getNodeArray().get(0).getLong();
        var personNode = n.getNodeForKey("living").getNodeForKey(String.valueOf(id));
        var house = new Ck3House(
                n.getNodeForKey("meta_data").getNodeForKey("meta_house_name").getString(),
                Ck3CoatOfArms.fromNode(n.getNodeForKey("meta_data").getNodeForKey("meta_house_coat_of_arms")));
        var person = Ck3Person.fromNode(personNode, house);
        var coaMap = Ck3CoatOfArms.createCoaMap(n.getNodeForKey("coat_of_arms")
                .getNodeForKey("coat_of_arms_manager_database"));
        var titles = Ck3Title.createTitleMap(n, coaMap);

        var domain = personNode.getNodeForKey("landed_data").getNodeForKey("domain");
        var tagTitles = domain.getNodeArray().stream()
                .map(idNode -> titles.get(idNode.getLong()))
                .collect(Collectors.toList());
        var tagClaims = new ArrayList<Ck3Title>();
        personNode.getNodeForKey("alive_data")
                .getNodeForKeyIfExistent("claim").ifPresent(claims -> {
            tagClaims.addAll(claims.getNodeArray().stream().map(c ->
                    titles.get(c.getNodeForKey("title").getLong())).collect(Collectors.toList()));
        });

        var coa = Ck3CoatOfArms.fromNode(
                n.getNodeForKey("meta_data").getNodeForKey("meta_coat_of_arms"));
        var name = n.getNodeForKey("meta_data").getNodeForKey("meta_title_name").getString();

        var gv = personNode.getNodeForKey("landed_data").getNodeForKey("government").getString();
        var tag = new Ck3Tag(id, person, tagTitles, tagClaims, gv, coa, name);
        allTags.removeIf(t -> t.id == id);
        allTags.add(tag);
        return tag;
    }

    public static List<Ck3Tag> fromNode(Node n) {
        var coaMap = Ck3CoatOfArms.createCoaMap(n.getNodeForKey("coat_of_arms")
                .getNodeForKey("coat_of_arms_manager_database"));
        Map<Long, Ck3Title> titleIds = Ck3Title.createTitleMap(n, coaMap);

        var living = n.getNodeForKey("living");
        List<Ck3Tag> allTags = new ArrayList<>();
        living.forEach((k, v) -> {
            if (!v.hasKey("landed_data")) {
                return;
            }

            long id = Long.parseLong(k);
            var domain = v.getNodeForKey("landed_data").getNodeForKey("domain");
            var primary = domain.getNodeArray().get(0).getLong();
            var gv = v.getNodeForKey("landed_data").getNodeForKey("government").getString();
            allTags.add(new Ck3Tag(id, null, List.of(titleIds.get(primary)), null, gv, null, null));
        });
        return allTags;
    }

    public Ck3Person getRuler() {
        return ruler;
    }

    public List<Ck3Title> getTitles() {
        return titles;
    }

    public List<Ck3Title> getClaims() {
        return claims;
    }

    public Ck3Title getPrimaryTitle() {
        return titles.get(0);
    }

    public long getId() {
        return id;
    }

    public String getGovernmentName() {
        return governmentName;
    }

    public Ck3CoatOfArms getCoatOfArms() {
        return coatOfArms != null ? coatOfArms : getPrimaryTitle().getCoatOfArms();
    }

    public String getName() {
        return name != null ? name : getPrimaryTitle().getName();
    }
}
