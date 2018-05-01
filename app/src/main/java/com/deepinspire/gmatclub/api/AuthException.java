package com.deepinspire.gmatclub.api;

/**
 * Created by dmytro mytsko on 17.04.18.
 */
public class AuthException extends Exception {
    final String type;

    public AuthException(final Exception ex) {
        super(ex);
        this.type = null;
    }

    public AuthException(final Exception ex, String type) {
        super(ex);
        this.type = type;
    }

    /**
     *
     * @return the type which use for identify exception
     */
    public String getType() {
        return type;
    }
}
