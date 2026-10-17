package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.JacksonMapper;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.crschnick.pdxu.app.prefs.PrefsChoiceValue.getAll;
import static com.crschnick.pdxu.app.prefs.PrefsChoiceValue.getSupported;

public class AppPrefsStorageHandler {

    private final Path file;
    private ObjectNode content;

    public AppPrefsStorageHandler(Path file) {
        this.file = file;
    }

    boolean isInitialized() {
        return content != null;
    }

    private JsonNode getContent(String key) {
        loadIfNeeded();
        return content.get(key);
    }

    private void loadIfNeeded() {
        if (content == null) {
            if (Files.exists(file)) {
                try {
                    var s = Files.readString(file);
                    if (!s.isEmpty()) {
                        var read = JacksonMapper.getDefault().readTree(s);
                        if (read.isObject()) {
                            content = (ObjectNode) read;
                        }
                    }
                } catch (Exception e) {
                    ErrorEventFactory.fromThrowable(e).handle();
                }

                // Keep a copy of an unreadable file so that the next save does not silently lose it
                if (content == null) {
                    backupCorruptedFile();
                }
            }

            if (content == null) {
                content = JsonNodeFactory.instance.objectNode();
            }
        }
    }

    private void backupCorruptedFile() {
        try {
            var backup = file.resolveSibling(file.getFileName() + ".bak");
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            TrackEvent.withWarn("Preferences file could not be read, created backup")
                    .tag("backup", backup)
                    .handle();
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).omit().expected().handle();
        }
    }

    private void setContent(String key, JsonNode value) {
        content.set(key, value);
    }

    @SneakyThrows
    void save() {
        FileUtils.forceMkdir(file.getParent().toFile());
        // Write to a temporary file first so that a crash during the write can't corrupt the existing file
        var temp = file.resolveSibling(file.getFileName() + ".tmp");
        JacksonMapper.getDefault().writeValue(temp.toFile(), content);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SneakyThrows
    public void updateObject(String key, Object object, JavaType type) {
        if (object instanceof PrefsChoiceValue prefsChoiceValue) {
            setContent(key, new StringNode(prefsChoiceValue.getId()));
            return;
        }

        if (object == null) {
            setContent(key, JsonNodeFactory.instance.nullNode());
            return;
        }

        var mapper = JacksonMapper.getDefault();
        setContent(key, mapper.writerFor(type).valueToTree(object));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <T> T loadObject(String id, JavaType type, T defaultObject, boolean log) {
        var tree = getContent(id);
        if (tree == null) {
            TrackEvent.withDebug("Preferences value not found")
                    .tag("id", id)
                    .tag("default", defaultObject)
                    .handle();
            return defaultObject;
        }

        if (PrefsChoiceValue.class.isAssignableFrom(type.getRawClass())) {
            List<T> all = (List<T>) getAll(type.getRawClass());
            if (all != null) {
                Class<PrefsChoiceValue> cast = (Class<PrefsChoiceValue>) type.getRawClass();
                var in = tree.asString();
                var found = all.stream()
                        .filter(t -> ((PrefsChoiceValue) t).getId().equalsIgnoreCase(in))
                        .findAny();
                if (found.isEmpty()) {
                    if (log) {
                        TrackEvent.withWarn("Invalid prefs value found")
                                .tag("key", id)
                                .tag("value", in)
                                .handle();
                    }
                    return defaultObject;
                }

                var supported = getSupported(cast);
                if (!supported.contains(found.get())) {
                    if (log) {
                        TrackEvent.withWarn("Unsupported prefs value found")
                                .tag("key", id)
                                .tag("value", in)
                                .handle();
                    }
                    return defaultObject;
                }

                if (log) {
                    TrackEvent.debug("Loading preferences value for key " + id + " from value " + found.get());
                }
                return found.get();
            }
        }

        try {
            if (log) {
                TrackEvent.debug("Loading preferences value for key " + id + " from value " + tree);
            }
            T value = JacksonMapper.getDefault().treeToValue(tree, type);
            if (value instanceof List<?> l) {
                var mod = l.stream().filter(v -> v != null).collect(Collectors.toCollection(ArrayList::new));
                return (T) mod;
            }
            return value;
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).expected().omit().handle();
            return defaultObject;
        }
    }
}
