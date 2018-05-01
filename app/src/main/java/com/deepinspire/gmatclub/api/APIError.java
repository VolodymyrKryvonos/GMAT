package com.deepinspire.gmatclub.api;

public class APIError {

    private int statusCode;
    private int code;
    private String message;

    public APIError() {
    }

    public int status() {
        return statusCode;
    }

    public String message() {
        return message;
    }

    public int code() {
        return code;
    }
}