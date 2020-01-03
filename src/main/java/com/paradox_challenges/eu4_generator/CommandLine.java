package com.paradox_challenges.eu4_generator;

import com.paradox_challenges.eu4_generator.format.eu4.ProvincesTransformer;
import com.paradox_challenges.eu4_generator.format.eu4.WarTransformer;
import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.GamestateParser;
import com.paradox_challenges.eu4_generator.savegame.SavegameExtractor;

import java.io.FileOutputStream;
import java.io.IOException;

public class CommandLine {

    public static void main(String[] args) throws InterruptedException, IOException {
        new SavegameExtractor().extract("C:\\Users\\cschn\\Desktop\\test_eu4\\test.eu4", "C:\\Users\\cschn\\Desktop\\test_eu4\\out");
    }
}
