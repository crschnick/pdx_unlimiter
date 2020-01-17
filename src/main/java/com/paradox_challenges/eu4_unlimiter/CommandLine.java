package com.paradox_challenges.eu4_unlimiter;

import com.paradox_challenges.eu4_unlimiter.converter.Eu4Converter;

import java.io.IOException;

public class CommandLine {

    public static void main(String[] args) throws InterruptedException, IOException {
        new Eu4Converter().extract("C:\\Users\\cschn\\Desktop\\test_eu4\\test.eu4", "C:\\Users\\cschn\\Desktop\\test_eu4\\out");
    }
}
