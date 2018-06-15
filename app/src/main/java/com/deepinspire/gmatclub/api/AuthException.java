package com.deepinspire.gmatclub.api;

/**
 * Created by dmytro mytsko on 17.04.18.
 */
public class AuthException extends Exception {
    final String type;
    final String message;
    String action = "ERROR";// UNKNOWN_HOST || ERROR

    public AuthException(final Exception ex) {
        super(ex);
        this.type = null;
        this.message = ex.getMessage();
    }

    public AuthException(final Exception ex, String type) {
        super(ex);
        this.type = type;
        this.message = ex.getMessage();
    }

    public AuthException(final Exception ex, String type, String message) {
        super(ex);
        this.type = type;
        this.message = message;
    }

    /**
     *
     * @return the type which use for identify exception
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return the message which use for show exception
     */
    public String getMessage() {
        return message;
    }

    public void setAction(String action) {
        if(action == null || action.isEmpty()) {
            this.action = "ERROR";
        } else {
            this.action  = action;
        }
    }

    public String getAction() {
        return this.action;
    }
}