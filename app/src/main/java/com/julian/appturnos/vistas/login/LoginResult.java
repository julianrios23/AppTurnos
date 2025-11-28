package com.julian.appturnos.vistas.login;

public class LoginResult {
    public enum Status { SUCCESS, ERROR }
    public final Status status;
    public final String message;
    public final String token;

    public LoginResult(Status status, String message, String token) {
        this.status = status;
        this.message = message;
        this.token = token;
    }
}

