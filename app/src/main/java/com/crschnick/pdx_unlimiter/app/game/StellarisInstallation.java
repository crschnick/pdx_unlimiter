package com.crschnick.pdx_unlimiter.app.game;

import java.nio.file.Path;
import java.util.Optional;

public class StellarisInstallation extends GameInstallation {

    public StellarisInstallation(Path path) {
        super(path);
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return Optional.empty();
    }

    @Override
    public void start() {

    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Path getExecutable() {
        return null;
    }

    @Override
    public Path getUserPath() {
        return null;
    }

    @Override
    public Path getSavegamesPath() {
        return null;
    }
}
