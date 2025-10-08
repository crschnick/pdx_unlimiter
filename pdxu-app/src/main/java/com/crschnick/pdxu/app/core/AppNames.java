package com.crschnick.pdxu.app.core;

public abstract class AppNames {

    public static AppNames ofMain() {
        return new Main();
    }

    public static AppNames ofCurrent() {
        return new Main();
    }

    public static String propertyName(String name) {
        return ofCurrent().getGroupName() + "." + ofCurrent().getArtifactName() + "." + name;
    }

    public static String packageName() {
        return packageName(null);
    }

    public static String packageName(String name) {
        return ofCurrent().getGroupName() + "." + ofCurrent().getArtifactName() + (name != null ? "." + name : "");
    }

    public abstract String getName();

    public abstract String getKebapName();

    public abstract String getSnakeName();

    public abstract String getUppercaseName();

    public abstract String getGroupName();

    public abstract String getArtifactName();

    public abstract String getExecutableName();

    public abstract String getDistName();

    private static class Main extends AppNames {

        @Override
        public String getName() {
            return "Pdx-Unlimiter";
        }

        @Override
        public String getKebapName() {
            return "pdx-unlimiter";
        }

        @Override
        public String getSnakeName() {
            return "pdx_unlimiter";
        }

        @Override
        public String getUppercaseName() {
            return "PDX_UNLIMITER";
        }

        @Override
        public String getGroupName() {
            return "com.crschnick";
        }

        @Override
        public String getArtifactName() {
            return "pdxu.app";
        }

        @Override
        public String getExecutableName() {
            return "pdx-unlimiter";
        }

        @Override
        public String getDistName() {
            return "pdx-unlimiter";
        }
    }
}
