package com.type_it_backend.utils;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.ResponseType;

public class ResponseFactory {

    public static String updateRoomResponse(Room room) {
        HashMap<String, Object> responseMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();

        responseMap.put("type", ResponseType.UPDATE_ROOM.getResponseType());
        dataMap.put("roomCode", room.getRoomCode());
        dataMap.put("players", room.getPlayersAsString());
        responseMap.put("data", dataMap);

        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String playerGuessedCorrectlyResponse(Player player, String word) {
        HashMap<String, Object> responseMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();

        responseMap.put("type", ResponseType.PLAYER_GUESSED_CORRECTLY.getResponseType());
        dataMap.put("playerId", player.getPlayerId());
        dataMap.put("playerName", player.getPlayerName());
        dataMap.put("currentTotalCharacters", player.getGussedCharacters());
        responseMap.put("data", dataMap);

        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String playerGuessedIncorrectlyResponse() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", ResponseType.PLAYER_GUESSED_INCORRECTLY.getResponseType());
        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String startGameResponse() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", ResponseType.GAME_STARTED.getResponseType());
        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String startNewRoundResponse(String question) {
        HashMap<String, Object> responseMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();

        responseMap.put("type", ResponseType.START_NEW_ROUND.getResponseType());
        dataMap.put("question", question);
        responseMap.put("data", dataMap);

        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String errorResponse(String message) {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", ResponseType.REQUEST_HANDLING_ERROR.getResponseType());
        responseMap.put("message", message);
        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String joinRoomFailedResponse() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", ResponseType.JOIN_ROOM_FAILED.getResponseType());
        return ResponseBuilder.buildResponse(responseMap);
    }

    public static String matchmakingFailedResponse() {
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("type", ResponseType.START_MATCHMAKING_FAILED.getResponseType());
        return ResponseBuilder.buildResponse(responseMap);
    }
}
