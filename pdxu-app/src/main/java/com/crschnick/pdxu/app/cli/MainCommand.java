package com.crschnick.pdxu.app.cli;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.ComponentManager;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(subcommands = {RenderCommand.class})
class MainCommand implements Runnable {


    @CommandLine.Parameters(
            description = "Optional input files",
            paramLabel = "<inputs>"
    )
    List<String> inputs = List.of();

    public static void main(String[] args) {
        MainCommand app = new MainCommand();
        new CommandLine(app)
                .execute(args);
    }

    @Override
    public void run() {
        ComponentManager.initialSetup(inputs);
        PdxuApp.main();
    }
}
