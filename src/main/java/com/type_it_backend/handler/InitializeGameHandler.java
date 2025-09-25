package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class InitializeGameHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        if (room.isInGame()) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Game already initialized"));
            return;
        }

        // Broadcast "game_started" to all players (starts countdown on client)
        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        // Schedule the first round to start after the countdown (5 seconds)
        scheduler.schedule(() -> {
            room.setInGame(true);
            room.setCurrentTopic(""); // ensure empty so NewRoundHandler picks a new topic
            NewRoundHandler.handle(roomCode);
        }, 5, TimeUnit.SECONDS);
    }
}
