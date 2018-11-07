package com.deepinspire.gmatclub.storage;

public class User {
    private boolean logged = false;

    private long id = -1;

    private int countFailedAuth = 0;

    User() {
        this.logged = false;
        this.id = -1;
        this.countFailedAuth = 0;
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

    public void setCountFailedAuth(int countFailedAuth) {
        this.countFailedAuth = countFailedAuth;
    }

    public void updateCountFailedAuth(int countFailedAuth) {
        this.countFailedAuth += countFailedAuth;
    }

    public int getCountFailedAuth() {
        return this.countFailedAuth;
    }

    public void clearCountFailedAuth() {
        this.countFailedAuth = 0;
    }
}