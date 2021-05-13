package com.crschnick.pdxu.app.savegame;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;

public final class SavegameNotes {

    private final StringProperty text;
    private final BooleanProperty remindMe;

    private SavegameNotes(String text, boolean remindMe) {
        this.text = new SimpleStringProperty(text);
        this.remindMe = new SimpleBooleanProperty(remindMe);
    }

    public static JsonNode toNode(SavegameNotes notes) {
        if (notes.textProperty().get().length() == 0 && !notes.remindMeProperty().get()) {
            return null;
        }

        var o = JsonNodeFactory.instance.objectNode();
        o.put("text", notes.text.getValue());
        o.put("remindMe", notes.remindMe.get());
        return o;
    }

    public static SavegameNotes empty() {
        return new SavegameNotes("", false);
    }

    public static SavegameNotes fromNode(JsonNode node) {
        if (node == null) {
            return empty();
        }

        var text = Optional.ofNullable(node.get("text")).map(JsonNode::asText).orElse("");
        var b = Optional.ofNullable(node.get("remindMe")).map(JsonNode::asBoolean).orElse(false);
        return new SavegameNotes(text, b);
    }

    public StringProperty textProperty() {
        return text;
    }

    public BooleanProperty remindMeProperty() {
        return remindMe;
    }
}
