package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.*;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    @Jacksonized
    @Builder
    public static class Resource {
        double stored;
        double income;
        double expense;

        public static Resource parseFromCountryNode(Node node, String name) {
            var budget = node.getNodeForKeys("budget", "current_month");
            final double[] income = {0.0};
            final double[] expense = {0.0};
            budget.getNodeForKey("income").forEach((k, v) -> {
                v.forEach((resourceName, amount) -> {
                    if (name.equals(resourceName)) {
                        income[0] += amount.getDouble();
                    }
                });
            });
            budget.getNodeForKey("expenses").forEach((k, v) -> {
                v.forEach((resourceName, amount) -> {
                    if (name.equals(resourceName)) {
                        expense[0] += amount.getDouble();
                    }
                });
            });
            var stored = node.getNodeForKeys("modules", "standard_economy_module", "resources", name).getDouble();
            return Resource.builder().stored(stored).income(income[0]).expense(expense[0]).build();
        }
    }

    StellarisEnergyComp energy;
    StellarisMineralsComp minerals;
    StellarisFoodComp food;
    StellarisAlloysComp alloys;
    StellarisAlliesComp allies;
    StellarisWarMultiComp wars;
    DlcComp dlcs;
    VersionComp version;

    public StellarisSavegameInfo() {
    }

    public StellarisSavegameInfo(SavegameContent content) throws Exception {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "stellaris";
    }

    @Override
    protected Class<? extends SavegameData<StellarisTag>> getDataClass() {
        return StellarisSavegameData.class;
    }
}
