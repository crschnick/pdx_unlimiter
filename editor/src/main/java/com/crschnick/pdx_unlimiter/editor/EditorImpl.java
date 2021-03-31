package com.crschnick.pdx_unlimiter.editor;

import com.crschnick.pdx_unlimiter.app.savegame.EditorProvider;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameContext;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.editor.core.Editor;
import com.crschnick.pdx_unlimiter.editor.target.EditTarget;
import com.crschnick.pdx_unlimiter.editor.target.StorageEditTarget;

import java.nio.file.Path;

public class EditorImpl implements EditorProvider {

    @Override
    public void open(Path file) {
        EditTarget.create(file).ifPresent(Editor::createNewEditor);
    }

    @Override
    public void open(SavegameEntry<?, ?> e) {
        SavegameContext.withSavegame(e, ctx -> {
            var in = ctx.getStorage().getSavegameFile(e);
            var target = EditTarget.create(in);
            target.ifPresent(t -> {
                var storageTarget = new StorageEditTarget(ctx.getStorage(), e, t);
                Editor.createNewEditor(storageTarget);
            });
        });
    }
}
