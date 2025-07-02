package com.type_it_backend.enums;

public enum ResponseType {
    START_GAME("start_game");

    private final String responseType;

    ResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }
}
