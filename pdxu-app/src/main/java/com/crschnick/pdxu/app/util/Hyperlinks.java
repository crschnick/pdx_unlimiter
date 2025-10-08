package com.crschnick.pdxu.app.util;

public class Hyperlinks {

    public static final String GITHUB = "https://github.com/crschnick/pdx_unlimiter";
    public static final String DOCS = "https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide";
    public static final String TRANSLATE = "https://github.com/crschnick/pdx_unlimiter/tree/master/lang";
    public static final String LAUNCH_GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#launching-savegames";
    public static final String SAVESCUM_GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#savegame-checkpoints-and-savescumming";
    public static final String EDITOR_GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/Editor-Guide";
    public static final String DISCORD = "https://discord.com/invite/BVE4vxqFpU";
    public static final String CK3_COA_WIKI = "https://ck3.paradoxwikis.com/Coat_of_arms_modding";


    public static void open(String uri) {
        DesktopHelper.openUrlInBrowser(uri);
    }
}
