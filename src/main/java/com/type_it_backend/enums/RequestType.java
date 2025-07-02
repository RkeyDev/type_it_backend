package com.type_it_backend.enums;

public enum RequestType {
    JOIN_ROOM("join_room"),
    START_MATCHMAKING("start_matchmaking"),
    CREATE_ROOM("create_room"),
    START_GAME("start_game"),
    WORD_SUBMISSION("word_submission");

    private final String requestType;

    RequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getrequestType() {
        return requestType;
    }
}
