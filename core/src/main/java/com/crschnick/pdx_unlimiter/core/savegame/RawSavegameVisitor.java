package com.crschnick.pdx_unlimiter.core.savegame;

import java.nio.file.Path;

public interface RawSavegameVisitor {

    static void vist(Path file, RawSavegameVisitor visitor) {
        if (file.getFileName().toString().endsWith("eu4")) {
            visitor.visitEu4(file);
        } else if (file.getFileName().toString().endsWith("hoi4")) {
            visitor.visitHoi4(file);
        } else if (file.getFileName().toString().endsWith("sav")) {
            visitor.visitStellaris(file);
        } else if (file.getFileName().toString().endsWith("ck3")) {
            visitor.visitCk3(file);
        } else {
            visitor.visitOther(file);
        }
    }

    void visitEu4(Path file);

    void visitHoi4(Path file);

    void visitStellaris(Path file);

    void visitCk3(Path file);

    void visitOther(Path file);
}
