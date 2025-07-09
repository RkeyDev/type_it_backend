package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.RequestType;
import com.type_it_backend.enums.ResponseType;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseBuilder;

public class RequestHandler {

    public static void handle(Request request) {
        RequestType requestType = request.getRequestType();
        HashMap<String, Object> requestData = request.getData();

        if (requestType == null) {
            throw new IllegalArgumentException("Request type cannot be null");
        }

        switch (requestType) {
            case JOIN_ROOM -> joinRoomRequest(request, requestData);
            case CREATE_ROOM -> createRoomRequest(request, requestData);
            case START_GAME -> startGameRequest(request, requestData);
            case START_MATCHMAKING -> startMatchmakingRequest(request, requestData);
            case WORD_SUBMISSION -> wordSubmissionRequest(request, requestData);
            default -> throw new UnsupportedOperationException("Request type not supported: " + requestType);
        }
    }

    private static void joinRoomRequest(Request request, HashMap<String, Object> data) {
        String roomCode = String.valueOf(data.get("roomCode"));

        if (roomCode == null || roomCode.isEmpty()) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": Room code cannot be null or empty");
            throw new IllegalArgumentException("Room code cannot be null or empty");
        }

        @SuppressWarnings("unchecked")
        HashMap<String, Object> playerData = (HashMap<String, Object>) data.get("player");

        Player player;
        try {
            // Build the player object from the data
            player = new Player(
                (String) playerData.get("name"),
                (String) playerData.get("skinPath"),
                false,
                request.getSenderConn()
            );
        } catch (Exception e) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            throw new IllegalArgumentException("Invalid player data: " + e.getMessage());
        }

        // Get the room by code
        Room room = RoomManager.getRoomByCode(roomCode);
        if (room == null) {
            player.getConn().send("{\"type\": \"" + ResponseType.JOIN_ROOM_FAILED.getResponseType() + "\"}");
            throw new IllegalArgumentException("Room with code " + roomCode + " does not exist");
        }

        player.setRoom(room);

        // Add player to the room's player map
        Player existing = room.getPlayers().putIfAbsent(player.getPlayerId(), player);
        if (existing != null) {
            player.getConn().send(ResponseType.JOIN_ROOM_FAILED.getResponseType());
            throw new IllegalArgumentException("Player is already in the room");
        }

        String response = updateRoomResponse(room);
        room.broadcastResponse(response);
    }

    private static void createRoomRequest(Request request, HashMap<String, Object> data) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> playerData = (HashMap<String, Object>) data.get("player");

        Player player;
        try {
            // Build the player object from the data
            player = new Player(
                (String) playerData.get("name"),
                (String) playerData.get("skinPath"),
                true,
                request.getSenderConn()
            );
        } catch (Exception e) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            throw new IllegalArgumentException("Invalid player data: " + e.getMessage());
        }

        // Create a new room with the generated code and the player as host
        Room room = RoomManager.createRoom(player);
        if (room == null) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": Failed to create room");
            throw new IllegalStateException("Failed to create room");
        }

        String response = updateRoomResponse(room);
        player.getConn().send(response);
    }

    private static void startGameRequest(Request request, HashMap<String, Object> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private static void wordSubmissionRequest(Request request, HashMap<String, Object> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private static void startMatchmakingRequest(Request request, HashMap<String, Object> data) {

        @SuppressWarnings("unchecked")
        HashMap<String, Object> playerData = (HashMap<String, Object>) data.get("player");

        Player player;
        try {
            // Build the player object from the data
            player = new Player(
                (String) playerData.get("name"),
                (String) playerData.get("skinPath"),
                true,
                request.getSenderConn()
            );
        } catch (Exception e) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            throw new IllegalArgumentException("Invalid player data: " + e.getMessage());
        }

        if(RoomManager.addPlayerToRandomRoom(player)){
            player.getConn().send(updateRoomResponse(player.getRoom()));
        }
        else {
            request.getSenderConn().send(ResponseType.START_MATCHMAKING_FAILED.getResponseType());
            throw new IllegalStateException("No available rooms for matchmaking");
        }
    }

    public static String updateRoomResponse(Room room) {
        HashMap<String, Object> responseMap = new HashMap<>();
        HashMap<String, Object> dataMap = new HashMap<>();

        String roomCode = room.getRoomCode();

        responseMap.put("type", ResponseType.UPDATE_ROOM.getResponseType());
        dataMap.put("roomCode", roomCode);
        dataMap.put("players", room.getPlayersAsString());
        responseMap.put("data", dataMap);

        return ResponseBuilder.buildResponse(responseMap);
    }
}
