package com.julian.appturnos.vistas.login;

public class LoginUiEvent {
    public enum Type { NAVIGATE, SHOW_MESSAGE }
    public final Type type;
    public final String message;
    public final int destinationId;

    public LoginUiEvent(Type type, String message, int destinationId) {
        this.type = type;
        this.message = message;
        this.destinationId = destinationId;
    }
}

