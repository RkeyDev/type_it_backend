package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.RequestType;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.RandomCodeGenerator;

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

        HashMap<String, String> playerData = stringToHashMap(data.get("player"));
        Player player = new Player(playerData.get("name"), playerData.get("skinPath"), request.getSenderConn());
        
        Room room = RoomManager.getRoomByCode(roomCode);
        if (room == null) {
            throw new IllegalArgumentException("Room with code " + roomCode + " does not exist");
            
        }

        room.getPlayers().put(player.getPlayerId(), player); // Add player to the room's player map

        player.getConn().sendText("joined_room",false);


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

    private HashMap<String, String> stringToHashMap(String str) {
        HashMap<String, String> map = new HashMap<>();
        if (str == null || str.isEmpty()) {
            return map;
        }
        String[] pairs = str.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");

            if (keyValue.length == 2) { 
                map.put(keyValue[0].trim(), keyValue[1].trim()); 
            }
        }
        return map;
    }


}

