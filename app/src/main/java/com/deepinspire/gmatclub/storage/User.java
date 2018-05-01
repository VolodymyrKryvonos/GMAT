package com.deepinspire.gmatclub.storage;

/**
 * Created by dmytro mytsko on 26.03.18.
 */
public class User {
    boolean logged = false;

    long id = -1;

    public User() {
        this.logged = false;
        this.id = -1;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public boolean getLogged() {
        return this.logged;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}