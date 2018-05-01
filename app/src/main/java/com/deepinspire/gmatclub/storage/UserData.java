package com.deepinspire.gmatclub.storage;

public class UserData {
    String username;
    String password;

    String login = "Login";

    String redirect = "index.php";

    public UserData(String username, String password) {
        this.username = username;
        this.password = password;
        this.login = "Login";
        this.redirect = "index.php";
    }
}