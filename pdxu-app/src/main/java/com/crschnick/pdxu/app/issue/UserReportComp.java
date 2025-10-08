package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.comp.base.*;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppLogs;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import atlantafx.base.controls.Spacer;

import java.nio.file.Path;

public class UserReportComp extends ModalOverlayContentComp {

    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty text = new SimpleStringProperty();
    private final ListProperty<Path> includedDiagnostics;
    private final ErrorEvent event;

    public UserReportComp(ErrorEvent event) {
        this.event = event;
        this.includedDiagnostics = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public static boolean show(ErrorEvent event) {
        var comp = new UserReportComp(event);
        var modal = ModalOverlay.of("errorHandler", comp);
        var sent = new SimpleBooleanProperty();
        modal.addButton(new ModalButton(
                "sendReport",
                () -> {
                    comp.send();
                    sent.set(true);
                },
                true,
                true));
        modal.showAndWait();
        return sent.get();
    }

    @Override
    protected Region createSimple() {
        var emailHeader = new Label(AppI18n.get("provideEmail"));
        emailHeader.setWrapText(true);
        var email = new TextField();
        this.email.bind(email.textProperty());
        VBox.setVgrow(email, Priority.ALWAYS);

        var infoHeader = new Label(AppI18n.get("additionalErrorInfo"));
        var tf = new TextArea();
        text.bind(tf.textProperty());
        VBox.setVgrow(tf, Priority.ALWAYS);

        var attachmentsHeader = new Label(AppI18n.get("additionalErrorAttachments"));
        var attachments = new ListSelectorComp<>(
                        FXCollections.observableList(event.getAttachments()),
                        file -> {
                            if (file.equals(AppLogs.get().getSessionLogsDirectory())) {
                                return AppI18n.get("logFilesAttachment");
                            }

                            return file.getFileName().toString();
                        },
                        includedDiagnostics,
                        file -> false,
                        () -> false)
                .styleClass("attachment-list")
                .createRegion();

        var reportSection = new VBox(
                infoHeader,
                tf,
                new Spacer(8, Orientation.VERTICAL),
                attachmentsHeader,
                new Spacer(3, Orientation.VERTICAL),
                attachments);
        reportSection.setSpacing(5);
        reportSection.getStyleClass().add("report");
        reportSection.getChildren().addAll(new Spacer(8, Orientation.VERTICAL), emailHeader, email);
        reportSection.setPrefWidth(600);
        reportSection.setPrefHeight(550);
        return reportSection;
    }

    private void send() {
        event.clearAttachments();
        event.setShouldSendDiagnostics(true);
        includedDiagnostics.forEach(event::addAttachment);
        event.attachUserReport(email.get(), text.get());
        SentryErrorHandler.getInstance().handle(event);
    }
}
