package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class InitializeGameHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room != null) {
            if (room.isInGame()) {
                request.getSenderConn().send(ResponseFactory.errorResponse("Game already initialized"));
                return;
            }

            
            room.broadcastResponse(ResponseFactory.startGameResponse(room));
        } else {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(room.getCurrentTopic() == null)
            NewRoundHandler.handle(request, data);
    }
}
