package com.crschnick.pdx_unlimiter.app.core.settings;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.InstallLocationHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class SettingsEntry<T> {

    static class BooleanEntry extends SettingsEntry<Boolean> {

        public BooleanEntry(String id, String serializationName, boolean value) {
            super(id, serializationName, Type.BOOLEAN, value);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(node.booleanValue());
        }

        @Override
        public JsonNode toNode() {
            return BooleanNode.valueOf(value.get());
        }
    }

    static class IntegerEntry extends SettingsEntry<Integer> {

        private final int min;
        private final int max;

        public IntegerEntry(String id, String serializationName, int value, int min, int max) {
            super(id, serializationName, Type.INTEGER, value);
            this.min = min;
            this.max = max;
        }

        @Override
        protected void set(Integer newValue) {
            //TODO check range
            super.set(newValue);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(node.intValue());
        }

        @Override
        public JsonNode toNode() {
            return new IntNode(value.get());
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    static class StringEntry extends SettingsEntry<String> {

        public StringEntry(String id, String serializationName, String value) {
            super(id, serializationName, Type.STRING, value);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(node.textValue());
        }

        @Override
        public JsonNode toNode() {
            return new TextNode(value.get());
        }
    }

    static abstract class DirectoryEntry extends SettingsEntry<Path> {

        public DirectoryEntry(String id, String serializationName, Path value) {
            super(id, serializationName, Type.PATH, value);
        }

        @Override
        public void set(JsonNode node) {
            this.set(Path.of(node.textValue()));
        }

        @Override
        public JsonNode toNode() {
            return new TextNode(this.value.get().toString());
        }
    }

    static class GameDirectory extends DirectoryEntry {

        private boolean disabled;
        private final Class<? extends GameInstallation> installClass;

        GameDirectory(String id, String serializationName, String name, Class<? extends GameInstallation> installClass) {
            super(id, serializationName, null);
            this.disabled = false;
            this.installClass = installClass;
            InstallLocationHelper.getSteamGameInstallPath(name).ifPresent(p -> set(p));
        }

        private void showInstallErrorMessage(Exception e, String name) {
            String msg = e.getClass().getSimpleName() + ": " + e.getMessage() +
                    ".\n\n" + name + " support has been disabled.\n" +
                    "If you believe that your installation is valid, " +
                    "please check in the settings menu whether the installation directory was correctly set.";
            GuiErrorReporter.showSimpleErrorMessage(
                    "An error occured while loading your " + name + " installation:\n" + msg);
        }

        @Override
        protected void set(Path newPath) {
            if (disabled) {
                disabled = false;
            }

            String name = null;
            try {
                var i = (GameInstallation) installClass.getDeclaredConstructors()[0].newInstance(newPath);
                name = i.getId();
                i.loadData();
                super.set(newPath);
            } catch (Exception e) {
                this.disabled = true;
                showInstallErrorMessage(e, name);
            }
        }

        @Override
        public void set(JsonNode node) {
            if (!node.textValue().equals("disabled")) {
                this.disabled = true;
                this.value.set(null);
            } else {
                this.disabled = false;
                this.value.set(Path.of(node.textValue()));
            }
        }

        @Override
        public JsonNode toNode() {
            if (disabled) {
                return new TextNode("disabled");
            } else {
                return new TextNode(value.get().toString());
            }
        }
    }


    static class StorageDirectory extends DirectoryEntry {

        public StorageDirectory(String id, String serializationName) {
            super(id, serializationName, PdxuInstallation.getInstance().getDefaultSavegamesLocation());
        }

        @Override
        protected void set(Path newPath) {
            if (FileUtils.listFiles(newPath.toFile(), null, false).size() > 0) {
                GuiErrorReporter.showSimpleErrorMessage("New storage directory " + newPath + " must be empty!");
            } else {
                try {
                    Files.delete(newPath);
                    FileUtils.moveDirectory(value.get().toFile(), newPath.toFile());
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
                this.value.set(newPath);
            }
        }
    }

    static class ThirdPartyDirectory extends DirectoryEntry {

        private final Path checkFile;

        public ThirdPartyDirectory(String id, String serializationName, Path checkFile) {
            super(id, serializationName, null);
            this.checkFile = checkFile;
        }


        @Override
        protected void set(Path newPath) {
            var file = newPath.resolve(checkFile);
            boolean found = Files.exists(file);
            if (!found) {
                showErrorMessage();
            } else {
                this.value.set(newPath);
            }
        }

        private void showErrorMessage() {
            String msg = PdxuI18n.get(name) + " support has been disabled.\n" +
                    "If you believe that the installation is valid, " +
                    "please check whether the installation directory was correctly set.";
            GuiErrorReporter.showSimpleErrorMessage(msg);
        }
    }

    public static enum Type {
        BOOLEAN,
        INTEGER,
        STRING,
        PATH
    }

    protected final String name;
    protected final String description;
    protected final String serializationName;
    protected final Type type;
    protected ObjectProperty<T> value;

    public SettingsEntry(String id, String serializationName, Type type, T value) {
        this.name = PdxuI18n.get(id);
        this.serializationName = serializationName;
        this.description = PdxuI18n.get(id + "_DESC");
        this.type = type;
        this.value = new SimpleObjectProperty<>(value);
    }

    public abstract void set(JsonNode node);

    public abstract JsonNode toNode();

    protected void set(T newValue) {
        value.set(newValue);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return value.get();
    }

    public String getSerializationName() {
        return serializationName;
    }
}
