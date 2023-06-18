package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.installation.Game;

public class SavegameBranches {

    public static boolean supportsBranching(SavegameEntry<?,?> e) {
        var ctx = SavegameContext.getContext(e);

        // We can't change the name of ironman save games here!
        if (ctx.getGame() == Game.CK2) {
            return !ctx.getInfo().getData().isIronman();
        }

        // VIC2 does not support rewriting yet for non-ironman
        if (ctx.getGame() == Game.VIC2) {
            return ctx.getInfo().getData().isIronman();
        }

        return true;
    }

    public static boolean supportsRewrite(SavegameEntry<?,?> e) {
        return !e.getInfo().getData().isIronman() && !e.getInfo().getData().isBinary();
    }
}
