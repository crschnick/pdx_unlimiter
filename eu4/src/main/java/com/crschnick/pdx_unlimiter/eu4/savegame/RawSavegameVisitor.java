package com.crschnick.pdx_unlimiter.eu4.savegame;

import java.nio.file.Path;

public interface RawSavegameVisitor {

    static void vist(Path file, RawSavegameVisitor visitor) {
        if (file.getFileName().toString().endsWith("eu4")) {
            visitor.visitEu4(file);
        } else if (file.getFileName().toString().endsWith("hoi4")) {
            visitor.visitHoi4(file);
        } else {
            throw new IllegalArgumentException("Invalid file type: " + file.getFileName());
        }
    }

    void visitEu4(Path file);

    void visitHoi4(Path file);
}
