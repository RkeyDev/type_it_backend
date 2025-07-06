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

    /**
     * Handles incoming requests based on their type.
     * @param request The incoming request to handle.
     */

    public static void handle(Request request) {
        RequestType requestType = request.getRequestType();
        HashMap<String, Object> requestData = request.getData();

        // Validate request type
        if (requestType == null) {
            throw new IllegalArgumentException("Request type cannot be null");
        }

        switch (requestType) {
            case JOIN_ROOM:
                joinRoomRequest(request, requestData);
                break;

            case CREATE_ROOM:
                createRoomRequest(request, requestData);
                break;
                
            case START_GAME:
                startGameRequest(request, requestData);
                break;

            case START_MATCHMAKING:
                startMatchmakingRequest(request, requestData);
                break;
                
            case WORD_SUBMISSION:
                wordSubmissionRequest(request, requestData);
                break;

            default:
                throw new UnsupportedOperationException("Request type not supported: " + requestType);
        }
    }

    private static void joinRoomRequest(Request request,HashMap<String,Object> data) {
        String roomCode = String.valueOf(data.get("roomCode"));

        if (roomCode == null || roomCode.isEmpty()) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": Room code cannot be null or empty");
            throw new IllegalArgumentException("Room code cannot be null or empty");
        }

        @SuppressWarnings("unchecked")
        HashMap<String, Object> playerData = (HashMap<String, Object>) data.get("player");
        
        Player player;
        try{
            // Build the player object from the data
            player = new Player(
                (String) playerData.get("name"),
                (String) playerData.get("skinPath"),
                (boolean) playerData.get("isHost"),
                request.getSenderConn()
            );
        }
        catch (Exception e) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            throw new IllegalArgumentException("Invalid player data: " + e.getMessage());
        }

        // Get the room by code
        Room room = RoomManager.getRoomByCode(roomCode); 

        if (room == null) {
            // Notify the player that they couldn't join the room
        player.getConn().send("{\"type\": \"" + ResponseType.JOIN_ROOM_FAILED.getResponseType() + "\"}");
            throw new IllegalArgumentException("Room with code " + roomCode + " does not exist");
            
        }
        
        // Add player to the room's player map. Returns null if player sucsessfully added, else return existing player
        Player playerExisting = room.getPlayers().putIfAbsent(player.getPlayerId(), player); 

        if (playerExisting != null) {
            // Notify the player that they couldn't join the room
            player.getConn().send(ResponseType.JOIN_ROOM_FAILED.getResponseType()); 
            throw new IllegalArgumentException("Player is already in the room");
        }




        HashMap<String, Object> responseHashMap = new HashMap<>();
        HashMap<String,Object> dataHashMap = new HashMap<>();

        // Set the response type
        responseHashMap.put("type", ResponseType.JOIN_ROOM_SUCCEEDED.getResponseType());

        // Add the data
        dataHashMap.put("roomCode", roomCode);
        dataHashMap.put("players", room.getPlayersAsString());

        // Add the data to the response
        responseHashMap.put("data", dataHashMap);
        
        // Convert the response HashMap to a JSON string
        String response = ResponseBuilder.buildResponse(responseHashMap);

        // Notify the player that they have joined the room
        player.getConn().send(response); 


    }

    private static void createRoomRequest(Request request,HashMap<String,Object> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private static void startGameRequest(Request request,HashMap<String,Object> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private static void wordSubmissionRequest(Request request,HashMap<String,Object> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private static void startMatchmakingRequest(Request request,HashMap<String,Object> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}

