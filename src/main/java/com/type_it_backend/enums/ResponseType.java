package com.type_it_backend.enums;

public enum ResponseType {
    START_GAME("start_game"),
    JOIN_ROOM_SUCCEEDED("join_room_succeeded"),
    JOIN_ROOM_FAILED("join_room_failed"),
    REQUEST_HANDLING_ERROR("request_handling_error"),;

    private final String responseType;

    ResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }
}
