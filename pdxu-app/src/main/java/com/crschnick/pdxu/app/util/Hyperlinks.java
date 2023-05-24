package com.crschnick.pdxu.app.util;

public class Hyperlinks {

    public static final String MAIN_PAGE = "https://github.com/crschnick/pdx_unlimiter/";
    public static final String GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide";
    public static final String LAUNCH_GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#launching-savegames";
    public static final String SAVESCUM_GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/User-Guide#savegame-checkpoints-and-savescumming";
    public static final String EDITOR_GUIDE = "https://github.com/crschnick/pdx_unlimiter/wiki/Editor-Guide";
    public static final String DISCORD = "https://discord.com/invite/BVE4vxqFpU";
    public static final String NEW_ISSUE = "https://github.com/crschnick/pdx_unlimiter/issues/new";
    public static final String TRANSLATION_ISSUE = "https://github.com/crschnick/pdx_unlimiter/issues/58";
    public static final String XPIPE = "https://github.com/xpipe-io/xpipe";

    public static final String CK3_TO_EU4_DOWNLOADS = "https://github.com/ParadoxGameConverters/CK3toEU4/releases";
    public static final String EU4_SE_MAIN_PAGE = "https://forum.paradoxplaza.com/forum/threads/save-game-editor.1450703/";
    public static final String RELEASES = "https://github.com/crschnick/pdx_unlimiter/releases/";
    public static final String CK3_COA_WIKI = "https://ck3.paradoxwikis.com/Coat_of_arms_modding";

    public static void open(String url) {
        ThreadHelper.browse(url);
    }
}
