package com.paradox_challenges.eu4_unlimiter;

import com.paradox_challenges.eu4_unlimiter.converter.Eu4Converter;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4NormalParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

public class CommandLine {

    public static void main(String[] args) throws InterruptedException, IOException {
        Optional<Node> node = new Eu4NormalParser().parse(new FileInputStream(new File("C:\\Users\\cschn\\Documents\\Paradox Interactive\\Europa Universalis IV\\save games\\autosave.eu4")));
        new Eu4Converter().extract(node.get(), "C:\\Users\\cschn\\Desktop\\test_eu4\\out.zip", false);
    }
}
