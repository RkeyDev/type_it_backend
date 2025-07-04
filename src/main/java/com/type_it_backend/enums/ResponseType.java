package com.type_it_backend.enums;

public enum ResponseType {
    START_GAME("start_game"),
    JOIN_ROOM("join_room"),
    JOIN_ROOM_FAILED("join_room_failed"),;

    private final String responseType;

    ResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }
}
