package com.crschnick.pdx_unlimiter.app.core.settings;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.lang.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.InvalidInstallationException;
import com.crschnick.pdx_unlimiter.app.util.integration.SteamHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class SettingsEntry<T> {

    private static final Logger logger = LoggerFactory.getLogger(SettingsEntry.class);

    /**
     * Supplies the displayed name of this entry in the settings menu.
     */
    protected final Supplier<String> name;

    /**
     * Supplies the displayed description of this entry in the settings menu.
     */
    protected final Supplier<String> description;

    /**
     * The key name that will be used for the entry when being saved to a file.
     */
    protected final String serializationName;

    /**
     * The value type of this settings entry.
     */
    protected final Type type;

    /**
     * The value of this settings entry.
     */
    protected ObjectProperty<T> value;

    /**
     * Creates a new settings entry using an i18n key name, a serialization name, and a type.
     *
     * @param id the i18n key name
     * @param serializationName the key name that will be used for the entry when being saved to a file
     * @param type the type of the entry
     */
    public SettingsEntry(String id, String serializationName, Type type) {
        this.name = () -> PdxuI18n.get(id);
        this.serializationName = serializationName;
        this.description = () -> PdxuI18n.get(id + "_DESC");
        this.type = type;
        this.value = new SimpleObjectProperty<>();
        setupLogger();
    }

    /**
     * Creates a new settings entry with support for a dynamic name and description.
     *
     * @param name a supplier for the displayed name (should be localized)
     * @param description a supplier for the displayed description (should be localized)
     * @param serializationName the key name that will be used for the entry when being saved to a file
     * @param type the type of the entry
     *
     * @see SettingsEntry#SettingsEntry(String, String, Type)
     */
    public SettingsEntry(Supplier<String> name, Supplier<String> description, String serializationName, Type type) {
        this.name = name;
        this.description = description;
        this.serializationName = serializationName;
        this.type = type;
        this.value = new SimpleObjectProperty<>();
        setupLogger();
    }

    private void setupLogger() {
        this.value.addListener((c, o, n) -> {
            logger.trace("Changing settings entry " + serializationName + " from " + o + " to " + n);
        });
    }

    /**
     * Sets the value using a Json node.
     *
     * @param node the json node
     */
    public abstract void set(JsonNode node);

    /**
     * Converts the value into a Json node.
     * Should be compatible with {@link #set(JsonNode)}.
     *
     * @return the json node
     */
    public abstract JsonNode toNode();

    /**
     * Sets the value of this entry.
     * This method can be overridden to enable custom behaviour when changing the value.
     *
     * @param newValue the new value
     */
    public void set(T newValue) {
        value.set(newValue);
    }

    /**
     * Sets the value of this entry to a default value.
     * This method will be called when no value has been
     * set for this entry when parsing a settings file.
     */
    public abstract void setDefault();

    public final String getName() {
        return name.get();
    }

    public final String getDescription() {
        return description.get();
    }

    public final Type getType() {
        return type;
    }

    public final T getValue() {
        return value.get();
    }

    public final String getSerializationName() {
        return serializationName;
    }

    public enum Type {
        BOOLEAN,
        INTEGER,
        STRING,
        DIRECTORY,
        CHOICE
    }


    public static abstract class SimpleEntry<T> extends SettingsEntry<T> {

        private final T defaultValue;

        public SimpleEntry(String id, String serializationName, Type type, T defaultValue) {
            super(id, serializationName, type);
            this.defaultValue = defaultValue;
        }

        @Override
        public final void setDefault() {
            this.value.set(defaultValue);
        }
    }

    public static class BooleanEntry extends SimpleEntry<Boolean> {

        public BooleanEntry(String id, String serializationName, boolean defaultValue) {
            super(id, serializationName, Type.BOOLEAN, defaultValue);
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

    public static class IntegerEntry extends SimpleEntry<Integer> {

        private final int min;
        private final int max;

        public IntegerEntry(String id, String serializationName, int defaultValue, int min, int max) {
            super(id, serializationName, Type.INTEGER, defaultValue);
            this.min = min;
            this.max = max;
        }

        @Override
        public void set(Integer newValue) {
            int usedValue = Math.max(min, Math.min(newValue, max));
            super.set(usedValue);
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

    public static class StringEntry extends SimpleEntry<String> {

        public StringEntry(String id, String serializationName, String defaultValue) {
            super(id, serializationName, Type.STRING, defaultValue);
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

    public static class ChoiceEntry<T> extends SimpleEntry<T> {

        private final Function<T,String> displayNameFunc;
        private final BidiMap<T,String> mapping;

        public ChoiceEntry(String id, String serializationName, T defaultValue, BidiMap<T,String> mapping, Function<T,String> displayNameFunc) {
            super(id, serializationName, Type.CHOICE, defaultValue);
            this.mapping = mapping;
            this.displayNameFunc = displayNameFunc;
        }

        @Override
        public void set(JsonNode node) {
            var name = node.textValue();
            var val = mapping.inverseBidiMap().get(name);

            if (val != null) {
                this.value.set(val);
            } else {
                String fullMsg = PdxuI18n.get("CHOICE_VALUE_ERROR", name);
                GuiErrorReporter.showSimpleErrorMessage(fullMsg);
                this.value.set(mapping.keySet().iterator().next());
            }
        }

        @Override
        public JsonNode toNode() {
            return new TextNode(mapping.get(value.get()));
        }

        public BidiMap<T, String> getMapping() {
            return mapping;
        }

        public Function<T, String> getDisplayNameFunc() {
            return displayNameFunc;
        }
    }

    public static abstract class FailableDirectoryEntry extends SettingsEntry<Path> {

        protected boolean disabled;

        public FailableDirectoryEntry(String id, String serializationName) {
            super(id, serializationName, Type.DIRECTORY);
            this.disabled = false;
        }

        public FailableDirectoryEntry(Supplier<String> name, Supplier<String> description, String serializationName) {
            super(name, description, serializationName, Type.DIRECTORY);
            this.disabled = false;
        }

        @Override
        public final void set(JsonNode node) {
            if (node.isNull()) {
                this.disabled = true;
                this.value.set(null);
            } else {
                this.set(Path.of(node.textValue()));
            }
        }

        @Override
        public final JsonNode toNode() {
            if (disabled) {
                return NullNode.getInstance();
            } else if (value.isNull().get()) {
                return null;
            } else {
                return new TextNode(value.get().toString());
            }
        }
    }

    public static class GameDirectory extends FailableDirectoryEntry {

        private final Game game;
        private final Class<? extends GameInstallation> installClass;

        GameDirectory(String serializationName, Game game, Class<? extends GameInstallation> installClass) {
            super(() -> PdxuI18n.get("GAME_DIR", game.getAbbreviation()),
                    () -> PdxuI18n.get("GAME_DIR_DESC", game.getAbbreviation()),
                    serializationName);
            this.game = game;
            this.installClass = installClass;
        }

        private void showInstallErrorMessage(String msg) {
            String fullMsg = PdxuI18n.get("GAME_DIR_ERROR", game.getFullName()) + ":\n" +
                    msg + "\n\n" + PdxuI18n.get("GAME_DIR_ERROR_MSG", game.getFullName());
            GuiErrorReporter.showSimpleErrorMessage(fullMsg);
        }

        @Override
        public void set(Path newPath) {
            if (newPath == null) {
                this.value.set(null);
                return;
            }

            if (newPath.equals(value.get())) {
                return;
            }

            try {
                var i = (GameInstallation) installClass.getDeclaredConstructors()[0].newInstance(newPath);
                GameInstallation.initTemporary(game, i);
                this.value.set(newPath);
                this.disabled = false;
            } catch (InvalidInstallationException e) {
                if (value.get() == null) {
                    this.disabled = true;
                }
                showInstallErrorMessage(e.getLocalisedMessage());
            } catch (Exception e) {
                if (value.get() == null) {
                    this.disabled = true;
                }
                showInstallErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        @Override
        public void setDefault() {
            SteamHelper.getSteamGameInstallPath(game.getFullName()).ifPresent(p -> {
                this.set(p);
            });
        }
    }

    public static class StorageDirectory extends SettingsEntry<Path> {

        public StorageDirectory(String id, String serializationName) {
            super(id, serializationName, Type.DIRECTORY);
        }

        private boolean showConfirmationDialog(String old, String newDir) {
            return GuiDialogHelper.showBlockingAlert(a -> {
                a.setAlertType(Alert.AlertType.CONFIRMATION);
                a.setTitle(PdxuI18n.get("STORAGE_DIR_DIALOG_TITLE"));
                a.setHeaderText(PdxuI18n.get("STORAGE_DIR_DIALOG_TEXT", old, newDir));
            }).map(t -> t.getButtonData().isDefaultButton()).orElse(false);
        }

        @Override
        public void set(JsonNode node) {
            this.value.set(Path.of(node.textValue()));
        }

        @Override
        public JsonNode toNode() {
            return new TextNode(this.value.get().toString());
        }

        @Override
        public void set(Path newPath) {
            Objects.requireNonNull(newPath);

            if (newPath.equals(value.get())) {
                return;
            }

            if (!showConfirmationDialog(value.get().toString(), newPath.toString())) {
                return;
            }

            if (FileUtils.listFiles(newPath.toFile(), null, true).size() > 0) {
                GuiErrorReporter.showSimpleErrorMessage("New storage directory " + newPath + " must be empty!");
            } else {
                try {
                    Files.delete(newPath);
                    FileUtils.moveDirectory(value.get().toFile(), newPath.toFile());
                    this.value.set(newPath);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }
        }

        @Override
        public void setDefault() {
            this.value.set(PdxuInstallation.getInstance().getDefaultSavegamesLocation());
        }
    }

    public static class ThirdPartyDirectory extends FailableDirectoryEntry {

        private final Path checkFile;
        private final Supplier<Path> defaultValue;

        public ThirdPartyDirectory(String id, String serializationName, Path checkFile, Supplier<Path> defaultValue) {
            super(id, serializationName);
            this.checkFile = checkFile;
            this.defaultValue = defaultValue;
        }

        @Override
        public void set(Path newPath) {
            if (newPath == null) {
                this.value.set(null);
                return;
            }


            if (newPath.equals(value.get())) {
                return;
            }

            var file = newPath.resolve(checkFile);
            boolean found = Files.exists(file);
            if (!found) {
                showErrorMessage();
                this.disabled = true;
                this.value.set(null);
            } else {
                super.set(newPath);
            }
        }

        @Override
        public void setDefault() {
            var df = defaultValue.get();
            if (df == null) {
                this.value.set(null);
                this.disabled = false;
                return;
            }

            var file = df.resolve(checkFile);
            boolean found = Files.exists(file);
            if (found) {
                this.value.set(df);
            }
        }

        private void showErrorMessage() {
            GuiErrorReporter.showSimpleErrorMessage(PdxuI18n.get("THIRD_PARTY_ERROR"));
        }
    }
}
