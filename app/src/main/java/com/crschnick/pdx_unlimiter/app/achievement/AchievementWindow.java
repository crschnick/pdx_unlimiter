package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AchievementWindow {

    public static void showAchievementDialog(Achievement a, Eu4IntermediateSavegame sg) {
        ButtonType foo = new ButtonType("Validate", ButtonBar.ButtonData.OK_DONE);
        ButtonType bar = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = DialogHelper.createAlertForNode(AchievementWindow.createAchievementInfoNode(a, sg));

        alert.getButtonTypes().add(foo);
        alert.getButtonTypes().add(bar);
        Button btOk = (Button) alert.getDialogPane().lookupButton(foo);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                Event::consume
        );

        Platform.runLater(alert::showAndWait);
    }

    public static Node createAchievementInfoNode(Achievement a) {
        VBox box = new VBox();
        box.setFillWidth(true);

        box.getChildren().add(new Region());
        Label name = new Label(a.getName());
        name.setStyle("-fx-font-size: 18px;");
        box.getChildren().add(name);

        box.getChildren().add(new Region());
        Label desc = new Label(a.getDescription());
        desc.setStyle("-fx-font-size: 16px;");
        box.getChildren().add(desc);

        return box;
    }

    public static Node createConditionNode(String type, Achievement.ConditionStatus status) {
        VBox box = new VBox();
        box.setFillWidth(true);

        box.getChildren().add(new Region());
        Label name = new Label(type + ": ");
        name.setStyle("-fx-font-size: 18px;");
        box.getChildren().add(name);

        for (var e : status.getConditions().entrySet()) {
            Label text = new Label(e.getKey().getDescription());
            Label b = new Label(e.getValue() ? "\u2713" : "\u274C");
            Region r = new Region();
            HBox hb = new HBox(text, r, b);
            hb.setStyle("-fx-font-size: 16px;");
            HBox.setHgrow(r, Priority.ALWAYS);
            box.getChildren().add(hb);
        }
        return box;
    }

    public static Node createScoreNode(Achievement a, Achievement.ScoreStatus status) {
        VBox box = new VBox();
        box.setFillWidth(true);

        box.getChildren().add(new Region());
        Label name = new Label("Score calculation: ");
        name.setStyle("-fx-font-size: 18px;");
        box.getChildren().add(name);

        for (var e : status.getValues().entrySet()) {
            Label text = new Label(e.getKey());
            Label b = new Label(String.valueOf(e.getValue()));
            Region r = new Region();
            HBox hb = new HBox(text, r, b);
            hb.setStyle("-fx-font-size: 16px;");
            HBox.setHgrow(r, Priority.ALWAYS);
            box.getChildren().add(hb);
        }

        Label scoreVal = new Label(a.getReadableScore() + " = " + status.getScore());
        scoreVal.setStyle("-fx-font-size: 18px;");
        box.getChildren().add(scoreVal);

        return box;
    }

    public static Node createAchievementInfoNode(Achievement a, Eu4IntermediateSavegame s) {
        VBox box = new VBox();
        box.setSpacing(12);
        box.setFillWidth(true);
        box.getChildren().add(createAchievementInfoNode(a));
        box.getChildren().add(createConditionNode("Elgibility conditions", a.checkEligible(s)));
        box.getChildren().add(createConditionNode("Achievement conditions", a.checkAchieved(s)));

        box.getChildren().add(createScoreNode(a, a.score(s)));

        return box;
    }
}
