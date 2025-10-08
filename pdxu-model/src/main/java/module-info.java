open module com.crschnick.pdxu.model {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.crschnick.pdxu.io;

    requires static lombok;

    exports com.crschnick.pdxu.model;
    exports com.crschnick.pdxu.model.ck3;
    exports com.crschnick.pdxu.model.ck2;
    exports com.crschnick.pdxu.model.eu4;
    exports com.crschnick.pdxu.model.hoi4;
    exports com.crschnick.pdxu.model.stellaris;
    exports com.crschnick.pdxu.model.vic2;
    exports com.crschnick.pdxu.model.vic3;
    exports com.crschnick.pdxu.model.coa;
}
