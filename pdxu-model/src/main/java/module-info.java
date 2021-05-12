module com.crschnick.pdxu.model {
    requires java.se;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires com.crschnick.pdxu.io;

    exports com.crschnick.pdxu.model;
    exports com.crschnick.pdxu.model.ck3;
    exports com.crschnick.pdxu.model.ck2;
    exports com.crschnick.pdxu.model.eu4;
    exports com.crschnick.pdxu.model.hoi4;
    exports com.crschnick.pdxu.model.stellaris;
    exports com.crschnick.pdxu.model.vic2;
}