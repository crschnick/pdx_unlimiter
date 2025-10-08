package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.SimpleComp;

import lombok.Getter;

@Getter
public abstract class ModalOverlayContentComp extends SimpleComp {

    protected ModalOverlay modalOverlay;

    void setModalOverlay(ModalOverlay modalOverlay) {
        this.modalOverlay = modalOverlay;
    }
}
