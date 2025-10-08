package com.crschnick.pdxu.app;

import com.crschnick.pdxu.app.core.AppInit;
import com.crschnick.pdxu.app.core.AppProperties;

public class Main {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("version")) {
            AppProperties.init(args);
            System.out.println(AppProperties.get().getVersion());
            return;
        }

        AppInit.init(args);
    }
}
