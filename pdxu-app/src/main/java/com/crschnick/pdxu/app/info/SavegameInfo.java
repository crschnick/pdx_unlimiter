package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.io.node.ArrayNode;

import java.lang.reflect.InvocationTargetException;

public abstract class SavegameInfo<T extends SavegameData> {

    protected T data;

    protected SavegameInfo(ArrayNode node) throws Exception {
        try {
            this.data = (T) getDataClass().getDeclaredConstructors()[0].newInstance(node);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            ErrorHandler.handleTerminalException(e);
        }

        for (var field : getClass().getFields()) {
            if (!SavegameInfoComp.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                field.set(this, field.getType().getDeclaredConstructors()[0].newInstance());
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                ErrorHandler.handleTerminalException(e);
            }

            SavegameInfoComp c = (SavegameInfoComp) field.get(this);
            c.init(node, this.data);
        }
    }

    protected abstract Class<T> getDataClass();
}
