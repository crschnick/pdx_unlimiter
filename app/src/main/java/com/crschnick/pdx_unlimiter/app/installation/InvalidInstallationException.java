package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.PdxuI18n;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;

public final class InvalidInstallationException extends Exception {

    private final String msgId;

    public InvalidInstallationException(String msgId, String... vars) {
        super(PdxuI18n.get(LocalisationHelper.Language.ENGLISH).getLocalised(msgId, vars));
        this.msgId = msgId;
    }

    public InvalidInstallationException(Throwable cause) {
        super(cause);
        this.msgId = "ERROR_OCCURED";
    }

    public String getMessageId() {
        return msgId;
    }
}
