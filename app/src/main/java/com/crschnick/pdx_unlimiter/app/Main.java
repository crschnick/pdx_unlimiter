package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameManagerApp;

import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        SavegameManagerApp.main(args);
    }
}
