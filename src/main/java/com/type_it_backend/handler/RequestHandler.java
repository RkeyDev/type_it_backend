package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.RequestType;
import com.type_it_backend.enums.ResponseType;
import com.type_it_backend.services.RoomManager;

public class RequestHandler {

    /**
     * Handles incoming requests based on their type.
     * @param request The incoming request to handle.
     */

    public void handle(Request request) {
        RequestType requestType = request.getRequestType();
        HashMap<String, String> requestData = request.getData();

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

    private void joinRoomRequest(Request request,HashMap<String,String> data) {
        String roomCode = data.get("roomCode");

        if (roomCode == null || roomCode.isEmpty()) {
            throw new IllegalArgumentException("Room code cannot be null or empty");
        }

        HashMap<String, String> playerData = Request.stringToHashMap(data.get("player"));
        Player player = new Player(playerData.get("name"), playerData.get("skinPath"), request.getSenderConn());
        
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            // Notify the player that they couldn't join the room
            player.getConn().sendText(ResponseType.JOIN_ROOM_FAILED.getResponseType(),false); 
            throw new IllegalArgumentException("Room with code " + roomCode + " does not exist");
            
        }
        
        // Add player to the room's player map. Returns null if player sucsessfully added, else return existing player
        Player playerExisting = room.getPlayers().putIfAbsent(player.getPlayerId(), player); 

        if (playerExisting != null) {
            // Notify the player that they couldn't join the room
            player.getConn().sendText(ResponseType.JOIN_ROOM_FAILED.getResponseType(),false); 
            throw new IllegalArgumentException("Player is already in the room");
        }



        // Notify the player that they have joined the room
        player.getConn().sendText(ResponseType.JOIN_ROOM.getResponseType(),false); 


    }

    private void createRoomRequest(Request request,HashMap<String,String> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private void startGameRequest(Request request,HashMap<String,String> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private void wordSubmissionRequest(Request request,HashMap<String,String> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private void startMatchmakingRequest(Request request,HashMap<String,String> data) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}

