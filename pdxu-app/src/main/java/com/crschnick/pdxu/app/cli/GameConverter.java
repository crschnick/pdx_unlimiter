package com.crschnick.pdxu.app.cli;

import com.crschnick.pdxu.app.installation.Game;

import picocli.CommandLine;

import java.util.Arrays;

public class GameConverter implements CommandLine.ITypeConverter<Game> {
    @Override
    public Game convert(String value) throws Exception {
        if (Arrays.stream(Game.values()).noneMatch(game -> game.getId().equals(value))) {
            throw new IllegalArgumentException(String.format("Invalid game %s", value));
        }

        var game = Game.byId(value);

        return game;
    }
}
