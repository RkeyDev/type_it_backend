package com.type_it_backend.enums;

public enum ResponseType {
    START_GAME("start_game"),

    JOIN_ROOM_SUCCEEDED("join_room_succeeded"),
    JOIN_ROOM_FAILED("join_room_failed"),

    CREATE_ROOM_SUCCEED("create_room_succeed"),
    CREATE_ROOM_FAILED("create_room_failed"),

    START_MATCHMAKING_SUCCEEDED("start_matchmaking_succeeded"),
    START_MATCHMAKING_FAILED("start_matchmaking_failed"),

    UPDATE_ROOM("update_room"),

    WORD_SUBMISSION_SUCCEEDED("word_submission_succeeded"),
    WORD_SUBMISSION_FAILED("word_submission_failed"),

    GAME_STARTED("game_started"),

    REQUEST_HANDLING_ERROR("request_handling_error"),;

    private final String responseType;

    ResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getResponseType() {
        return responseType;
    }
}
