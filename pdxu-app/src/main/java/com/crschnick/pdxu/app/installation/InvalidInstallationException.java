package com.crschnick.pdxu.app.installation;


import com.crschnick.pdxu.app.core.AppI18n;

public final class InvalidInstallationException extends Exception {

    private final String msgId;
    private final Object[] variables;

    public InvalidInstallationException(String msgId, Object... vars) {
        super(AppI18n.get(msgId, vars));
        this.msgId = msgId;
        this.variables = vars;
    }

    public InvalidInstallationException(Throwable cause) {
        super(cause);
        this.msgId = "errorOccurred";
        this.variables = new String[]{cause.getMessage()};
    }

    public String getMessageId() {
        return msgId;
    }

    public String getLocalisedMessage() {
        return AppI18n.get(msgId, variables);
    }
}
