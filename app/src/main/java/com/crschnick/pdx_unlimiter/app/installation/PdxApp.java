package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.Eu4App;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;

public class PdxApp {

    private Type type;
    private ProcessHandle process;

    public PdxApp(ProcessHandle process, Type type) {
        this.process = process;
        this.type = type;
    }

    public void onStart() {
    }

    public void onShutdown() {
    }

    public Type getType() {
        return type;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public void kill() {
        process.destroyForcibly();
    }

    public static enum Type {

        EU4("Europa Universalis IV");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
