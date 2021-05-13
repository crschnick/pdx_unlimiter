package com.crschnick.pdxu.app;

import com.crschnick.pdxu.app.core.ComponentManager;


public class Main {

    public static void main(String[] args) {
        ComponentManager.initialSetup(args);
        PdxuApp.main(args);
    }
}
