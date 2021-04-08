package com.crschnick.pdx_unlimiter.app.core.settings;

public abstract class AbstractSettings {

    private final String name;

    public AbstractSettings() {
        this.name = createName();
    }

    public final void load() {
        SettingsIO.load(this);
        check();
        SettingsIO.save(this);
    }

    public final void update(Runnable r) {
        r.run();
        check();
        SettingsIO.save(this);
    }

    protected abstract String createName();

    public abstract void check();

    public final String getName() {
        return name;
    }
}
