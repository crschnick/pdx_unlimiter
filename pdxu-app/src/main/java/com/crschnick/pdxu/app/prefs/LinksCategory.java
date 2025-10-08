package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.comp.base.TileButtonComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import com.crschnick.pdxu.app.util.Hyperlinks;

public class LinksCategory extends AppPrefsCategory {

    private Comp<?> createLinks() {
        return new OptionsBuilder()
                .addTitle("links")
                .addComp(Comp.vspacer(19))
                .addComp(
                        new TileButtonComp("discord", "discordDescription", "bi-discord", e -> {
                            Hyperlinks.open(Hyperlinks.DISCORD);
                            e.consume();
                        })
                                .maxWidth(2000),
                        null)
                .addComp(
                        new TileButtonComp(
                                        "documentation", "documentationDescription", "mdi2b-book-open-variant", e -> {
                                            Hyperlinks.open(Hyperlinks.DOCS);
                                            e.consume();
                                        })
                                .maxWidth(2000),
                        null)
                .addComp(
                        new TileButtonComp("thirdParty", "thirdPartyDescription", "mdi2o-open-source-initiative", e -> {
                                    var comp = new ThirdPartyDependencyListComp()
                                            .prefWidth(650)
                                            .styleClass("open-source-notices");
                                    var modal = ModalOverlay.of("openSourceNotices", comp);
                                    modal.show();
                                })
                                .maxWidth(2000))
                .addComp(Comp.vspacer(40))
                .buildComp();
    }

    @Override
    public String getId() {
        return "links";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2l-link-box-outline");
    }

    @Override
    public Comp<?> create() {
        return createLinks().styleClass("information").styleClass("about-tab").apply(struc -> struc.get()
                .setPrefWidth(600));
    }
}
