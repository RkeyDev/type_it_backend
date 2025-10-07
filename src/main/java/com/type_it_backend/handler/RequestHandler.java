package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.RequestType;
import com.type_it_backend.services.RoomManager;

public class RequestHandler {

    public static void handle(Request request) {
        RequestType requestType = request.getRequestType();
        HashMap<String, Object> requestData = request.getData();

        if (requestType == null) {
            throw new IllegalArgumentException("Request type cannot be null");
        }

        switch (requestType) {
            case JOIN_ROOM -> JoinRoomHandler.handle(request, requestData);
            case CREATE_ROOM -> CreateRoomHandler.handle(request, requestData);
            case START_GAME -> {
                String roomCode = requestData.get("roomCode").toString();
                Room room = RoomManager.getRoomByCode(roomCode);
                String senderUsername = (String) request.getData().get("host");

                if (room != null && senderUsername.equals(room.getHost().getPlayerName())) {
                    StartGameHandler.handle(request, requestData);
                }
            }
            case INITIALIZE_GAME -> {
                String roomCode = requestData.get("roomCode").toString();
                Room room = RoomManager.getRoomByCode(roomCode);
                String senderUsername = (String) request.getData().get("username");

                if (room != null && senderUsername.equals(room.getHost().getPlayerName())) {
                    InitializeGameHandler.handle(request, requestData);
                }
            }
            case START_MATCHMAKING -> MatchmakingHandler.handle(request, requestData);
            case WORD_SUBMISSION -> WordSubmissionHandler.handle(request, requestData);
            case START_NEW_ROUND -> {
                String roomCode = requestData.get("roomCode").toString();
                Room room = RoomManager.getRoomByCode(roomCode);
                String requestSenderUsername = (String) request.getData().get("username");

                if (room != null && requestSenderUsername.equals(room.getHost().getPlayerName())) {
                    NewRoundHandler.handle(room);
                }
            }
            default -> throw new UnsupportedOperationException("Request type not supported: " + requestType);
        }
    }
}
