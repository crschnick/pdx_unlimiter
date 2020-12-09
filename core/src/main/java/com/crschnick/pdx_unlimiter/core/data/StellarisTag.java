package com.crschnick.pdx_unlimiter.core.data;

import com.crschnick.pdx_unlimiter.core.parser.Node;

public class StellarisTag {

    private String name;

    private String iconCategory;
    private String iconFile;

    private String backgroundCategory;
    private String backgroundFile;
    private String backgroundPrimaryColor;
    private String backgroundSecondaryColor;

    public StellarisTag(String name, String iconCategory, String iconFile, String backgroundCategory, String backgroundFile, String backgroundPrimaryColor, String backgroundSecondaryColor) {
        this.name = name;
        this.iconCategory = iconCategory;
        this.iconFile = iconFile;
        this.backgroundCategory = backgroundCategory;
        this.backgroundFile = backgroundFile;
        this.backgroundPrimaryColor = backgroundPrimaryColor;
        this.backgroundSecondaryColor = backgroundSecondaryColor;
    }

    private StellarisTag() {}

    public static StellarisTag fromNode(Node node) {
        Node flagNode = Node.getNodeForKey(node, "flag");
        StellarisTag tag = new StellarisTag();
        tag.name = Node.getString(Node.getNodeForKey(node, "name"));

        Node icon = Node.getNodeForKey(flagNode, "icon");
        tag.iconCategory = Node.getString(Node.getNodeForKey(icon, "category"));
        tag.iconFile = Node.getString(Node.getNodeForKey(icon, "file"));

        Node bg = Node.getNodeForKey(flagNode, "background");
        tag.backgroundCategory = Node.getString(Node.getNodeForKey(bg, "category"));
        tag.backgroundFile = Node.getString(Node.getNodeForKey(bg, "file"));

        var colors = Node.getNodeArray(Node.getNodeForKey(flagNode, "colors"));
        tag.backgroundPrimaryColor = Node.getString(colors.get(0));
        tag.backgroundSecondaryColor = Node.getString(colors.get(1));
        return tag;
    }

    public String getName() {
        return name;
    }

    public String getIconCategory() {
        return iconCategory;
    }

    public String getIconFile() {
        return iconFile;
    }

    public String getBackgroundCategory() {
        return backgroundCategory;
    }

    public String getBackgroundFile() {
        return backgroundFile;
    }

    public String getBackgroundPrimaryColor() {
        return backgroundPrimaryColor;
    }

    public String getBackgroundSecondaryColor() {
        return backgroundSecondaryColor;
    }
}
