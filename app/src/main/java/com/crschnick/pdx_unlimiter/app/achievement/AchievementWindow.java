package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.Eu4ImageLoader;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AchievementWindow {

    private static Tooltip tooltip(String text) {
        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.millis(100));
        t.setStyle("-fx-font-size: 14px;");
        return t;
    }

    private static void sortAchievementList(VBox list, List<Achievement> as, Eu4Campaign.Entry entry) {
        List<Node> newOrder = as.stream()
                .map(a -> {
                    Node n = AchievementWindow.createAchievementInfoNode(a);
                    n.setOnMouseClicked(e -> {
                        showAchievementDialog(a, entry);
                    });
                    return n;
                })
                .collect(Collectors.toList());
        list.getChildren().setAll(newOrder);
    }

    public static void showAchievementList(Eu4Campaign.Entry entry) {
        Alert alert = DialogHelper.createAlert();
        alert.setTitle("Achievement List");

        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        Button refresh = new Button("Refresh");
        Region spacer = new Region();
        CheckBox onlyOfficial = new CheckBox("Official only");
        CheckBox onlyEligible = new CheckBox("Eligible only");
        HBox top = new HBox(refresh, spacer, onlyOfficial, onlyEligible);
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        refresh.setOnMouseClicked(e -> {
            AchievementManager.getInstance().refresh();
            sortAchievementList(grid, AchievementManager.getInstance().getSuitableAchievements(
                    entry.getInfo().get().getSavegame(), onlyOfficial.isSelected(), onlyEligible.isSelected()), entry);
        });

        top.setSpacing(10);
        top.setPadding(new Insets(5, 5, 5, 5));

        javafx.beans.value.ChangeListener<? super Boolean> l = (c,o,n) -> {
            sortAchievementList(grid, AchievementManager.getInstance().getSuitableAchievements(
                    entry.getInfo().get().getSavegame(), onlyOfficial.isSelected(), onlyEligible.isSelected()), entry);
        };
        onlyEligible.selectedProperty().addListener(l);
        onlyOfficial.selectedProperty().addListener(l);
        l.changed(null, null, null);

        ScrollPane pane = new ScrollPane(grid);
        pane.setPadding(Insets.EMPTY);
        pane.setMinViewportWidth(350);
        pane.setMinViewportHeight(500);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pane.setFitToWidth(true);

        VBox content = new VBox(top, pane);
        content.setPadding(new Insets(0, 0, 0, 0));
        content.setFillWidth(true);
        alert.getDialogPane().setContent(content);
        alert.getButtonTypes().add(ButtonType.CLOSE);
        Platform.runLater(alert::showAndWait);
    }

    public static void showAchievementDialog(Achievement a, Eu4Campaign.Entry entry) {
        if (entry.getInfo().isEmpty()) {
            return;
        }

        ButtonType foo = new ButtonType("Validate", ButtonBar.ButtonData.OK_DONE);
        ButtonType bar = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        AchievementMatcher matcher = a.match(entry.getInfo().get().getSavegame());
        Alert alert = DialogHelper.createAlert();
        alert.setTitle("Achievement Information");
        var node = AchievementWindow.createAchievementInfoNode(a, matcher, false);
        alert.getDialogPane().setContent(node);
        alert.getButtonTypes().add(foo);
        alert.getButtonTypes().add(bar);
        Button val = (Button) alert.getDialogPane().lookupButton(foo);
        val.addEventFilter(
                ActionEvent.ACTION,
                e -> {
                    try {
                        AchievementMatcher m = AchievementManager.getInstance().validateSavegame(a, entry);
                        alert.getDialogPane().setContent(AchievementWindow.createAchievementInfoNode(a, m, true));
                        val.setDisable(true);
                        alert.hide();
                        alert.showAndWait();
                        e.consume();
                    } catch (IOException ioException) {
                        ErrorHandler.handleException(ioException);
                    }
                }
        );

        Platform.runLater(alert::showAndWait);
    }

    public static Node createAchievementInfoNode(Achievement a) {
        VBox box = new VBox();
        box.setFillWidth(true);
        box.setSpacing(5);
        box.setPadding(new Insets(0, 5, 5, 5));

        box.getChildren().add(new Region());
        Label name = new Label(a.getName());
        name.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        box.getChildren().add(name);

        box.getChildren().add(new Region());
        Label desc = new Label(a.getDescription());
        desc.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        box.getChildren().add(desc);

        HBox b = new HBox();
        b.setAlignment(Pos.CENTER_LEFT);
        b.getChildren().add(getImageForIcon(a.getIcon(), 60));
        b.getChildren().add(box);
        b.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;");
        b.setPadding(new Insets(5, 5, 5, 5));
        return b;
    }

    private static Node getImageForIcon(Optional<Path> p, int size) {
        if (p.isPresent()) {
            ImageView img = new ImageView(Eu4ImageLoader.loadImage(p.get()));
            img.setFitWidth(size);
            img.setFitHeight(size);
            return img;
        } else {
            Label l = new Label("?");
            l.setStyle("-fx-text-fill: white; -fx-font-size: 30px;");
            l.minWidthProperty().setValue(size);
            l.prefHeightProperty().setValue(size);
            l.alignmentProperty().set(Pos.CENTER);
            return l;
        }
    }

    public static Node createConditionNode(String type, AchievementMatcher.ConditionStatus status) {
        VBox box = new VBox();
        box.setFillWidth(true);
        box.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;");
        box.setSpacing(5);
        box.setPadding(new Insets(0, 8, 8, 8));

        box.getChildren().add(new Region());
        Label name = new Label(type);
        name.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        box.getChildren().add(name);

        for (var e : status.getConditions().entrySet()) {
            Label text = new Label(e.getKey().getDescription());
            Label b = new Label(e.getValue() ? "\u2713" : "\u274C");
            Region r = new Region();
            HBox hb = new HBox(text, r, b);
            text.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
            b.setStyle(e.getValue() ?
                    "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: #7b7; -fx-border-color: #666666;":
                    "-fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: #b77; -fx-border-color: #666666;");
            HBox.setHgrow(r, Priority.ALWAYS);
            box.getChildren().add(hb);
            //Tooltip.install(hb, tooltip("Condition for node " + e.getKey().getNode() + ": " + e.getKey().getFilter().toString()));
        }
        return box;
    }

    public static Node createScoreNode(Achievement a, AchievementMatcher.ScoreStatus status) {
        VBox box = new VBox();
        box.setFillWidth(true);
        box.setSpacing(5);
        box.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;");

        box.setPadding(new Insets(0, 8, 8, 8));

        box.getChildren().add(new Region());
        Label name = new Label("Score calculation: ");
        name.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        box.getChildren().add(name);

        for (var e : status.getValues().entrySet()) {
            Label text = new Label(e.getKey());
            Label b = new Label(new DecimalFormat("#0.00").format(e.getValue()));
            Region r = new Region();
            HBox hb = new HBox(text, r, b);
            text.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
            b.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
            HBox.setHgrow(r, Priority.ALWAYS);
            box.getChildren().add(hb);
        }

        Region line = new Region();
        line.setStyle("-fx-border-color: white;");
        box.getChildren().add(line);

        Label scoreCalc = new Label(a.getReadableScore());
        scoreCalc.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Label scoreVal = new Label("= " + new DecimalFormat("#0.00").format(status.getScore()));
        scoreVal.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        Region r = new Region();
        HBox hb = new HBox(scoreCalc, r, scoreVal);
        HBox.setHgrow(r, Priority.ALWAYS);
        box.getChildren().add(hb);

        return box;
    }

    private static Node createValidatedNode() {
        Label text = new Label("Validated");
        Label b = new Label("\u2713");
        Region r = new Region();
        HBox hb = new HBox(text, r, b);
        text.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        b.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        HBox.setHgrow(r, Priority.ALWAYS);

        hb.setStyle("-fx-border-color: #66aa66; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #77bb77;");
        hb.setPadding(new Insets(5, 8, 3, 8));

        return hb;
    }

    public static VBox createAchievementInfoNode(Achievement a, AchievementMatcher m, boolean validated) {
        VBox box = new VBox();
        box.setPadding(Insets.EMPTY);
        box.setSpacing(4);
        box.setFillWidth(true);
        box.setMinWidth(350);
        box.getChildren().add(createAchievementInfoNode(a));
        if (m.getValidType().isPresent()) {
            box.getChildren().add(createConditionNode(
                    "Type: " + m.getValidType().get().getName(),
                    m.getTypeStatus().get(m.getValidType().get())));
        } else {
            m.getTypeStatus().forEach(
                    (t,s) -> box.getChildren().add(createConditionNode("Type: " + t.getName(), s)));
        }
        box.getChildren().add(createConditionNode("Eligibility conditions:", m.getEligibleStatus()));
        box.getChildren().add(createConditionNode("Achievement conditions:", m.getAchievementStatus()));

        box.getChildren().add(createScoreNode(a, m.getScoreStatus()));

        if (validated) {
            box.getChildren().add(createValidatedNode());
        }
        return box;
    }
}