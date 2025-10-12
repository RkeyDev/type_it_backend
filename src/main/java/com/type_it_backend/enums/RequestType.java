package com.type_it_backend.enums;

public enum RequestType {
    JOIN_ROOM("join_room"),
    START_MATCHMAKING("start_matchmaking"),
    CREATE_ROOM("create_room"),
    START_GAME("start_game"),
    WORD_SUBMISSION("word_submission"),
    INITIALIZE_GAME("initialize_game"),
    START_NEW_ROUND("start_new_round"),
    GET_ROOM_DATA("get_room_data"),
    TOGGLE_MATCHMAKING("toggle_matchmaking");

    private final String requestType;

    RequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getrequestType() {
        return requestType;
    }
}
